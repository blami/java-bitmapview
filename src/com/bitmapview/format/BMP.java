package com.bitmapview.format;

import com.bitmapview.Bitmap;
import com.bitmapview.io.DataInputStreamLE;
import com.bitmapview.io.DataOutputStreamLE;

import java.awt.*;
import java.io.*;

/**
 * Trida pro praci s formatem BMP.
 */
public class BMP {

	// Verejne metody
	/**
	 * Precte BMP soubor a vrati objekt typu Bitmap obsahujici obrazek.
 	 * @param f					objekt File reprezentujici BMP soubor
	 * @return					objekt Bitmap s obrazkem nebo null
	 * @throws FileNotFoundException pokud nebyl soubor nalezen
	 */
	public static Bitmap load(File f) throws Exception {
		// Pokud nebyl predan soubor neni treba cokoliv delat
		if(f == null)
			return null;

		// Otevrit soubor pro binarni cteni
		DataInputStreamLE reader = new DataInputStreamLE(
				new FileInputStream(f));
		// Pomocna promenna pro ruzne odmerovani prectenych dat
		int readBytes;

		// Vytvorime objekt Bitmap
		Bitmap bmp = new Bitmap(f.getName());

		/* BMP ma nasledujici tvar:
		 * Hlavicka (14b)
		 * DIB hlavicka (14b)
		 * Paleta (volitelna, povinna pouze pro bpp <= 8)
		 * Pixeldata
		 */


		/* Hlavicka (14b)
		 * 2b - signature (BM)
		 * 4b - velikost BMP v bytes
		 * 4b - reserved
		 * 4b - offset oblasti s obrazovymi daty
		 */
		String signature = new String(new byte[] {
				(byte) reader.readByte(),
				(byte) reader.readByte()
		});
		bmp.addHeader("BMP Signature", signature);
		int length = reader.readInt();
		// Rezervovane bytes
		reader.skipBytes(4); // 2b + 2b
		int offset = reader.readInt();

		offset -= 14;

		// DEBUG
		/*
		System.out.println("DEBUG header ["
				+ "signature=" + signature
				+ ", offset=" + (offset + 14)
				+ "]");
		*/

		/* DIB hlavicka (existuje 7 typu, ale vsechny zacinaji velikosti
		 * hlavicky)
		 * 4b - velikost DIB hlavicky (vzdy)
		 * OS2/1.x BITMAPCOREHEADER (12)
		 * 2b - sirka v px
		 * 2b - vyska v px
		 * 2b - pocet planes
		 * 2b - barevna hloubka v bitech
		 * BITMAPINFOHEADER (40) resp. BITMAPV4HEADER+
		 * 4b - sirka v px
		 * 4b - vyska v px
		 * 2b - pocet planes
		 * 2b - barevna hloubka
		 * 4b - typ komprese
		 * 4b - velikost bitmapovych dat v bytes
		 * 4b - horizontalni rozliseni
		 * 4b - vertikalni rozliseni
		 * 4b - pocet barev v palete
		 * 4b - pocet dulezitych barev v palete
		 */
		int dibHeaderLength = reader.readInt();
		Dimension dibImageSize = new Dimension(0, 0);
		int dibPlanes = 1;
		int dibBpp = 8;
		int dibRawLength = 0;
		int dibPaletteLength = 0;

		readBytes = 4; // budeme si pocitat kolik z hlavicky uz mame
					   // precteno, abychom lehko skocili na jeji konec
		bmp.addHeader("BMP Header Length", String.valueOf(dibHeaderLength));

		// Povinna cast vsech typu DIB hlavicek
		if (dibHeaderLength == 12 || dibHeaderLength == 64) {
			// 2b width + 2b height
			dibImageSize.setSize(reader.readShort(), reader.readShort());
			readBytes += 4;
		} else if (dibHeaderLength == 40 || dibHeaderLength >= 108) {
			// 4b width + 4b height
			dibImageSize.setSize(reader.readInt(), reader.readInt());
			readBytes += 8;
		}
		bmp.addHeader("Size",
				dibImageSize.width + "px * " + dibImageSize.height + "px");
		bmp.setSize(dibImageSize);

		// 2b planes (musi byt 1) + 2b bpp
		if (reader.readShort() != 1)
			throw new Exception("Unsupported number of colorplanes in BMP");
		dibBpp = reader.readShort();
		readBytes += 4;
		bmp.addHeader("Bpp", String.valueOf(dibBpp));

		// Cast DIB hlavicek BITMAPINFOHEADER resp. BITMAPV4HEADER+
		if(dibHeaderLength == 40 || dibHeaderLength >= 108) {
			// 4b typ komprese (akceptujeme pouze 0 = BI_RGB)
			if (reader.readInt() != 0)
				throw new Exception();
			// 4b velikost bitmapovych dat (muze byt 0 u BI_RGB)
			dibRawLength = reader.readInt();
			if (dibRawLength == 0) {
				dibRawLength = dibImageSize.width * dibImageSize.height * dibBpp;
			}
			// 4b horiz. + 4b vert. rozliseni (neni treba vedet)
			reader.skipBytes(8);
			// 4b pocet barev v palete + 4b pocet dulezitych barev (ignorujeme)
			dibPaletteLength = reader.readInt();
			reader.skipBytes(4);
			readBytes += 24;
		}
		// Preskocit zbytek DIB hlavicky
		reader.skipBytes(dibHeaderLength - readBytes);
		offset -= dibHeaderLength;

		// Nepodporujeme jiny format nez BI_RGB takze se nemuze stat, ze
		// bychom zde meli nacitat barevne masky

		// Obrazky s 8bpp a mene musi mit paletu
		if (dibBpp <= 8 && offset == 0)
			throw new Exception("Missing BMP color table");

		// DEBUG
		/*
		System.out.println("DEBUG dibheader ["
				+ "headerlength=" + dibHeaderLength
				+ ",size=" + dibImageSize.width + "x" + dibImageSize.height
				+ ",bpp=" + dibBpp
				+ ",rawlength=" + dibRawLength
				+ ",palettelenght=" + dibPaletteLength
				+ "]");
		System.out.println("offset=" + offset);
		*/


		/* Paleta
		 */
		Color palette[] = null;
		// Obrazky s paletou a barevnou hloubkou vetsi nez 8bpp mohou mit
		// paletu, ale my ji preskocime.
		if (offset > 0 && dibBpp > 8) {
			reader.skipBytes(offset);
			offset = 0;
		}
		if (offset > 0) {
			readBytes = 0; // pocet prectenych bytes z palety

			// Pokud je delka palety 0 nastavime maximalni moznou delku (coz
			// je standard pro knihovny s kratkou hlavickou
			if(dibPaletteLength == 0)
				dibPaletteLength = (int) Math.pow(2, dibBpp);

			palette = new Color[dibPaletteLength];

			for (int i = 0; i < dibPaletteLength; i++) {
				int b = reader.readByte() & 0xFF;
				int g = reader.readByte() & 0xFF;
				int r = reader.readByte() & 0xFF;

				// RGB24 (BITMAPCOREHEADER)
				if (dibHeaderLength == 12 || dibHeaderLength == 64) {
					palette[i] = new Color(r, g, b);
					readBytes += 3;
				// RGBA32
				} else {
					int a = reader.readByte() & 0xFF;
					palette[i] = new Color(r, g, b);
					readBytes += 4;
				}
			}

			// DEBUG
			/*
			System.out.print("DEBUG palette [");
			for (int i = 0; i < dibPaletteLength; i++) {
				System.out.print(i + "=" + palette[i].getRed()
						+ "," + palette[i].getGreen()
						+ "," + palette[i].getBlue());
				if(i < dibPaletteLength - 1)
					System.out.print(";");
			}
			System.out.println("]");
			*/

			offset -= readBytes;
		}
		// U bitmap, kde jsme si domysleli barvy je treba dopreskocit
		if (offset != 0)
			reader.skipBytes(offset);
		// TODO nastavit paletu objektu Bitmap pro editace/export


		/* Pixeldata
		 */
		// Obrazova data jsou usporadana v radcich zarovnanych na 4 bytes
		int rowLength = ((dibBpp * dibImageSize.width + 31) / 32) * 4;
		//System.out.println(rowLength);
		// y-ova souradnice je v BMP inverzni
		// FIXME vyska muze byt zaporna, pak to znamena neinverzni

		for(int y = dibImageSize.height - 1; y >= 0; y--) {
			readBytes = 0;

			// Pro barevne hloubky mensi nez 8bpp
			byte tmpByte = 0;
			int tmpByteOffset = 0;

			for (int x = 0; x < dibImageSize.width; x++) {
				// Podporujeme pouze BI_RGB bez komprese

				// 1bpp (paleta)
				if (dibBpp == 1) {
					// tmpByteOffset urcuje primon n-ty pixel v byte
					if (tmpByteOffset == 0) {
						tmpByte = reader.readByte();
						// V nactenem byte postupujeme od nejmene duleziteho
						// bitu az k nejdulezitejsimu
						tmpByteOffset = 7;

						readBytes += 1;
					} else {
						tmpByteOffset--;
					}
					// Zjistime pravdivostni hodnotu bitu na tmpByteOffset-te
					// pozici coz je index bitu v palete.
					int i = (tmpByte >> tmpByteOffset) & 0x01;
					bmp.setPixel(x, y, palette[i]);
				}
				// 4bpp (paleta)
				else if (dibBpp == 4) {
					// tmpByteOffset je 0 pro prvni pixel (nibble) a 1 pro
					// druhy ve smyslu ze mene dulezity nibble je ten vice
					// vpravo (a tudiz bude nacten prvni)
					if (tmpByteOffset == 0) {
						tmpByte = reader.readByte();
						tmpByteOffset = 1;

						readBytes += 1;
					} else {
						tmpByteOffset--;
					}
					// Precteme cely nibble
					int i = (tmpByte >> (tmpByteOffset * 4)) & 0x0F;
					bmp.setPixel(x, y, palette[i]);
				}
				// 8bpp (paleta)
				else if (dibBpp == 8) {
					int i = reader.readByte() & 0xFF;
					bmp.setPixel(x, y, palette[i]);

					readBytes += 1;
				}
				// 24bpp 3 bytes na pixel 1b G, 1b B, 1b R
				else if (dibBpp == 24) {
					int b = reader.readByte() & 0xFF;
					int g = reader.readByte() & 0xFF;
					int r = reader.readByte() & 0xFF;

					bmp.setPixel(x, y, new Color(r, g, b));
					readBytes += 3;
				} else {
					throw new Exception("Unsupported BMP bpp");
				}
			}
			// Preskocit data do konce radku
			reader.skipBytes(rowLength - readBytes);
		}

		// Zavrit soubor
		reader.close();

		return bmp;
	}

	/**
	 * Ulozi objekt typu Bitmap do 24bpp BMP souboru. Pozn. pro funkci save
	 * neuvazujeme zadnou kompresi, pouze jednu plane a vzdy 24bpp bez palety.
	 * @param bmp				objekt Bitmap
	 * @param f					objekt reprezentujici soubor kam ukladame
	 */
	public static void save(Bitmap bmp, File f) throws Exception {
		if(bmp == null || f == null)
			return;

		DataOutputStreamLE writer = new DataOutputStreamLE(
				new FileOutputStream(f));

		// Tato metoda je pouze reverzi metody load a veskere potrebne
		// informace jsou zdokumentovany jiz tam.

		// Vypocteme delku souboru (14b + 40b + 3*sirka*vyska)
		int rawLength = 3 * (bmp.getSize().width * bmp.getSize().height);
		int length = 14 + 40 + rawLength;
		// Offset pixeldat je vzdy 54
		int offset = 54;

		/* Hlavicka */
		writer.write(new byte[] {0x42, 0x4D}); // BM
		writer.writeInt(length); // delka bitmapy v bytes
		writer.write(new byte[4]); // rezervovane bytes (4 x 0)
		writer.writeInt(offset); // offset pixeldat

		/* DIB hlavicka (BITMAPINFOHEADER) */
		writer.writeInt(40); // delka hlavicky
		// Rozmery bitmapy
		writer.writeInt(bmp.getSize().width);
		writer.writeInt(bmp.getSize().height);
		writer.writeShort(1); // pocet planes
		writer.writeShort(24); // bpp
		writer.writeInt(0); // typ komprese BI_RGB
		writer.writeInt(rawLength);
		// Rozliseni (600x600dpi), neni dulezite
		writer.writeInt(600);
		writer.writeInt(600);
		// Paletu nepouzivame
		writer.writeInt(0); // pocet barev v palete
		writer.writeInt(0); // pocet pouzitych barev

		// Delka radku (zarovnano na 4 byte)
		int rowLength = ((24 * bmp.getSize().width + 31) / 32) * 4;

		/* Pixeldata */
		for (int y = bmp.getSize().height - 1; y >= 0; y--) {
			// Pomocna promenna pro pocitani zapsanych bytes na radku kvuli
			// zarovnani
			int writtenBytes = 0;

			// Zapsat radek
			for (int x = 0; x < bmp.getSize().width; x++) {
				int r = bmp.getPixel(x, y).getRed();
				int g = bmp.getPixel(x, y).getGreen();
				int b = bmp.getPixel(x, y).getBlue();

				byte pixel[] = new byte[] {(byte) b, (byte) g, (byte) r};
				writer.write(pixel);
				writtenBytes += 3;
			}

			// Zarovnani na delku radku
			if (writtenBytes < rowLength)
				writer.write(new byte[rowLength - writtenBytes]);
		}

		// Zavrit soubor
		writer.close();
	}
}
