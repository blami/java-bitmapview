package com.bitmapview.io;

import java.io.*;

/**
 * Trida implementujici DataOutputStream akorat s tim ze zapis veskerych
 * datovych typu neprobiha s predpokladem razeni bytes v big-endian (network)
 * poradi, ale little-endian, kdy nejmene dulezity byt ma nejmensi adresu.
 * @see java.io.DataOutputStream
 */
public class DataOutputStreamLE
	extends FilterOutputStream
	implements DataOutput {

	// Konstruktor
	/**
	 * Vytvori novy DataOutputStream s little-endian razenymi bytes
	 * @param out				output stream, do ktereho se bude psat
	 */
	public DataOutputStreamLE(OutputStream out) {
		super(new DataOutputStream(out));
	}


	// Pretizeni metod
	/**
	 * Pretizeni implementace write() deklarovane ve FilterOutputStreamu
	 * @param b					pole bytes
	 * @param off				offset
	 * @param len				delka
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		// write() u filtru garantuje big-endian zapis, proto musime
		// zapisovat takto a pretizit metodu.
		this.out.write(b, off, len);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		((DataOutputStream) this.out).writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		((DataOutputStream) this.out).writeByte(v);
	}

	/**
	 * Zapise short do streamu s little-endian razenymi bytes.
	 * @param v					zapisovany char
	 * @throws IOException
	 */
	@Override
	public void writeShort(int v) throws IOException {
		out.write(0xFF & v);
		out.write(0xFF & (v >> 8));
	}

	/**
	 * Zapise char do streamu s little-endian razenymi bytes.
	 * @param v					zapisovany short
	 * @throws IOException
	 */
	@Override
	public void writeChar(int v) throws IOException {
		this.writeShort(v);
	}

	/**
	 * Zapise int do streamu s little-endian razenymi bytes.
	 * @param v					zapisovany int
	 * @throws IOException
	 */
	@Override
	public void writeInt(int v) throws IOException {
		out.write(0xFF & v);
		out.write(0xFF & (v >> 8));
		out.write(0xFF & (v >> 16));
		out.write(0xFF & (v >> 24));
	}

	/**
	 * Zapise long do streamu s little-endian razenymi bytes.
	 * @param v					zapisovany long
	 * @throws IOException
	 */
	@Override
	public void writeLong(long v) throws IOException {
		byte[] bytes = new byte[8];
		long vLE = Long.reverseBytes(v);
		// Precteme jednotlive bity a ulozime je v obracenem poradi
		for (int i = 7; i >= 0; i++) {
			bytes[i] = (byte) (vLE & 0xFFL);
			vLE >>= 8;
		}
		this.write(bytes, 0, bytes.length);
	}

	/**
	 * Zapise float do streamu s little-endian razenymi bytes.
	 * @param v					zapisovany float
	 * @throws IOException
	 */
	@Override
	public void writeFloat(float v) throws IOException {
		this.writeInt(Float.floatToIntBits(v));
	}

	/**
	 * Zapise double do streamu s little-endian razenymi bytes.
	 * @param v					zapisovany float
	 * @throws IOException
	 */
	@Override
	public void writeDouble(double v) throws IOException {
		this.writeLong(Double.doubleToLongBits(v));
	}

	/**
	 * Zapise retezec jako sekvenci bytes.
	 * @param s					zapisovany retezec
	 * @throws IOException		pokud nastane chyba zapisu
	 */
	@Override
	public void writeBytes(String s) throws IOException {
		((DataOutputStream) out).writeBytes(s);
	}

	/**
	 * Zapise retezec po jednom znaku do streamu s little-endian razenymi bytes.
	 * @param s					zapisovany retezec
	 * @throws IOException		pokud nastane chyba zapisu
	 */
	@Override
	public void writeChars(String s) throws IOException {
		for (int i = 0; i < s.length(); i++)
			this.writeChar(s.charAt(i));
	}

	@Override
	public void writeUTF(String s) throws IOException {
		// FIXME
		throw new UnsupportedOperationException();
	}
}
