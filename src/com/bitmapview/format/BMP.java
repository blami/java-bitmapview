package com.bitmapview.format;

import com.bitmapview.Bitmap;
import com.bitmapview.util.DataInputStreamLE;

import java.awt.*;
import java.io.*;

/**
 * Created by blami on 22. 2. 2015.
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

		// Vytvorime objekt Bitmap
		Bitmap bmp = new Bitmap(f.getName());

		/* BMP ma nasledujici tvar:
		 * hlavicka (14b)
		 *
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
		int length = reader.readInt();
		// Rezervovane bytes
		reader.skipBytes(4); // 2b + 2b
		int offset = reader.readInt();

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

		// Povinna cast vsech typu DIB hlavicek
		if (dibHeaderLength == 12 || dibHeaderLength == 64) {
			// 2b width + 2b height
			dibImageSize.setSize(reader.readShort(), reader.readShort());
		} else if (dibHeaderLength == 40 || dibHeaderLength >= 108) {
			// 4b width + 4b height
			dibImageSize.setSize(reader.readInt(), reader.readInt());
		}
		// 2b planes (musi byt 1) + 2b bpp
		if (reader.readShort() != 1)
			throw new Exception();
		dibBpp = reader.readShort();

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
		}

		// TODO preskocit veci pro 108+, prip 24b pro 12

		/* Paleta
		 */
		int[] palette;




		System.out.println(dibHeaderLength);
		System.out.println(dibImageSize.toString());
		System.out.println(dibPlanes);
		System.out.println(dibBpp);
		System.out.println(dibRawLength);
		System.out.println(dibPaletteLength);

		//System.out.println(size + "," + offset);



		return bmp;
	}
}
