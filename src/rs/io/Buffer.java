package rs.io;

import net.burtleburtle.bob.rand.IsaacRandom;
import rs.util.CacheableNode;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A {@link Buffer} encapsulates a finite amount of data and provides methods for reading and writing to that data.
 * The <code>put()</code> and <code>get()</code> methods describe the type of data they use by attributes in their
 * suffix. The naming convention is <code>put[type][U:unsigned]</code>.
 * <p>
 * <b>Types:</b><br/>
 * # — the number of bytes<br/>
 * Op — 1 byte which is modified by the {@link #isaac} number generator.<br/>
 * Smart — 1 or 2 byte value [-16384...16383]<br/>
 * USmart — 1 or 2 byte value [0...32768]<br/>
 * String — {@link StandardCharsets#ISO_8859_1} encoding delimited by a newline character (\n)<br/>
 * <p>
 */
public final class Buffer extends CacheableNode {

	private static final int[] BITMASK;

	public byte[] data;
	public int position;
	public int bitPosition;
	public IsaacRandom isaac;

	public Buffer(int size) {
		this(new byte[size]);
	}

	public Buffer(byte[] src) {
		data = src;
		position = 0;
	}

	public void putOp(int opcode) {
		data[position++] = (byte) (opcode + isaac.nextInt());
	}

	public void put1(int i) {
		data[position++] = (byte) i;
	}

	public void put2(int i) {
		data[position++] = (byte) (i >> 8);
		data[position++] = (byte) i;
	}

	public void put4(int i) {
		data[position++] = (byte) (i >> 24);
		data[position++] = (byte) (i >> 16);
		data[position++] = (byte) (i >> 8);
		data[position++] = (byte) i;
	}

	public void put8(long l) {
		data[position++] = (byte) (int) (l >> 56);
		data[position++] = (byte) (int) (l >> 48);
		data[position++] = (byte) (int) (l >> 40);
		data[position++] = (byte) (int) (l >> 32);
		data[position++] = (byte) (int) (l >> 24);
		data[position++] = (byte) (int) (l >> 16);
		data[position++] = (byte) (int) (l >> 8);
		data[position++] = (byte) (int) l;
	}

	public void put(String s) {
		System.arraycopy(s.getBytes(), 0, data, position, s.length());
		position += s.length();
		data[position++] = (byte) 10;
	}

	public void put(byte[] src, int off, int len) {
		for (int i = off; i < off + len; i++) {
			data[position++] = src[i];
		}
	}

	public void putSize1(int length) {
		data[position - length - 1] = (byte) length;
	}

	public int get1U() {
		return data[position++] & 0xff;
	}

	public byte get1() {
		return data[position++];
	}

	public int get2U() {
		position += 2;
		return (((data[position - 2] & 0xff) << 8) + (data[position - 1] & 0xff));
	}

	public int get2() {
		position += 2;
		int i = (((data[position - 2] & 0xff) << 8) + (data[position - 1] & 0xff));
		if (i > 0x7fff) {
			i -= 0x10000;
		}
		return i;
	}

	public int get3() {
		position += 3;
		return (((data[position - 3] & 0xff) << 16) + ((data[position - 2] & 0xff) << 8) + (data[position - 1] & 0xff));
	}

	public int get4() {
		position += 4;
		return (((data[position - 4] & 0xff) << 24) + ((data[position - 3] & 0xff) << 16) + ((data[position - 2] & 0xff) << 8) + (data[position - 1] & 0xff));
	}

	public long get8() {
		long a = (long) get4() & 0xffffffffL;
		long b = (long) get4() & 0xffffffffL;
		return (a << 32) + b;
	}

	public String getString() {
		int startPosition = position;
		while (data[position++] != 10) {
			/* empty */
		}
		return new String(data, startPosition, position - startPosition - 1);
	}

	public byte[] getStringRaw() {
		int startPosition = position;
		while (data[position++] != 10) {
			/* empty */
		}
		byte[] bytes = new byte[position - startPosition - 1];
		for (int i = startPosition; i < position - 1; i++) {
			bytes[i - startPosition] = data[i];
		}
		return bytes;
	}

	public void get(byte[] dst, int off, int len) {
		for (int i = off; i < off + len; i++) {
			dst[i] = data[position++];
		}
	}

	public void accessBits() {
		bitPosition = position * 8;
	}

	public int getBits(int bits) {
		int bytePos = bitPosition >> 3;
		int msb = 8 - (bitPosition & 0x7);
		int i = 0;

		bitPosition += bits;

		for (/**/; bits > msb; msb = 8) {
			i += ((data[bytePos++] & BITMASK[msb]) << (bits - msb));
			bits -= msb;
		}

		if (bits == msb) {
			i += data[bytePos] & BITMASK[msb];
		} else {
			i += ((data[bytePos] >> (msb - bits)) & BITMASK[bits]);
		}

		return i;
	}

	public void accessBytes() {
		position = (bitPosition + 7) / 8;
	}

	/**
	 * Gets a 1 or 2 byte varint which has the range [-16384...16383].
	 *
	 * @return the value.
	 */
	public int getSmart() {
		int i = data[position] & 0xff;
		if (i < 0x80) {
			return get1U() - 0x40;
		}
		return get2U() - 0xC000;
	}

	/**
	 * Gets a 1 or 2 byte varint which has the range [0...32768].
	 *
	 * @return the value.
	 */
	public int getSmartU() {
		int i = data[position] & 0xff;
		if (i < 0x80) {
			return get1U();
		}
		return get2U() - 0x8000;
	}

	public void encode(BigInteger exponent, BigInteger modulus) {
		byte[] encoded = new BigInteger(Arrays.copyOfRange(data, 0, position)).modPow(exponent, modulus).toByteArray();
		position = 0;
		put1(encoded.length);
		put(encoded, 0, encoded.length);
	}

	static {
		BITMASK = new int[33];
		for (int i = 0; i < 32; i++) {
			BITMASK[i] = (1 << i) - 1;
		}
		BITMASK[32] = -1;
	}
}
