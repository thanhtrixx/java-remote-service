/**
 * 
 */
package com.l3v.entity;

/**
 * @author Trí
 *
 */
public class BufferInfo {

	private byte[] byteBuffer;
	private int size;
	private int position;

	/**
	 * 
	 */
	public BufferInfo(byte[] byteBuffer, int size) {
		this.byteBuffer = byteBuffer;
		this.size = size;
	}

	public byte[] getByteBuffer() {
		return byteBuffer;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the position
	 */
	public final int getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public final void setPosition(int position) {
		this.position = position;
	}

}
