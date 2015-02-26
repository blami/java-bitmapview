package com.bitmapview.format;

import com.bitmapview.Bitmap;
import com.bitmapview.io.DataInputStreamLE;
import com.bitmapview.io.DataOutputStreamLE;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * Trida pro praci s formatem PCX.
 */
public class PCX {

	public static Bitmap load (File f) throws Exception {
		// Pokud nebyl predan soubor neni treba cokoliv delat
		if(f == null)
			return null;

		// Otevrit soubor pro binarni cteni
		DataInputStreamLE reader = new DataInputStreamLE(
				new FileInputStream(f));
		// Pomocna promenna pro ruzne odmerovani prectenych dat
		int readBytes;

		// Vytvorime objekt Bitmap
		Bitmap pcx = new Bitmap(f.getName());

		/* PCX ma nasledujici tvar:
		 * hlavicka (128b)
		 * pixeldata
		 */


		/* Hlavicka
		 * 1b - identifikator (vzdy 0x0A)
		 * 1b - verze
		 * 1b - format
		 * 1b - bpp
		 * 2b - xstart
		 * 2b - ystart
		 * 2b - xend
		 * 2b - yend
		 * 2b - horiz. rozliseni
		 * 2b - vert. rozliseni
		 * 48b - EGA paleta (16 barev)
		 * 1b - reserved
		 * 1b - planes
		 * 2b - bytes per line
		 * 2b - typ palety (barevna/seda)
		 * 2b - horiz. velikost obrazovky
		 * 2b - vert. velikost obrazovky
		 * 54b - reserved
		 */
		if (reader.readByte() != 0x0A)
			throw new Exception("Invalid PCX file header");
		// Verze
		int version = reader.readByte() & 0xFF;
		pcx.addHeader("PCX Version", String.valueOf(version));
		if (reader.readByte() != 1)
			throw new Exception("Unsupported PCX encoding (not RLE)");
		// Bpp
		int bpp = reader.readByte() & 0xFF;
		pcx.addHeader("Bpp", String.valueOf(bpp));
		// Velikost obrazku je spocitana z rozdilu xend-xstart x yend-ystart
		int xStart = reader.readShort();
		int yStart = reader.readShort();
		int xEnd = reader.readShort();
		int yEnd = reader.readShort();
		Dimension imageSize = new Dimension(
				xEnd - xStart + 1,
				yEnd - yStart + 1);
		pcx.setSize(imageSize);
		pcx.addHeader("Size", imageSize.width + "px * "
				+ imageSize.height + "px");
		// Rozliseni (ignorujeme)
		reader.skipBytes(4);
		// Barevna mapa EGA (pro 16 barev). Pozn: 256+ barev je na konci souboru
		byte[] egaPalette = new byte[48];
		reader.read(egaPalette);
		// Reserved
		reader.skipBytes(1);
		// Pocet planes (podporujeme pouze kombinace 1 plane nebo 3 planes a
		// 8 bpp = 24bit)
		int planes = reader.readByte() & 0xFF;
		if (!((planes == 3 && bpp == 8) || (planes == 1)))
			throw new Exception("Unsupported number of planes");
		pcx.addHeader("Color planes", String.valueOf(planes));

		// Bytes per line
		int bytesPerLine = reader.readShort();
		// 2b typ palety, 2b + 2b velikost obrazovky, 54b reserved
		reader.skipBytes(60);

		// DEBUG
		/*
		System.out.println("DEBUG header ["
				+ "version=" + version
				+ ", bpp=" + bpp
				+ ", size=" + imageSize.width + "x" + imageSize.height
				+ ", planes=" + planes
				+ ", bytesPerline=" + bytesPerLine
				+ "]");
		*/

		/* Pixeldata
		 */
		readBytes = 128; // v readBytes budeme odmerovat offset 768b od konce,
		// zatim mame prectenu hlavicku souboru

		// Delka jedne radky v bytech
		int scanLineLength = planes * bytesPerLine;
		int padding = (scanLineLength * (8 / bpp)) - imageSize.width;

		// Buffer je 2d pole usporadane po radkach
		byte[][] buffer = new byte[imageSize.height][scanLineLength];

		for (int l = 0; l < imageSize.height; l++) {
			int i = 0;
			do {
				// Nacteme byte ze souboru
				byte b = reader.readByte();
				readBytes++;

				// Predpoklad je ze nejde o run (RLE)
				byte runByte = b;
				int runLength = 1;
				// Pokud jsou nastaveny horni dva bity pak jde o run
				if ((b & 0xC0) == 0xC0) {
					// Delka runu je spodnich sest bitu
					runLength = b & 0x3F;
					// Opakuje se nasledujici byte
					runByte = reader.readByte();
					readBytes++;
				}
				// Zapsat cely beh do bufferu
				for(int r = 0; r < runLength; r++)
					buffer[l][i++] = runByte;

			} while (i < scanLineLength);
		}

		// DEBUG
		/*
		System.out.println("DEBUG pixeldata [read=" + readBytes
				+ ", length=" + f.length()
				+ ", offset=" + (f.length() - readBytes)
				+ "]");
		*/

     	/* Paleta
     	 * U formatu PCX nastavaji 3 situace.
     	 * 1/ obrazek je v EGA palete tj. max 16 barev, pak je paleta ulozena
     	 * primo v hlavicce. Toto plati i pro CGA paletu, kterou nepodporujeme
     	 * 2/ obrazek je ve VGA palete tj. max 256 barev. Tato paleta se do
     	 * hlavicky nevejde a najde se na offsetu 768b (= 256b*3) od konce
     	 * souboru signalizovana sekvenci 0C na offsetu 769b na tomto offsetu
     	 * 3/ obrazek ma 3 planes a 8bpp a je tedy 24b, v takovem pripade
     	 * neni paleta potreba
		 */
		Color palette[] = null;
		boolean hasVGApalette = false;


		// Jedinny pripad kdy nenacitame zadnou paletu je 24b obrazek
		if (!(bpp == 8 && planes == 3)) {
			// Pokud zbyva vice nez 769 bytes od konce preskocit tolik bytes
			// aby presne zbyvalo 769 bytes od konce.
			long paletteOffset = f.length() - readBytes;
			if(paletteOffset > 769) {
				reader.skipBytes((int) (paletteOffset - 769));
				paletteOffset = 769;
			}
			// Pokud je priznak 0x0C muzeme zacist cist VGA paletu
			if ((paletteOffset == 769) && (reader.readByte() == 0x0C))
				hasVGApalette = true;

			// EGA paleta
			if (!hasVGApalette) {
				palette = new Color[16]; // 2, 4, 8 nebo 16 EGA barev
				for (int i = 0; i < 48; i += 3) {
					int r = (egaPalette[i] >> 6) & 0xFF;
					int g = (egaPalette[i + 1] >> 6) & 0xFF;
					int b = (egaPalette[i + 2] >> 6) & 0xFF;
					palette[i / 3] = new Color(r, g, b);
				}
				pcx.addHeader("Palette", "EGA");
			}
			// VGA paleta
			else {
				// DEBUG
				//System.out.println("DEBUG vga palette found");
				palette = new Color[256];
				for (int i = 0; i < 768; i += 3) {
					int r = reader.readByte() & 0xFF;
					int g = reader.readByte() & 0xFF;
					int b = reader.readByte() & 0xFF;
					palette[i / 3] = new Color(r, g, b);
				}
				pcx.addHeader("Palette", "EGA");
			}
		}
		// TODO predat paletu do Bitmap objektu pro pripadne ukladani/export
		// FIXME VGA paleta


		// Zapsat vyslednou bitmapu
		for (int y = 0; y < imageSize.height; y++) {
			// Pro barevne hloubky mensi nez 8bpp
			byte tmpByte = 0;
			int tmpByteOffset = 0;
			int tmpX = 0;

			for (int x = 0; x < imageSize.width; x++) {
				// 1bpp (EGA)
				// co bit to index v EGA palete. V tmpByte mame ulozeny
				// aktualni byte z bufferu, dalsi indexujeme pomoci tmpX coz
				// je vlastne x/8 (na jeden byte je 8 pixelu). Pozici v
				// aktualne zpracovavanem byte uchovavame v tmpByteOffset
				if (bpp == 1 && palette != null) {
					if (tmpByteOffset == 0) {
						tmpByte = buffer[y][tmpX++];
						tmpByteOffset = 7;
					} else {
						tmpByteOffset--;
					}
					int i = (tmpByte >> tmpByteOffset) & 0x01;
					pcx.setPixel(x, y, palette[i]);
				}
				// 4bpp (EGA)
				// co 4 bity to index v EGA palete. Analogicky ke zpracovani
				// 1bit barevne hloubky pouzivame tmpByte a tmpX,
				// tmpByteOffset = 0 znaci prvni nibble (pulka byte) s
				// indexem v palete a tmpByteOffset = 1 druhy.
				else if (bpp == 4 && palette != null) {
					if (tmpByteOffset == 0) {
						tmpByte = buffer[y][tmpX++];
						tmpByteOffset = 1;
					} else {
						tmpByteOffset--;
					}
					int i = (tmpByte >> (tmpByteOffset * 4)) & 0x0F;
					pcx.setPixel(x, y, palette[i]);
				}
				// 8bpp (EGA/VGA)
				// index v palete
				else if (bpp == 8 && planes == 1) {
					int colorIndex = buffer[y][x] & 0xFF;
					if (colorIndex > 15 && hasVGApalette == false)
						throw new Exception("Missing VGA palette at 8bpp");
					pcx.setPixel(x, y, palette[colorIndex]);
				}
				// bez palety (24bpp)
				// pixely jsou usporadany tak, ze v kazde plane je jedna
				// barva. Tj. v bufferu je na indexech:
				// 0 - bytesPerLine-1 cervena slozka
				// bytesPerLine - bytesPerLine*2-1 zelena slozka
				// bytesPerLine*2 - scanLineLength-1 modra slozka
				else if (bpp == 8 && planes == 3) {
					int r = buffer[y][0 + x] & 0xFF;
					int g = buffer[y][bytesPerLine + x] & 0xFF;
					int b = buffer[y][bytesPerLine * 2 + x] & 0xFF;
					pcx.setPixel(x, y, new Color(r, g, b));
				} else
					throw new Exception("Invalid color encoding");
			}
		}

		// Zavrit soubor
		reader.close();

		return pcx;
	}

	/**
	 * Ulozi objekt typu Bitmap do 24bpp (3 plane x 8bpp) PCX souboru. Vzdy
	 * je uvazovana komprese RLE a nikdy neni obsazena paleta.
	 * @param pcx				objekt Bitmap
	 * @param f					objekt reprezentujici soubor kam ukladame
	 */
	public static void save(Bitmap pcx, File f) throws Exception {
		if(pcx == null || f == null)
			return;

		DataOutputStreamLE writer = new DataOutputStreamLE(
				new FileOutputStream(f));

		// Pocet bytes na jednu radku
		int bytesPerLine = pcx.getSize().width * 1; // 1 byte na plane

		// Tato metoda je pouze reverzi metody load a veskere potrebne
		// informace jsou zdokumentovany jiz tam.

		/* Hlavicka */
		writer.writeByte(0x0A); // identifikator PCX
		writer.writeByte(5); // verze PCX je vzdy 5 (podpora 24bpp)
		writer.writeByte(1); // RLE komprese
		writer.writeByte(8); // bpp
		// Rozmery obrazku
		writer.writeShort(0); // xStart
		writer.writeShort(0); // yStart
		writer.writeShort(pcx.getSize().width - 1); // xEnd
		writer.writeShort(pcx.getSize().height - 1); // yEnd
		// Rozliseni 600x600 dpi (neni dulezite)
		writer.writeShort(600);
		writer.writeShort(600);
		// EGA paleta (vse 0)
		writer.write(new byte[48]);
		writer.writeByte(0); // reserved
		// Pocet planes - vzdy 3 pro kombinaci 8x3
		writer.writeByte(3);
		// Pocet bytes na jednu radku obrazku
		writer.writeShort(bytesPerLine);
		// Typ palety
		writer.writeShort(1); // ignorovano
		// Velikost obrazovky (vyrezu)
		writer.writeShort(pcx.getSize().width); // sirka
		writer.writeShort(pcx.getSize().height); // vyska
		// Reserved
		writer.write(new byte[54]); // 0

		/* Pixeldata */
		int scanLineLength = 3 * bytesPerLine;
		for (int l = 0; l < pcx.getSize().height; l++) {
			// buffer pro jednu radku
			byte[] buffer = new byte[scanLineLength];
			// do promenne buffer nacteme celou radku z Bitmap objektu
			// kazda barva je reprezentovana jednim byte v prislusne plane
			// viz. nacitaci funkce pro PCX bez palety vyse
			for (int x = 0; x < pcx.getSize().width; x++) {
				buffer[0 + x] = (byte) pcx.getPixel(x, l).getRed();
				buffer[bytesPerLine + x] = (byte) pcx.getPixel(x, l).getGreen();
				buffer[bytesPerLine * 2 + x ]
						= (byte) pcx.getPixel(x, l).getBlue();
			}

			// provedeme RLE kompresi a zapis do souboru
			int i = 0;
			do {
				int runLength = 0;
				// spocitame kolik (do 63 coz je maximalni delka runu) je
				// stejnych byte vedle sebe
				while (runLength < 62 && i + runLength + 1 < scanLineLength
						&& buffer[i + runLength] == buffer [i + runLength +	1])
					runLength++;

				// Pokud je run delsi nez jedna provedeme kompresi
				if (runLength > 0) {
					// zapiseme RLE byte s delkou runu
					writer.writeByte(runLength + 1 | 0xC0);
					// zapiseme opakujici se byte
					writer.writeByte(buffer[i]);
					// posuneme se v radce az za sekvenci, kterou jsme zapsali
					i += runLength + 1;
				} else {
					// musime osetrit situaci kdy delka runu je 1 (pokud je
					// hodnota 0xC0 to prijde na dva byte 0xC1 + hodnota
					// co se opakuje 1x)
					if ((buffer[i] & 0xC0) == 0xC0)
						writer.writeByte(0xC1); // 1 opakovani nasled. byte
					writer.writeByte(buffer[i++]);
				}

			} while (i < scanLineLength);
		}

		// Zavrit soubor
		writer.close();
	}

}
