/**
 *
 */
package com.l3v.forwarder;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.common.Worker;
import com.l3v.config.AppConfig;
import com.l3v.entity.Remoter;
import com.l3v.enums.ForwardType;
import com.l3v.manager.RemoteManager;

/**
 * @author Trí
 *
 */
public class FSender implements Worker {

	private static final Logger log = LogManager.getLogger(FSender.class);
	private boolean isRun;
	private ForwardType type;

	private int id;
	private Socket socket;
	private TransferQueue<byte[]> queue;

	/**
	 * @param socket
	 */
	public FSender(int id, Socket socket, TransferQueue<byte[]> queue, ForwardType type) {
		this.id = id;
		this.socket = socket;
		this.queue = queue;
		this.type = type;
	}

	@Override
	public void run() {
		log.info("Sender [{}]-[{}] started", id, type);

		isRun = true;

		OutputStream ops = null;
		byte[] sendBytes = null;
		try {
			ops = socket.getOutputStream();
		} catch (IOException e) {
			log.error("Can't get OPS [" + id + "]-[" + type + "]. Stop sender", e);
			isRun = false;
		}
		while (isRun) {
			try {

				// get from queue
				sendBytes = queue.poll(AppConfig.queueTimeOut, TimeUnit.MILLISECONDS);
				if (null != sendBytes && 0 < sendBytes.length) {
					ops.write(sendBytes);
				} else {
					log.debug("[{}]-[{}]. Get bytes from queue is null or empty. Check forwarder status", id, type);
					// Check forwarder status
					Remoter rInfo = RemoteManager.getInfo(id);
					if (null == rInfo) {
						// Logout
						log.warn("[{}] Logouted", id);
						break;
					} else {
						Forwarder forwarder = null;
						switch (type) {
						case Mk:
							forwarder = rInfo.getMkForwarder();
							break;
						case Screen:
							forwarder = rInfo.getMkForwarder();
							break;
						case None:
						case Command:
						default:
							break;
						}

						if (null == forwarder) {
							log.warn("[{}]-[{}] Forwarder is null. Stop sender", id, type);
							break;
						} else {
							if (this != forwarder.getSender()) {
								log.warn("Current sender not this. Stop sender");
								break;
							}
						}
					}
				}

			} catch (InterruptedException e) {
				log.error("Exception when get from queue [" + id + "]-[" + type + "]", e);
				break;
			} catch (IOException e) {
				log.error("Send error [" + id + "]-[" + type + "]. Stop send", e);
				break;
			}

		}
		log.info("Sender [{}]-[{}] stopped", id, type);
		// Stop forwarder
		RemoteManager.stopForwarder(id, type);
		// Clean up
		queue.clear();
		queue = null;
	}

	@Override
	public void stop() {
		isRun = false;
		log.debug("Sender [{}]-[{}] stopping", id, type);
	}
}
