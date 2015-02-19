package com.bitmapview;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
		this.askToSaveModified();

		// Nastavime novou bitmapu
		this.bitmap = bitmap;
		// Odstranime puvodni bitmapu z okna
		this.getContentPane().removeAll();
		if(this.bitmap != null) {
			this.add(this.bitmap);
			// Nastavime minimalni velikost okna tak aby neslo zmensit vice nez
			// na velikost bitmapy (+ vyska menu)
			this.setMinimumSize(new Dimension(
					this.getMinimumSize().width,
					this.getJMenuBar().getHeight() + this.getMinimumSize().height
			));
			// Prizpusobime velikost okna
			this.pack();
		} else {
			this.setMinimumSize(this.emptySize);
		}
		this.modified = true;
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

		// Soubor
		menu = new JMenu("File");
		menu.add(new JMenuItem("Open", 'O')).addActionListener(this);
		menu.addSeparator();
		menu.add(new JMenuItem("Exit", 'x')).addActionListener(this);
		menuBar.add(menu);

		this.setJMenuBar(menuBar);
	}

	/**
	 * Nabidnout ulozeni obrazku pokud byl od otevreni upraven.
	 */
	private void askToSaveModified() {
		if (this.modified) {
			JOptionPane.showConfirmDialog(this,
					"Picture was modified since opened.\n"
					+"Do you want to save changes?",
					"Save changes?",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			// TODO ulozit zmeny v this.bitmap
		}
	}


	// Pretizeni metod
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Exit")) {
			// Zeptame se na ulozeni predchozich zmen, pokud nejake jsou
			this.askToSaveModified();

			// Zavrit okno
			this.dispose();
		}
	}


	// Main
	/**
	 * Vstupni bod programu. Metoda vytvori nove prazdne okno a zobrazi jej.
	 * @param args		argumenty predane programu prikazovou radkou
	 */
	public static void main(String args[]) {
		Bitmap test = new Bitmap();
		test.setSize(640, 480);

		Window window = new Window(test);
	}
}
