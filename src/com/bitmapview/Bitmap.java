package com.bitmapview;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

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
	HashMap<String, String> headers;
	//Color palette[];
	Color pixels[][];


	// Konstruktor
	/**
	 * Vytvori prazdnou bitmapu o rozmerech 0x0.
 	 */
	public Bitmap(String name) {
		this.name = (name == null) ? "Unnamed" : name;
		this.headers = new HashMap<String, String>();
		//this.palette = null;

		this.setSize(0, 0);
	}


	// Verejne metody
	/**
	 * Nastavit velikost bitmapy se zachovanim puvodniho obrazku v levem hornim
	 * rohu a doplnenim bilou barvou pokud jde o zvetseni, v opacnem pripade
	 * oriznuti.
	 * @param width				sirka bitmapy
	 * @param height			vyska bitmapy
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
		this.updateUI();
	}

	/**
	 * Nastavi velikost bitmapy.
	 * @param d					rozmery bitmapy
	 */
	public void setSize(Dimension d) {
		if (d == null)
			this.setSize(0, 0);
		else
			this.setSize(d.width, d.height);
	}

	/**
	 * Vrati aktualni rozmery bitmapy.
	 * @return					aktualni rozmery bitmapy
	 */
	public Dimension getSize() {
		return this.size;
	}

	/**
	 * Nastavi hodnotu hlavicky.
	 * @param key				klic (nazev pole)
	 * @param value				hodnota
	 */
	public void addHeader(String key, String value) {
		this.headers.put(key, value);
	}

	/**
	 * Vrati hlavicky.
	 * @return					hlavicky
	 */
	public HashMap<String, String> getHeaders() {
		return this.headers;
	}

	/**
	 * Vrati nazev obrazku
	 */
	public String getName() {
		return this.name;
	}


	/**
	 * Nastavi barvu pixelu na pozici x, y.
	 * @param x					souradnice pixelu v ose X
	 * @param y					souradnice pixelu v ose Y
	 * @param color				barva pixelu
	 */
	public void setPixel(int x, int y, Color color) {
		// TODO overit rozsah souradnic jinak vyjimka
		this.pixels[x][y] = color;
	}

	/**
	 * Vrati barvu pixelu na pozici x, y.
	 * @param x					souradnice pixelu v ose X
	 * @param y					souradnice pixelu v ose Y
	 * @return					barva pixelu
	 */
	public Color getPixel(int x, int y) {
		// TODO overit rozsah souradnic jinak vyjimka
		return this.pixels[x][y];
	}


	// Operace
	/**
	 * Provede rotaci obrazku o 90 stupnu po smeru hodinovych rucicek
	 * @param counterClockwise	rotace proti smeru
	 */
	public void rotate(boolean counterClockwise) {
		int width = this.getSize().width;
		int height = this.getSize().height;
		Color newPixels[][] = new Color[height][width];

		// Provedeme rotaci (presoupeme pixely do noveho 2D pole)
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				if (counterClockwise == false) {
					newPixels[height - y - 1][x] = this.pixels[x][y];
				} else {
					newPixels[y][width - x - 1] = this.pixels[x][y];
				}
		// Nastavime nove rozmery a nahradime pixely
		this.setSize(height, width);
		this.pixels = newPixels;
	}

	/**
	 * Provede efekt zrcadleni obrazku v jedne nebo druhe ose
	 * @param horizontal		zrcadleni v ose Y
	 */
	public void mirror(boolean horizontal) {
		int width = this.getSize().width;
		int height = this.getSize().height;
		Color newPixels[][] = new Color[width][height];

		// Provedeme operaci zrcadleni do noveho 2D pole
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				if (horizontal == false) {
					newPixels[x][y] = this.pixels[width - x - 1][y];
				} else {
					newPixels[x][y] = this.pixels[x][height - y - 1];
				}
		// Nahradime pixely a prekreslime obrazek
		this.pixels = newPixels;
		this.repaint();
	}

	/**
	 * Provedeme barevnou inverzi v celem RGB prostoru bez ohledu na paletu,
	 * pocet barev bude tedy zachovan (akorat budou jine).
	 */
	public void invertColors() {
		int width = this.getSize().width;
		int height = this.getSize().height;
		Color newPixels[][] = new Color[width][height];

		// Pro kazdy pixel spocitame inverzni barvu (odecteme od 255)
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				int r = this.pixels[x][y].getRed();
				int g = this.pixels[x][y].getGreen();
				int b = this.pixels[x][y].getBlue();
				newPixels[x][y] = new Color(255-r, 255-g, 255-b);
			}

		// Nahradime pixely a prekreslime obrazek
		this.pixels = newPixels;
		this.repaint();
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
				g.setColor(this.getPixel(x, y));
				g.drawLine(x, y, x, y);
			}
	}
}
