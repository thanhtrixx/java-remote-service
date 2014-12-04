/**
 *
 */
package com.l3v.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.config.AppConfig;
import com.l3v.enums.RDTCommand;
import com.l3v.manager.RemoteManager;
import com.l3v.util.ByteUtilLittle;

/**
 * @author Trí
 *
 */
public abstract class Receiver {
	private static final Logger log = LogManager.getLogger(Receiver.class);
	protected int id;
	protected InputStream ips;
	protected OutputStream ops;
	protected String addressInfo;
	protected byte[] byteBuf = new byte[4];

	public Receiver(int id, InputStream ips, OutputStream ops, String addressInfo) {
		this.id = id;
		this.ips = ips;
		this.ops = ops;
		this.addressInfo = addressInfo;
	}

	public boolean receive() throws IOException {
		// Read full packet
		byte[] receiveBytes = null;
		receiveBytes = readFull();

		// Not process when exception of error
		if (null == receiveBytes) {
			log.warn("[{}] read error. May be remote close not send logout", id);
			// Remove in remote list
			RemoteManager.remoteLogout(id);
			// Stop receiver
			return false;
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(receiveBytes).order(ByteOrder.LITTLE_ENDIAN);
		// Skip total size
		byteBuffer.getInt();
		// Get command

		RDTCommand command = RDTCommand.parseFromCode(byteBuffer.get());

		// ------------------------------------------|
		// Process command |
		processCommand(byteBuffer, command); // |
		// ------------------------------------------|
		// Next step
		return true;
	}

	protected abstract void processCommand(ByteBuffer byteBuffer, RDTCommand command);

	public byte[] readFull() throws IOException {
		// Check remain byte
		int totalReadByte = 0;
		int readByteNum = 0;
		byte[] result = null;

		// Read packet size
		readByteNum = ips.read(byteBuf);

		// Check read size to end read
		if (0 >= readByteNum) {
			return null;
		}

		totalReadByte += readByteNum;
		// Get full packet size
		int packetSize = ByteUtilLittle.getInt(byteBuf, 0);
		if (totalReadByte == packetSize) {
			// Full packet to small. Don't need use byte buffer
			result = new byte[packetSize];
			System.arraycopy(byteBuf, 0, result, 0, packetSize);
			return result;
		} else if (0 >= packetSize) {
			log.warn("[{}] packet size [{}] <= 0", id, packetSize);
			return null;
		} else if (AppConfig.maxPacketSize < packetSize) {
			log.warn("[{}] packet size [{}] > [{}]", id, packetSize, AppConfig.maxPacketSize);
			return null;
		}

		// Case read size < packet size
		// ByteBuffer bb = ByteBuffer.allocate(packetSize);
		result = new byte[packetSize];
		// Copy read byte
		log.debug("[{}] totalReadByte: [{}], byteBuf [{}], packetSize [{}]", id, totalReadByte, byteBuf.length,
				packetSize);
		System.arraycopy(byteBuf, 0, result, 0, totalReadByte);

		do {
			readByteNum = ips.read(result, totalReadByte, packetSize - totalReadByte);
			if (0 > readByteNum) {
				break;
			}
			totalReadByte += readByteNum;
			// Check packet size
			if (packetSize == totalReadByte) {
				return result;
			}
		} while (true);

		if (packetSize != totalReadByte) {
			log.warn("[{}] packet size in header incorrect, packetSize [{}], totalReadByte [{}]", id, packetSize,
					totalReadByte);
			return null;
		}

		return result;
	}

	protected void processLogout() {
		RemoteManager.remoteLogout(id);
		log.debug("[{}]", id);
	}

	protected void processEndRemote(ByteBuffer byteBuffer) {
		RemoteManager.remoteEndRemote(id, byteBuffer);
		log.debug("[{}]", id);
	}
}
