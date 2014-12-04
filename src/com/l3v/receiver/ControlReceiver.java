/**
 *
 */
package com.l3v.receiver;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.common.Worker;
import com.l3v.manager.RemoteManager;

/**
 * @author Trí
 *
 */
public class ControlReceiver implements Worker {

	private static final Logger log = LogManager.getLogger(ControlReceiver.class);
	private Socket socket;
	private Receiver receiver;
	private volatile boolean isRun;

	/**
	 * @param socket
	 */
	public ControlReceiver(Socket socket, Receiver receiver) {
		this.socket = socket;
		this.receiver = receiver;
	}

	@Override
	public void run() {
		log.info("[{}]-[{}] started", receiver.id, socket.getRemoteSocketAddress());
		isRun = true;

		try {
			while (receiver.receive() && isRun) {
				// not do any thing in body
			}
		} catch (IOException e) {
			// If run then log
			if (isRun) {
				log.error("Read error [" + receiver.id + "]-[" + receiver.addressInfo + "]", e);
			}
			RemoteManager.remoteLogout(receiver.id);
		}
		log.info("[{}]-[{}] stopped", receiver.id, socket.getRemoteSocketAddress());
	}

	@Override
	public void stop() {
		isRun = false;
		try {
			socket.close();
		} catch (IOException e) {
			log.error("Close socket error [" + receiver.id + "]-[" + socket.getRemoteSocketAddress() + "]", e);
		}
		log.debug("[{}]-[{}] stopping", receiver.id, socket.getRemoteSocketAddress());
	}

	/**
	 * @return the receiver
	 */
	public final Receiver getReceiver() {
		return receiver;
	}

}
