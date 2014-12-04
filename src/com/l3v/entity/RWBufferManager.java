/**
 * 
 */
package com.l3v.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Trí
 *
 */
public class RWBufferManager {

	private static final Logger log = LogManager.getLogger(RWBufferManager.class);
	private static RWBuffer_BK[] rwBuffers;

	public synchronized void Initialize(int size, int bufferSize) {
		log.debug("Initialize with size: {}, bufferSize: {}", size, bufferSize);
		rwBuffers = new RWBuffer_BK[size];
		for (int i = 0; i < size; i++) {
			rwBuffers[i] = new RWBuffer_BK(i, bufferSize);
		}
	}

	private RWBufferManager(int size, int bufferSize) {
	}

	public static synchronized RWBuffer_BK getFreeBuffer() {
		for (RWBuffer_BK rwBuffer : rwBuffers) {
			if (rwBuffer.isFree()) {
				log.debug("Get buffer noId: {}", rwBuffer.getNoId());
				rwBuffer.setFree(false);
				return rwBuffer;
			}
		}
		log.warn("Don't have any buffer free");
		return null;
	}

	public static synchronized void releaseBuffer(RWBuffer_BK rwBuffer) {
		log.warn("Release buffer with noId: {}", rwBuffer.getNoId());
		rwBuffer.setFree(true);
		rwBuffer.setSize(0);
	}
}
