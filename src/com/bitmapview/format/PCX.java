package com.bitmapview.format;

import com.bitmapview.Bitmap;
import com.bitmapview.util.DataInputStreamLE;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;


/**
 * Trida pro nacitani PCX.
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
			throw new Exception("Invalid PCX file format");
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
		byte[] egaColorTable = new byte[48];
		reader.read(egaColorTable);
		// Reserved
		reader.skipBytes(1);
		// Pocet planes
		int planes = reader.readByte() & 0xFF;
		// Bytes per line
		int bytesPerLine = reader.readShort();
		// 2b typ palety, 2b + 2b velikost obrazovky, 54b reserved
		reader.skipBytes(60);

		// DEBUG
		System.out.println("DEBUG header ["
				+ "version=" + version
				+ ", bpp=" + bpp
				+ ", size=" + imageSize.width + "x" + imageSize.height
				+ ", planes=" + planes
				+ ", bytesPerline=" + bytesPerLine
				+ "]");


		/* Pixeldata
		 */
		readBytes = 128; // v readBytes budeme odmerovat offset 768b od konce,
		// zatim mame prectenu hlavicku souboru

		// Delka jedne radky v bytech
		int scanLineLength = planes * bytesPerLine;
		int padding = (scanLineLength * (8 / bpp)) - imageSize.width;

		// Buffer je 2d pole usporadane po radkach. Nejprve dekodujeme
		byte[][] buffer = new byte[imageSize.height][scanLineLength];
		int bufferFilled = 0;

		for (int l = 0; l < imageSize.height; l++) {
			int i = 0;
			do {
				byte b = reader.readByte();
				readBytes += 1;
				byte runByte = b;
				int runLength = 1;
				// RLE komprese, v pripade ze prvni dva bity jsou 11 pak jde
				// o tzv. run length - tedy opakujici se stejnou hodnotu
				if ((b & 0x0C) == 0x0C)  {
					// Delka opakovani (pro barvy od indexu 192 to muze byt i 1)
					runLength = b & 0x3F;
					// Opakujici se hodnota je dalsi byte
					runByte = reader.readByte();
					readBytes += 1;
				}

				// Expandujeme nactenou hodnotu do bufferu na prislusne radce
				for (bufferFilled += runLength;
					 	runLength > 0 && i < scanLineLength;
						i++, runLength--) {
					buffer[l][i] = runByte;
				}
			} while (i < scanLineLength);
		}


		// Po nacteni kazdeho byte zkontrolujeme zdali nejsme na offsetu 768b
		// od konce souboru


		// Prevedeme


     	/* Paleta
		 * u formatu PCX mohou nastat dve situace, paleta je bud EGA a pak je
		 * soucasti hlavicky (tj. v promenne egaColorTable) a nebo je VGA a
		 * pak je umistena na offsetu 768b (256*3b) od konce souboru coz se
		 * pozna podle priznaku 0x0C. Ten ale muze byt i primo v souboru
		 * takze musime nejprve nacist obrazova data a zjistit co vlastne na
		 * offsetu 768b od konce je.
		 */
		boolean hasVGApalette = false;



		// Zavrit soubor
		reader.close();

		return pcx;
	}

}
