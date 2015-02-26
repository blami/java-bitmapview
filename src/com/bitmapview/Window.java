package com.bitmapview;

import com.bitmapview.format.BMP;
import com.bitmapview.format.PCX;
import com.sun.glass.ui.CommonDialogs;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Hlavni trida aplikace. Predstavuje okno, ktere je bud prazdne, nebo zobrazuje
 * bitmapu.
 */
public class Window
	extends JFrame
	implements ActionListener
{
	private Bitmap bitmap = null;
	private boolean modified = false;

	final private Dimension emptySize = new Dimension(200, 200);


	// Konstruktory
	/**
	 * Vytvori nove okno a zobrazi predanou bitmapu
	 * @param bitmap 	bitmapa, ktera ma byt zobrazena, nebo null
	 */
	public Window(Bitmap bitmap) {
		super();

		// Inicializovat okno a menu
		this.initializeWindow();
		this.initializeMenu();

		this.setVisible(true);

		// Nastavit prazdnou bitmapu
		this.setBitmap(bitmap);
	}

	/**
	 * Implicitni konstruktor.
	 */
	public Window() {
		this(null);
	}


	// Verejne metody
	/**
	 * Nastavi bitmapu do okna.
	 * @param bitmap	bitmapa, ktera ma byt zobrazena, nebo null
	 */
	public void setBitmap(Bitmap bitmap) {
		// Zeptame se na ulozeni predchozich zmen, pokud nejake jsou
		this.saveModified();

		// Nastavime novou bitmapu
		this.bitmap = bitmap;
		// Odstranime puvodni bitmapu z okna
		this.getContentPane().removeAll();
		if(this.bitmap != null) {
			this.add(this.bitmap);
			// Nastavime minimalni velikost okna tak aby neslo zmensit vice nez
			// na velikost bitmapy (+ vyska menu). Pokud je bitmapa moc mala
			// pouzijeme jako zaklad prazdnou velikost.
			int exactWidth = this.bitmap.getMinimumSize().width;
			int exactHeight = this.getJMenuBar().getHeight()
					+ this.bitmap.getMinimumSize().height;
			this.setMinimumSize(new Dimension(
					exactWidth < this.emptySize.width
							? this.emptySize.width
							: exactWidth,
					exactHeight < this.emptySize.height
							? this.emptySize.height
							: exactHeight));
			// Nastavime titulek na jmeno obrazku
			this.setTitle("Bitmap Viewer - " + this.bitmap.getName());
		} else {
			this.setMinimumSize(this.emptySize);
			// Nastavime prazdny titulek
			this.setTitle("Bitmap Viewer");
		}
		// Modifikacni priznak
		this.modified = false;

		// Prizpusobime velikost okna
		this.pack();
	}

	/**
	 * Vrati instanci aktualni bitmapy zobrazene v okne.
	 * @return			bitmapa, ktera je zobrazena, nebo null
	 */
	public Bitmap getBitmap() {
		return this.bitmap;
	}


	// Privatni metody
	/**
	 * Inicializovat okno.
	 */
	private void initializeWindow() {
		this.setTitle("Bitmap Viewer");
		this.getContentPane().setBackground(Color.BLACK);
		// Pouzivame GridBag layout manazer aby obrazek byl pri zvetseni okna
		// vzdy vycentrovany na stred.
		this.getContentPane().setLayout(new GridBagLayout());

		// Uvolnit pamet okna pri zavreni, v pripade ze je zavreno posledni okno
		// ukoncit aplikaci.
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Vytvorit roletkove menu v okne.
	 */
	private void initializeMenu() {
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();

		// File
		menu = new JMenu("File");
		menu.add(new JMenuItem("Open", 'O')).addActionListener(this);
		menu.add(new JMenuItem("Save", 'S')).addActionListener(this);
		menu.add(new JMenuItem("Info", 'I')).addActionListener(this);
		menu.addSeparator();
		menu.add(new JMenuItem("Exit", 'x')).addActionListener(this);
		menuBar.add(menu);

		// Edit
		menu = new JMenu("Edit");
		menu.add(new JMenuItem("Rotate 90CW", 'R')).addActionListener(this);
		menu.add(new JMenuItem("Rotate 90CCW", 't')).addActionListener(this);
		menu.add(new JMenuItem("Vert. mirror", 'V')).addActionListener(this);
		menu.add(new JMenuItem("Horiz. mirror", 'H')).addActionListener(this);
		menu.add(new JMenuItem("Invert colors", 'n')).addActionListener(this);
		menuBar.add(menu);


		this.setJMenuBar(menuBar);
	}

	/**
	 * Nabidnout ulozeni obrazku pokud byl od otevreni upraven.
	 */
	private void saveModified() {
		if (this.modified) {
			int result = JOptionPane.showConfirmDialog(this,
					"Picture was modified since opened.\n"
					+"Do you want to save changes?",
					"Save changes?",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				this.save(null);
			}
		}
	}

	/**
	 * Zobrazit dialog pro otevreni obrazku z pevneho disku a pokud je uspesne
	 * vybran soubor otevrit ho.
	 * @param f
	 */
	public void open(File f) {
		// Pokud neni soubor predan primo zobrazime dialog
		if (f == null) {
			JFileChooser chooser = new JFileChooser();

			// Povolit pouze vyber BMP a PCX souboru
			chooser.removeChoosableFileFilter(
					chooser.getChoosableFileFilters()[0]);
			chooser.addChoosableFileFilter(new FileNameExtensionFilter(
					"Bitmap files (*.bmp, *.pcx)", "bmp", "pcx"));
			// Nastavit adresar s aplikaci
			chooser.setCurrentDirectory(
					new File(System.getProperty("user.dir")));

			// Zobrazit dialog
			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				f = chooser.getSelectedFile();
			}
		}

		// Pokud byl vybran soubor bude f nastaveno
		if (f != null) {
			// Nacist soubor
			try {
				Bitmap bitmap = null;

				// Podle koncovky pouzit prislusny format
				if (f.getName().toLowerCase().endsWith(".bmp"))
					bitmap = BMP.load(f);
				else if (f.getName().toLowerCase().endsWith(".pcx"))
					bitmap = PCX.load(f);
				else
					throw new Exception("Unsupported file type");

				this.setBitmap(bitmap);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Ulozi obrazek ve 24bit variante jednoho z formatu. Podporujeme pouze
	 * 24bpp z duvodu konverze mezi formaty (v nekterych pripadech by byla
	 * nutna barevna redukce, ktera neni predmetem prace).
	 *
	 * @param f					objekt souboru do ktereho bude obrazek ulozen
	 */
	public void save(File f) {
		// Pokud neni soubor zadan primo zobrazime dialog
		if (f == null) {
			JFileChooser chooser = new JFileChooser();
			// Povolit pouze vyber 24bpp BMP a PCX souboru
			chooser.removeChoosableFileFilter(
					chooser.getChoosableFileFilters()[0]);
			chooser.addChoosableFileFilter(new FileNameExtensionFilter(
					"Bitmap (24bpp) file (*.bmp)", "bmp"));
			chooser.addChoosableFileFilter(new FileNameExtensionFilter(
					"Bitmap (24bpp) file (*.pcx)", "pcx"));

			// Nastavit adresar s aplikaci
			chooser.setCurrentDirectory(
					new File(System.getProperty("user.dir")));

			// Zobrazit dialog
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
				f = chooser.getSelectedFile();
			// Pokud nebyla zadana koncovka souboru pripojime ji
			if ((f != null) && (f.getName().lastIndexOf('.') == -1)) {
				String desc = chooser.getFileFilter().getDescription();
				if (desc.endsWith("bmp)"))
					f = new File(f.getAbsolutePath() + ".bmp");
				else if (desc.endsWith("pcx)"))
					f = new File(f.getAbsolutePath() + ".pcx");
			}
		}

		// Pokud byl vybran soubor bude f nastaveno
		if (f != null) {
			try {
				// Podle koncovky pouzit prislusny format
				if (f.getName().endsWith(".bmp"))
					BMP.save(this.bitmap, f);
				else if (f.getName().endsWith(".pcx"))
					PCX.save(this.bitmap, f);
				else
					throw new Exception("Unsupported file type");
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Zobrazit informace o souboru (hlavicky).
	 */
	private void showInfo() {
		String info = "No file opened.";
		if (this.bitmap != null) {
			info = "File (" + this.bitmap.getName() +") Information \n";
			Iterator it = this.bitmap.getHeaders().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				info += pair.getKey() + ": " + pair.getValue() + "\n";
			}
		}
		// Zobrazit dialog
		JOptionPane.showMessageDialog(this, info, "Info",
				JOptionPane.INFORMATION_MESSAGE);
	}


	// Pretizeni metod
	@Override
	public void actionPerformed(ActionEvent e) {
		// File
		if (e.getActionCommand().equals("Open")) {
			this.open(null);
		}
		if (e.getActionCommand().equals("Save")) {
			this.save(null);
		}
		else if (e.getActionCommand().equals("Info")) {
			this.showInfo();
		}
		else if (e.getActionCommand().equals("Exit")) {
			// Zeptame se na ulozeni predchozich zmen, pokud nejake jsou
			this.saveModified();

			// Zavrit okno
			this.dispose();
		}

		// Edit
		else if (e.getActionCommand().equals("Rotate 90CW")) {
			this.bitmap.rotate(false);
			this.modified = true;
		}
		else if (e.getActionCommand().equals("Rotate 90CCW")) {
			this.bitmap.rotate(true);
			this.modified = true;
		}
		else if (e.getActionCommand().equals("Vert. mirror")) {
			this.bitmap.mirror(false);
			this.modified = true;
		}
		else if (e.getActionCommand().equals("Horiz. mirror")) {
			this.bitmap.mirror(true);
			this.modified = true;
		}
		else if (e.getActionCommand().equals("Invert colors")) {
			this.bitmap.invertColors();
			this.modified = true;
		}
	}


	// Main
	/**
	 * Vstupni bod programu. Metoda vytvori nove prazdne okno a zobrazi jej.
	 * @param args		argumenty predane programu prikazovou radkou
	 */
	public static void main(String args[]) {
		Window window = new Window();
	}
}
