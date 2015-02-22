package com.bitmapview.util;

import java.io.*;

/**
 * Trida implementujici DataInputStream akorat s tim ze nacitani veskerych
 * datovych typu neprobiha s predpokladem razeni bytes v big-endian (network)
 * poradi, ale little-endian, kdy nejmene dulezity byt ma nejmensi adresu.
 * @see java.io.DataInputStream
 */
public class DataInputStreamLE
	extends FilterInputStream
	implements DataInput {


	// Konstruktor
	/**
	 * Vytvori novy DataInputStream s little-endian razenymi bytes
	 * @param in				input stream, ze ktereho se bude cist
	 */
	public DataInputStreamLE(InputStream in) {
		super(in);
	}


	// Pretizeni metod
	@Override
	public void readFully(byte[] b) throws IOException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		// FIXME
		throw new UnsupportedOperationException();
	}

	/**
	 * Preskoci n bytes ze streamu.
	 * @param n					pocet bytes k preskoceni
	 * @return					pocet skutecne preskocenych bytes
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public int skipBytes(int n) throws IOException {
		return (int) in.skip(n);
	}

	/**
	 * Precte boolean ze streamu s little-endian razenymi bytes (tj. precte
	 * jeden byte a podle toho jestli je nebo neni 0 vrati true/false.)
	 * @return					precteny boolean
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public boolean readBoolean() throws IOException {
		return (this.readUnsignedByte() != 0);
	}

	/**
	 * Precte byte ze streamu s little-endian razenymi bytes (tj. precte
	 * prave jeden byte.
	 * @return					precteny byte
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public byte readByte() throws IOException {
		return (byte) this.readUnsignedByte();
	}

	/**
	 * Precte unsigned byte ze streamu s little-endian razenymi bytes.
	 * @return					precteny unsigned byte
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public int readUnsignedByte() throws IOException {
		return in.read();
	}

	/**
	 * Precte short ze streamu s little-endian razenymi bytes.
	 * @return					precteny short
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public short readShort() throws IOException {
		return (short) this.readUnsignedShort();
	}

	/**
	 * Precte unsigned short ze streamu s little-endian razenymi bytes.
	 * @return					precteny unsigned short
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public int readUnsignedShort() throws IOException {
		// Nacteme dva bytes (short)
		byte b1 = (byte) in.read();
		byte b2 = (byte) in.read();

		// Vratime unsigned short poskladany little-endian (nejmin dulezity 2.
		// byte ma nejnizsi adresu)
		return (b2 & 0xFF) << 8
				| (b1 & 0xFF);
	}

	/**
	 * Precte char ze streamu s little-endian razenymi bytes.
	 * @return					precteny char
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public char readChar() throws IOException {
		return (char) this.readUnsignedShort();
	}

	/**
	 * Precte int ze streamu s little-endian razenymi bytes.
	 * @return					precteny int
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public int readInt() throws IOException {
		// Nacteme ctyri bytes (int)
		byte b1 = (byte) in.read();
		byte b2 = (byte) in.read();
		byte b3 = (byte) in.read();
		byte b4 = (byte) in.read();

		// Vratime int poskladany little-endian (nejmin dulezity 4. byte ma
		// nejnizsi adresu)
		return ((b1 & 0xFF) << 24
				| (b2 & 0xFF) << 16
				| (b3 & 0xFF) << 8
				| (b4 & 0xFF));
	}

	/**
	 * Precte long ze streamu s little-endian razenymi bytes.
	 * @return					precteny long
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public long readLong() throws IOException {
		// Nacteme osm bytes (long)
		byte b1 = (byte) in.read();
		byte b2 = (byte) in.read();
		byte b3 = (byte) in.read();
		byte b4 = (byte) in.read();
		byte b5 = (byte) in.read();
		byte b6 = (byte) in.read();
		byte b7 = (byte) in.read();
		byte b8 = (byte) in.read();

		// Vratime long poskladany little-endian (nejmin dulezity 8. byte ma
		// nejnizsi adresu)
		return ((b1 & 0xFFL) << 56
				| (b2 & 0xFFL) << 48
				| (b3 & 0xFFL) << 40
				| (b4 & 0xFFL) << 32
				| (b5 & 0xFFL) << 24
				| (b6 & 0xFFL) << 16
				| (b7 & 0xFFL) << 8
				| (b8 & 0xFFL));
	}

	/**
	 * Precte float ze streamu s little-endian razenymi bytes.
	 * @return					precteny float
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(this.readInt());
	}

	/**
	 * Precte double ze streamu s little-endian razenymi bytes.
	 * @return					precteny double
	 * @throws IOException		pokud nastane chyba cteni
	 */
	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(this.readLong());
	}

	@Override
	public String readLine() throws IOException {
		// FIXME (toto ani nejde naimplementovat, protoze nejde o string buffer)
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		// FIXME
		throw new UnsupportedOperationException();
	}

}
