package com.bitmapview;

import javax.swing.*;
import java.awt.*;

/**
 * Trida reprezentujici bitmapu zobrazenou v okne Window. Zobrazuje bitmapu jako
 * pole barevnych bodu usporadanych v souradnem systemu. Neni zavisla na
 * konkretni bitove hloubce/formatu obrazku.
 */
public class Bitmap
	extends JPanel
{
	Dimension size;
	String name;

	Color pixels[][];


	// Konstruktor
	/**
	 * Vytvori prazdnou bitmapu o rozmerech 0x0.
 	 */
	public Bitmap() {
		this.name = "Unnamed";
		this.setSize(0, 0);
	}


	// Verejne metody
	/**
	 * Nastavit velikost bitmapy se zachovanim puvodniho obrazku v levem hornim
	 * rohu a doplnenim bilou barvou pokud jde o zvetseni, v opacnem pripade
	 * oriznuti.
	 * @param width		sirka bitmapy
	 * @param height	vyska bitmapy
	 */
	public void setSize(int width, int height) {
		Color newPixels[][] = new Color[width][height];

		// Zkopriovat puvodni obrazek do leveho horniho rohu a doplnit bilou
		// barvou. V pripade zmenseni useknout.
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++) {
				if (x < this.getSize().width && y < this.getSize().height) {
					// Zkopirovat puvodni barvu
					newPixels[x][y] = pixels[x][y];
				} else {
					newPixels[x][y] = Color.WHITE;
				}
			}

		// Nastavit novy obrazek a velikost
		this.pixels = newPixels;
		this.size = new Dimension(width, height);

		// Prekreslit bitmapu (nutne pro bitmapy zobrazene ve Window)
		this.repaint();
	}

	/**
	 * Vrati aktualni rozmery bitmapy
	 * @return			aktualni rozmery bitmapy
	 */
	public Dimension getSize() {
		return this.size;
	}

	/**
	 * Nastavi barvu pixelu na pozici x, y.
	 * @param x			souradnice pixelu v ose X
	 * @param y			souradnice pixelu v ose Y
	 * @param color		barva pixelu
	 */
	public void setPixel(int x, int y, Color color) {
		// TODO overit rozsah souradnic jinak vyjimka
		this.pixels[x][y] = color;
	}

	/**
	 * Vrati barvu pixelu na pozici x, y.
	 * @param x			souradnice pixelu v ose X
	 * @param y			souradnice pixelu v ose Y
	 * @return			barva pixelu
	 */
	public Color getPixel(int x, int y) {
		// TODO overit rozsah souradnic jinak vyjimka
		return this.pixels[x][y];
	}


	// Pretizeni metod
	@Override
	public Dimension getPreferredSize() {
		return this.getSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return this.getSize();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Vykreslit jednotlive body
		for(int x = 0; x < this.getSize().width; x++)
			for(int y = 0; y < this.getSize().height; y++) {
				g.setColor(this.getPixel(x,y));
				g.drawLine(x, y, x, y);
			}
	}
}
