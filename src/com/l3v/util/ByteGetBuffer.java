/**
 * 
 */
package com.l3v.util;

/**
 * @author Trí
 *
 */
public class ByteGetBuffer {
	private byte[] byteBuffer;
	private int position;
	private int oldPosition;

	/**
	 * @param byteBuffer
	 */
	public ByteGetBuffer(byte[] byteBuffer) {
		this.byteBuffer = byteBuffer;
		this.position = 0;
	}

	public void seek(int position) {
		this.position = position;
	}

	public void skip(int increase) {
		position += increase;
	}

	public int size() {
		return byteBuffer.length;
	}

	public int position() {
		return position;
	}

	public byte getByte() {
		oldPosition = position;
		position++;
		return byteBuffer[oldPosition];
	}

	public int getInt() {
		oldPosition = position;
		position += 4;
		return ByteUtilLittle.getInt(byteBuffer, oldPosition);
	}
}
