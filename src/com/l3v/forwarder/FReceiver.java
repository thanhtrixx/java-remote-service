/**
 *
 */
package com.l3v.forwarder;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.common.Worker;
import com.l3v.config.AppConfig;
import com.l3v.enums.ForwardType;
import com.l3v.manager.RemoteManager;

/**
 * @author Trí
 *
 */
public class FReceiver implements Worker {

	private static final Logger log = LogManager.getLogger(FReceiver.class);
	private boolean isRun;
	private ForwardType type;

	private int id;
	private Socket socket;
	private TransferQueue<byte[]> queue;

	private byte[] receiveBytes = new byte[AppConfig.maxBufferSize];

	/**
	 * @param socket
	 */
	public FReceiver(int id, Socket socket, TransferQueue<byte[]> queue, ForwardType type) {
		this.id = id;
		this.socket = socket;
		this.queue = queue;
		this.type = type;
	}

	@Override
	public void run() {
		log.info("Receiver [{}]-[{}] started", id, type);

		isRun = true;

		InputStream ips = null;
		byte[] sendBytes = null;
		int receiveSize;
		try {
			ips = socket.getInputStream();
		} catch (IOException e) {
			log.error("Can't get input stream [{}]-[{}]. Stop receiver", id, type, e);
			isRun = false;
		}
		while (isRun) {
			try {
				// receive
				receiveSize = ips.read(receiveBytes);
				if (0 < receiveSize) {
					// create bytes and copy
					sendBytes = new byte[receiveSize];
					System.arraycopy(receiveBytes, 0, sendBytes, 0, receiveSize);

					// add to queue
					// check size to transfer
					if (AppConfig.maxQueueSize < queue.size()) {
						// try to transfer if timeout then stop receive
						if (!queue.tryTransfer(sendBytes, AppConfig.queueTimeOut, TimeUnit.MILLISECONDS)) {
							// stop receive
							log.warn("[{}]-[{}]. Transfer to sender timeout. Stop receive", id, type);
							break;
						}
					} else {
						// add normal
						queue.put(sendBytes);
					}
				} else {
					if (isRun) {
						log.error("[{}]-[{}], recieve size [{}] <= 0", id, type, receiveSize);
					}
					break;
				}

			} catch (InterruptedException e) {
				if (isRun) {
					log.error("Put to queue error [" + id + "]-[" + type + "]", e);
				}
			} catch (IOException e) {
				if (isRun) {
					log.error("Receive error [" + id + "]-[" + type + "]. Stop receive", e);
				}
				break;
			}

		}
		// Stop forwarder
		RemoteManager.stopForwarder(id, type);
		// Clean up
		queue.clear();
		queue = null;
		log.info("[{}]-[{}] stopped", id, type);
	}

	@Override
	public void stop() {
		isRun = false;
		log.debug("[{}]-[{}] stopping", id, type);
	}
}
