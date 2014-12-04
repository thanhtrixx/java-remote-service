package com.l3v.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.enums.RDTCommand;
import com.l3v.manager.RemoteManager;

/**
 * @author Trí
 *
 */
public class RemotingCommandReceiver extends Receiver {

	private static final Logger log = LogManager.getLogger(RemotingCommandReceiver.class);

	/**
	 * @param ips
	 * @param addressInfo
	 */
	public RemotingCommandReceiver(int id, InputStream ips, OutputStream ops, String addressInfo) {
		super(id, ips, ops, addressInfo);
	}

	/**
	 * @param byteBuffer
	 * @param command
	 */
	@Override
	protected void processCommand(ByteBuffer byteBuffer, RDTCommand command) {
		log.debug("[{}] send command [{}]", id, command.toString());
		switch (command) {
		case CheckOnline:
			processCheckOnline(byteBuffer);
			break;
		case Logout:
			processLogout();
			break;
		case EndRemote:
			processEndRemote(byteBuffer);
			break;
		case RequestRemote:
			processRequestRemote(byteBuffer);
			break;
		// Master command
		case GetRemoterList:
			processGetRemoterList();
			break;
		// Don't support command
		case AcceptLogin:
		case AcceptRemote:
		case DenyRemote:
		case Error:
		case Login:
		case None:
		case SendData:
			log.error("Don't support command: [{}] from: [{}]", command, id);
			break;
		default:
			log.error("Unknown command: [{}]", command);
			break;
		}
	}

	private void processGetRemoterList() {
		RemoteManager.getRemoterList(id, ops);
	}

	private void processCheckOnline(ByteBuffer byteBuffer) {
		// Get list check online
		List<Integer> onelineList = new ArrayList<>();
		int tmpInt;
		StringBuilder checkListSB = null, onlineListSB = null;
		// Log list of device need check online
		if (log.isDebugEnabled()) {
			checkListSB = new StringBuilder();
			onlineListSB = new StringBuilder();
		}
		// Get list size
		int listSize = byteBuffer.getInt();
		while (byteBuffer.capacity() >= (byteBuffer.position() + 4) && listSize > 0) {
			tmpInt = byteBuffer.getInt();
			if (log.isDebugEnabled()) {
				checkListSB.append(tmpInt).append(", ");
			}
			if (RemoteManager.checkOnline(tmpInt)) {
				onelineList.add(tmpInt);
				if (log.isDebugEnabled()) {
					onlineListSB.append(tmpInt).append(", ");
				}
			}
			listSize--;
		}
		if (log.isDebugEnabled()) {
			log.debug("[{}] check online list: [{}]", id,
					checkListSB.length() == 0 ? "EMPTY" : checkListSB.substring(0, checkListSB.length() - 2));
			log.debug("List partner online: [{}] of [{}]",
					onlineListSB.length() == 0 ? "EMPTY" : onlineListSB.substring(0, onlineListSB.length() - 2), id);
		}
		// Caculate size of answer check online command: 4 byte total size , 1
		// byte command, online list
		int totalSize = 4 + 1 + 4 + onelineList.size() * 4;
		// create answer packet
		ByteBuffer answerByteBuffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN);
		// Set total size and command
		answerByteBuffer.putInt(totalSize);
		answerByteBuffer.put(RDTCommand.CheckOnline.getCode());
		answerByteBuffer.putInt(onelineList.size());
		for (int id : onelineList) {
			answerByteBuffer.putInt(id);
		}

		// Direct send
		try {
			ops.write(answerByteBuffer.array());
		} catch (IOException e) {
			RemoteManager.remoteLogout(id);
			log.error("Can't answer check online [" + id + "]", e);
		}
	}

	private void processRequestRemote(ByteBuffer byteBuffer) {
		// Check size
		if (RDTCommand.RequestRemote.getSize() != byteBuffer.capacity()) {
			// TODO: send error message
			log.warn("RequestRemote from address: {}, id: {} incorrect. Send size: {}", addressInfo, id,
					byteBuffer.capacity());
			return;
		}
		// Get remoted id
		int remotedId = byteBuffer.getInt();
		// Get remoting id
		int remotingId = byteBuffer.getInt();

		// Call manager
		RemoteManager.requestRemote(remotingId, remotedId, byteBuffer);
	}
}
