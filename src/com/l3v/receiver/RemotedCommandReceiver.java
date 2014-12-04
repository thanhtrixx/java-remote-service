package com.l3v.receiver;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.enums.RDTCommand;
import com.l3v.manager.RemoteManager;

/**
 * @author Trí
 *
 */
public class RemotedCommandReceiver extends Receiver {

	private static final Logger log = LogManager.getLogger(RemotedCommandReceiver.class);

	/**
	 * @param ips
	 * @param addressInfo
	 */
	public RemotedCommandReceiver(int id, InputStream ips, OutputStream ops, String addressInfo) {
		super(id, ips, ops, addressInfo);
	}

	@Override
	protected void processCommand(ByteBuffer byteBuffer, RDTCommand command) {
		log.debug("[{}] send command [{}]", id, command.toString());
		switch (command) {
		case Logout:
			processLogout();
			break;
		case EndRemote:
			processEndRemote(byteBuffer);
			break;
		case AcceptRemote:
			processAcceptRemote(byteBuffer);
			break;
		case SendData:
		default:
			log.error("Unknown command: {}", command);
			break;
		}

	}

	private void processAcceptRemote(ByteBuffer byteBuffer) {
		// Check size
		if (RDTCommand.AcceptRemote.getSize() != byteBuffer.capacity()) {
			// TODO: send error message
			log.warn("AcceptLogin from address: [{}], id: [{}] incorrect", addressInfo, id);
			return;
		}
		// Get remoting id
		int remotingId = byteBuffer.getInt();
		// Get remoted id
		int remotedId = byteBuffer.getInt();

		// Call manager
		RemoteManager.acceptRemote(remotingId, remotedId, byteBuffer);
	}
}
