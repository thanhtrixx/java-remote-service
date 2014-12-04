/**
 *
 */
package com.l3v.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.config.AppConfig;
import com.l3v.entity.Remoter;
import com.l3v.entity.VerifyInfo;
import com.l3v.enums.RDTCommand;
import com.l3v.enums.SocketType;
import com.l3v.util.ByteUtilLittle;

/**
 * @author Trí
 *
 */
public class Verifier implements Runnable {

	private static final Logger log = LogManager.getLogger(Verifier.class);
	private TransferQueue<VerifyInfo> queue = new LinkedTransferQueue<>();
	// private byte[] ACCEPTLOGIN = { RDTcpCommand.AcceptLogin.getCode() };
	private byte[] firstByte = new byte[AppConfig.maxBufferSize];
	private int id;
	private int readByteNum;
	private boolean isRun;

	// Singleton
	private Verifier() {
	}

	public static Verifier getInstance() {
		return VerifierLazyHolder.INSTANCE;
	}

	@Override
	public void run() {
		isRun = true;
		while (isRun) {
			VerifyInfo verifyInfo = null;
			Socket socket = null;
			// Use poll() if don't have any---> wait until time out.
			try {
				verifyInfo = queue.poll(AppConfig.queueTimeOut, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error("InterruptedException when take socket from queue. Detail: {}", e);
			}

			if (null == verifyInfo) {
				log.trace("Get verifyInfo from queue is null. Continue");
				continue;
			}
			socket = verifyInfo.getSocket();
			// Get input stream & output stream
			InputStream ips = null;
			OutputStream ops = null;
			try {
				ips = socket.getInputStream();
				ops = socket.getOutputStream();
			} catch (IOException e) {
				log.error("Can't get IPS & OPS from A[" + socket.getRemoteSocketAddress() + "]. Detail: {}", e);
				continue;
			}
			if (verifyLogin(ips, verifyInfo.getSocketType())) {
				// Set Id to verify info
				verifyInfo.setId(id);

				// Check id to change to master
				if (100 > id) {
					verifyInfo.setSocketType(SocketType.Master);
				}

				// Call remote manager to
				RemoteManager.acceptLogIn(verifyInfo, socket, ips, ops);
			} else {
				log.info("Deny login [{}]-[{}]", verifyInfo.getSocketType(), socket.getRemoteSocketAddress());
			}
		}
		// Stop thread. Clear queue
		queue.clear();
		queue = null;
		log.debug("Verifier stoped. Clear queue");
	}

	// private boolean verifyLogin(VerifyInfo info) {
	private boolean verifyLogin(InputStream ips, SocketType socketType) {
		// Read first request
		int totalReadByte = 0;
		try {
			do {
				readByteNum = ips.read(firstByte, totalReadByte, firstByte.length - totalReadByte);
				totalReadByte += readByteNum;
			} while (0 > readByteNum);
		} catch (IOException e) {
			log.error("IOException. Detail: {}", e);
			return false;
		}
		// Check login
		if (RDTCommand.Login.getSize() == totalReadByte && RDTCommand.Login.getCode() == firstByte[4]) {
			// Get Id with order little-endian
			id = ByteUtilLittle.getInt(firstByte, 5);
			// Check logged
			Remoter info = RemoteManager.getInfo(id);

			switch (socketType) {
			case RemotedCommand:
			case RemotingCommand:
				if (null != info) {
					log.warn("Remote [{}]-[{}] logged", socketType, id);
					return false;
				}
				return true;
			case RemotedMK:
			case RemotedScreen:
			case RemotingMK:
			case RemotingScreen:
				// TODO hung
				// if (!info.isAcceptRemote()) {
				// log.warn("Deny connect [{}]-[{}]", socketType, id);
				// return false;
				// }
				return true;
			default:
				return false;
			}
		}
		// Value is indicate not allow login request
		return false;
	}

	public void addToVerify(VerifyInfo verifyInfo) {
		try {
			queue.put(verifyInfo);
		} catch (InterruptedException e) {
			log.error("Can't add into queue to verify", e);
		}
		log.debug("[{}]-[{}]", verifyInfo.getSocketType(), verifyInfo.getSocket().getRemoteSocketAddress());
	}

	public void stop() {
		isRun = false;
		log.debug("Verifier stopping");
	}

	private static class VerifierLazyHolder {
		private static final Verifier INSTANCE = new Verifier();
	}
}
