package com.l3v.forwarder;

import java.net.Socket;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.common.Worker;
import com.l3v.enums.ForwardType;

public class Forwarder implements Worker {

	private static final Logger log = LogManager.getLogger(Forwarder.class);
	private ForwardType type;
	private TransferQueue<byte[]> queue;

	private int sourceId;
	private Socket sourceSocket;
	private Worker receiver;

	private int destId;
	private Socket destSocket;
	private Worker sender;

	public Forwarder(int sourceId, Socket sourceSocket, int destId, Socket destSocket, ForwardType type) {
		this.sourceId = sourceId;
		this.sourceSocket = sourceSocket;

		this.destId = destId;
		this.destSocket = destSocket;

		this.type = type;
	}

	public void run() {
		// Check before start
		if (null == queue) {
			queue = new LinkedTransferQueue<byte[]>();

			// Create receive thread
			receiver = new FReceiver(sourceId, sourceSocket, queue, type);
			Thread receiveThread = new Thread(receiver);
			receiveThread.start();

			// Create send thread
			sender = new FSender(destId, destSocket, queue, type);
			Thread sendThread = new Thread(sender);
			sendThread.start();

			log.info("Forwarder S[{}]-D[{}]-[{}] started", sourceId, destId, type);
		}
	}

	public Worker getReceiver() {
		return receiver;
	}

	public Worker getSender() {
		return sender;
	}

	@Override
	public void stop() {
		// Stop sender
		sender.stop();
		// Stop receiver
		receiver.stop();
		// release send queue
		queue = null;
		log.info("Forwarder S[{}]-D[{}]-[{}] stopped", sourceId, destId, type);
	}
}
