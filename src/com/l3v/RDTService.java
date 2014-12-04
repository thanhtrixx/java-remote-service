/**
 *
 */
package com.l3v;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.enums.SocketType;
import com.l3v.manager.Doorman;
import com.l3v.manager.RemoteManager;
import com.l3v.manager.Verifier;

/**
 * @author Trí
 *
 */
public class RDTService {

	private static final Logger log = LogManager.getLogger(RDTService.class);

	private static Doorman remotedDM;
	private static Doorman remotingDM;
	private static Doorman remotedScreenDM;
	private static Doorman remotingScreenDM;
	private static Doorman remotedMKDM;
	private static Doorman remotingMKDM;

	private static Verifier verifier;

	private static Thread remotedDMThread;
	private static Thread remotingDMThread;
	private static Thread remotedScreenDMThread;
	private static Thread remotingScreenDMThread;
	private static Thread remotedMKDMThread;
	private static Thread remotingMKDMThread;

	private static Thread verifierThread;

	public static boolean isRun;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.info("Start");

		initializeService();
		startService();
		waitStop();

		log.info("End");
	}

	/**
	 *
	 */
	private static void initializeService() {
		// Start doorman
		remotedDM = new Doorman(SocketType.RemotedCommand);
		remotingDM = new Doorman(SocketType.RemotingCommand);
		remotedScreenDM = new Doorman(SocketType.RemotedScreen);
		remotingScreenDM = new Doorman(SocketType.RemotingScreen);
		remotedMKDM = new Doorman(SocketType.RemotedMK);
		remotingMKDM = new Doorman(SocketType.RemotingMK);

		verifier = Verifier.getInstance();

		remotedDMThread = new Thread(remotedDM);
		remotingDMThread = new Thread(remotingDM);
		remotedScreenDMThread = new Thread(remotedScreenDM);
		remotingScreenDMThread = new Thread(remotingScreenDM);
		remotedMKDMThread = new Thread(remotedMKDM);
		remotingMKDMThread = new Thread(remotingMKDM);
		verifierThread = new Thread(verifier);

	}

	/**
	 *
	 */
	private static void startService() {
		isRun = true;

		remotedDMThread.start();
		remotingDMThread.start();
		remotedScreenDMThread.start();
		remotingScreenDMThread.start();
		remotedMKDMThread.start();
		remotingMKDMThread.start();
		verifierThread.start();
	}

	/**
	 *
	 */
	private static void stopService() {
		remotedDM.stop();
		remotingDM.stop();
		remotedScreenDM.stop();
		remotingScreenDM.stop();
		remotedMKDM.stop();
		remotingMKDM.stop();

		verifier.stop();

		waitStop();

		RemoteManager.resetMap();
	}

	/**
	 *
	 */
	private static void waitStop() {
		try {
			remotedDMThread.join();
			remotingDMThread.join();
			remotedScreenDMThread.join();
			remotingScreenDMThread.join();
			remotedMKDMThread.join();
			remotingMKDMThread.join();
			verifierThread.join();
		} catch (InterruptedException e) {
			log.error("InterruptedException when join", e);
		}
	}
}
