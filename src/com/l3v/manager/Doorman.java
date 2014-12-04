/**
 *
 */
package com.l3v.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.config.AppConfig;
import com.l3v.entity.VerifyInfo;
import com.l3v.enums.SocketType;

/**
 * @author Trí
 *
 */
public class Doorman implements Runnable {

	private static final Logger log = LogManager.getLogger(Doorman.class);
	private int port;
	private SocketType socketEnum;
	private boolean isRun;

	private ServerSocket socketServer = null;
	private Socket socket;

	public Doorman(SocketType socketEnum) {
		this.socketEnum = socketEnum;
		this.port = getPort(socketEnum);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		// set start flag
		isRun = true;

		try {
			socketServer = new ServerSocket(port);
			// socketServer.setReceiveBufferSize(AppConfig.maxBufferSize);
		} catch (IOException e) {
			log.error("Binding remotedSocketServer error", e);
		}

		Verifier verifier = Verifier.getInstance();

		// loop to wait new connect
		// Socket socket;
		while (isRun) {
			try {
				socket = socketServer.accept();
				log.info("Accepted [{}], server port [{}]", socket.getRemoteSocketAddress(), port);

				verifier.addToVerify(new VerifyInfo(socket, socketEnum));
			} catch (IOException e) {
				// If running then log error
				if (isRun) {
					log.warn("ServerSocket accept error", e);
				}
			}
		}
		// Close socket
		try {
			if (null != socketServer) {
				socketServer.close();
			}
		} catch (IOException e) {
			log.warn("Can't close socket server", e);
		}
		log.debug("Doorman [{}] stoped", port);
	}

	private int getPort(SocketType socketEnum) {
		switch (socketEnum) {
		case RemotedCommand:
			return AppConfig.remotedPort;
		case RemotingCommand:
			return AppConfig.remotingPort;
		case RemotedMK:
			return AppConfig.remotedMKPort;
		case RemotingMK:
			return AppConfig.remotingMKPort;
		case RemotedScreen:
			return AppConfig.remotedScreenPort;
		case RemotingScreen:
			return AppConfig.remotingScreenPort;
		}
		return 0;
	}

	public void stop() {
		log.debug("Doorman with port: {} stopping", port);

		isRun = false;
		try {
			socketServer.close();
		} catch (IOException e) {
			log.error("Can't close socket service", e);
		}
	}
}
