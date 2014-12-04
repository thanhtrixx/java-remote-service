/**
 * 
 */
package com.l3v.entity;

/**
 * @author Trí
 *
 */
public class RWBuffer_BK {

	private int noId;
	private byte[] byteBuffer;
	private int size;
	private boolean isFree;

	/**
	 * 
	 */
	RWBuffer_BK(int noId, int bufferSize) {
		this.noId = noId;
		this.byteBuffer = new byte[bufferSize];
		this.isFree = true;
		this.size = 0;
	}

	public boolean isFree() {
		return isFree;
	}

	void setFree(boolean isFree) {
		this.isFree = isFree;
	}

	public int getNoId() {
		return noId;
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
}
