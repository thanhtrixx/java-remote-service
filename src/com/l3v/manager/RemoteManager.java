/**
 *
 */
package com.l3v.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.entity.Remoter;
import com.l3v.entity.VerifyInfo;
import com.l3v.enums.ForwardType;
import com.l3v.enums.RDTCommand;
import com.l3v.enums.RoleType;
import com.l3v.enums.SocketType;
import com.l3v.receiver.ControlReceiver;
import com.l3v.receiver.Receiver;
import com.l3v.receiver.RemotedCommandReceiver;
import com.l3v.receiver.RemotingCommandReceiver;
import com.l3v.util.ByteUtilLittle;

/**
 * @author Trí
 *
 */
public class RemoteManager {

	private static final Logger log = LogManager.getLogger(RemoteManager.class);
	private static Map<Integer, Remoter> map = new ConcurrentHashMap<>();

	public static void acceptLogIn(VerifyInfo info, Socket socket, InputStream ips, OutputStream ops) {
		switch (info.getSocketType()) {
		case RemotedCommand:
		case RemotingCommand:
		case Master:
			remoteCommandAcceptLogin(info, socket, ips, ops);
			break;
		case RemotedScreen:
		case RemotingScreen:
			remoteScreenAcceptLogin(info, socket, ops);
			break;
		case RemotedMK:
		case RemotingMK:
			remoteMKAcceptLogin(info, socket, ops);
			break;
		default:
			log.warn("Unknown socket type, [{}]-[{}]", info.getId(), info.getSocketType());
			break;
		}
		log.info("[{}]-[{}]", info.getId(), info.getSocketType());
	}

	/**
	 * @param id
	 */
	public static void remoteLogout(int id) {
		Remoter info = map.remove(id);
		if (null != info) {

			// End remote
			if (info.isAcceptRemote()) {
				endRemote(info);
			}

			// Control
			info.stopControlReceiver();
			info.closeControlSocket();

			log.info("[{}] logout", id);
		} else {
			log.warn("[{}] offline", id);
		}
	}

	/**
	 * @param id
	 */
	public static void remoteEndRemote(int id, ByteBuffer byteBuffer) {
		Remoter info = map.get(id);
		if (null != info) {

			Remoter pInfo = info.getPartner();
			if (null != pInfo) {
				// Forward to partner
				try {
					sendCommand(pInfo.getControlSocket(), byteBuffer);
				} catch (IOException e) {
					// TODO resend error
				}
			} else {
				log.warn("[{}]' partner offline", id);
			}

			// End remote
			endRemote(info);
		}
	}

	/**
	 * @param info
	 */
	private static void endRemote(Remoter info) {
		if (null != info) {
			Remoter partner = info.getPartner();
			if (null != partner) {
				// Stop screen & MK forwarder
				info.stopScreenForwarder();
				info.stopMkForwarder();
				// Close screen & MK socket
				info.closeMkSocket();
				info.closeScreenSocket();

				// Disconnect parter
				info.disconnectPartner();

				log.info("End remote S[{}]-D[{}]", info.getId(), partner.getId());
			} else {
				log.warn("[{}]'s partner offline", info.getId());
			}
		}
	}

	public static void connectRemoteInfo(Remoter remotingInfo, Remoter remotedInfo) {
		remotingInfo.connectPartner(remotedInfo);
		log.info("Remiting [{}] connect remoted [{}] successfuly", remotingInfo.getId(), remotedInfo.getId());
	}

	private static void remoteCommandAcceptLogin(VerifyInfo info, Socket socket, InputStream ips, OutputStream ops) {
		int id = info.getId();
		Remoter remoter = null;

		// Create receiver
		Receiver receiver = null;
		if (SocketType.RemotedCommand == info.getSocketType()) {
			receiver = new RemotedCommandReceiver(id, ips, ops, socket.getRemoteSocketAddress().toString());

			remoter = new Remoter(id, RoleType.Remoted, socket, createAndStartRDTReceiver(socket, receiver));
		} else {
			receiver = new RemotingCommandReceiver(id, ips, ops, socket.getRemoteSocketAddress().toString());

			if (SocketType.RemotingCommand == info.getSocketType()) {
				remoter = new Remoter(id, RoleType.Remoting, socket, createAndStartRDTReceiver(socket, receiver));
			} else {
				remoter = new Remoter(id, RoleType.Master, socket, createAndStartRDTReceiver(socket, receiver));
			}
		}

		// Put to remote info map
		map.put(id, remoter);
		// Send accept login
		sendAcceptLogin(info, ops);

		// else {
		// info.setRemotingId(id);
		// info.setRemotingSocket(socket);
		// info.setRemotingReceiver(rdtReceiver);
		// }
		// } else {
		// info.setRemotedId(id);
		// info.setRemotedSocket(socket);
		// info.setRemotedReceiver(rdtReceiver);
		// }
	}

	private static void remoteScreenAcceptLogin(VerifyInfo info, Socket socket, OutputStream ops) {
		// Set socket
		Remoter remoter = map.get(info.getId());

		remoter.setScreenSocket(socket);
		// Check partner screen socket connected then start forwarder
		if (null != remoter.getPartner() && null != remoter.getPartner().getScreenSocket()) {
			remoter.startScreenForwarder();
		}

		// Send accept login
		sendAcceptLogin(info, ops);
	}

	private static void remoteMKAcceptLogin(VerifyInfo info, Socket socket, OutputStream ops) {
		// Set socket
		Remoter remoter = map.get(info.getId());

		remoter.setMkSocket(socket);
		// Check partner screen socket connected then start forwarder
		if (null != remoter.getPartner() && null != remoter.getPartner().getMkSocket()) {
			remoter.startMKForwarder();
		}

		// Send accept login
		sendAcceptLogin(info, ops);
	}

	public static void stopForwarder(int id, ForwardType type) {
		Remoter remoter = map.get(id);
		if (null != remoter) {
			if (ForwardType.Mk == type) {
				remoter.stopMkForwarder();
			} else if (ForwardType.Screen == type) {
				remoter.stopScreenForwarder();
			} else {
				log.warn("Invaild ForwardType");
			}
		} else {
			log.warn("[{}] logouted", id);
		}
	}

	// private static void stopForwarder(Remoter info, ForwardType type) {
	// if (null != info) {
	// Forwarder forwarder = null;
	// if (ForwardType.Screen == type) {
	// forwarder = info.getScreenForwarder();
	// // Close and set socket to reconnect
	// closeSocket(info.getRemotingScreenSocket());
	// info.setRemotingScreenSocket(null);
	// closeSocket(info.getRemotedScreenSocket());
	// info.setRemotedScreenSocket(null);
	// } else if (ForwardType.Mk == type) {
	// forwarder = info.getMKForwarder();
	// // Set socket to reconnect
	// closeSocket(info.getRemotingMKSocket());
	// info.setRemotingMKSocket(null);
	// closeSocket(info.getRemotedMKSocket());
	// info.setRemotedMKSocket(null);
	// }
	//
	// if (null != forwarder) {
	// forwarder.stop();
	// log.info("Stop forwarder E[{}]-T[{}]-[{}] success", info.getRemotedId(),
	// info.getRemotingId(), type);
	// }
	// }
	// }

	/**
	 * @param socket
	 * @param receiver
	 * @return
	 */
	private static ControlReceiver createAndStartRDTReceiver(Socket socket, Receiver receiver) {
		ControlReceiver rdtReceiver = new ControlReceiver(socket, receiver);
		Thread rdtReceiverThread = new Thread(rdtReceiver);
		rdtReceiverThread.start();
		return rdtReceiver;
	}

	public static boolean checkOnline(Integer key) {
		log.trace("Check online [{}]", key);
		return map.containsKey(key);
	}

	public static Remoter getInfo(Integer key) {
		log.trace("Get [{}] remote info", key);
		return map.get(key);
	}

	public static void requestRemote(int remotingId, int remotedId, ByteBuffer byteBuffer) {
		// Check to send to remoted
		Remoter info = map.get(remotedId);
		if (null == info) {
			log.warn("Remited [{}] doesn't online when remoting [{}] request remote", remotedId, remotingId);
			return;
		}

		// Set request remote into remoted map info
		info.setRequestRemote(true);

		// Forward to remoted
		try {
			sendCommand(info.getControlSocket(), byteBuffer);
		} catch (IOException e) {
			// Logout remoted
			remoteLogout(remotedId);
			// TODO: send error
			// Resend error
			log.warn("Send request remote from remoting [{}] to remoted [{}] error", remotingId, remotedId);
			return;
		}

		log.debug("Remoting [{}] request remote remoted [{}] successfuly", remotingId, remotedId);
	}

	public static void acceptRemote(int remotingId, int remotedId, ByteBuffer byteBuffer) {
		// Check to send to remoted
		Remoter remotedInfo = map.get(remotedId);
		// Check online
		if (null == remotedInfo) {
			log.warn("Remited [{}] doesn't online when remoting [{}] request remote", remotedId, remotingId);
			// TODO: send error
			return;
		}
		// Check request remote
		if (!remotedInfo.isRequestRemote()) {
			log.warn("Remiting [{}] doesn't request remote remoted [{}]", remotingId, remotedId);
			// TODO: send error
			return;
		}
		// Set request remote into remoting map info
		Remoter remotingInfo = map.get(remotingId);
		if (null == remotingInfo) {
			log.warn("Remoted [{}] can't accept remote because remoting [{}] offline", remotingId, remotingId);
			// TODO: send error
			return;
		}

		// Merge remote info map
		connectRemoteInfo(remotingInfo, remotedInfo);
		// TODO: hung
		remotedInfo.startScreenForwarder();
		remotedInfo.startMKForwarder();
		// ----------------------------------------------

		// Send accept remote to remoting
		try {
			sendCommand(remotingInfo.getControlSocket(), byteBuffer);
		} catch (IOException e) {
			// Logout remoting
			remoteLogout(remotingId);
			// TODO: resend error
			log.warn("Send request remote from remoting [{}] to remoted [{}] error", remotingId, remotedId);
			return;
		}

		log.debug("Remoted [{}] accept remote remoting [{}] successfuly", remotedId, remotingId);
	}

	public static void getRemoterList(int id, OutputStream ops) {
		Collection<Remoter> remoterList = map.values();
		int packetSize = 4 + 1 + 4 + map.size() * 15;
		ByteBuffer byteBuffer = ByteBuffer.allocate(packetSize).order(ByteOrder.LITTLE_ENDIAN);
		// Add packet size
		byteBuffer.putInt(packetSize);
		// Add command
		byteBuffer.put(RDTCommand.GetRemoterList.getCode());
		// Add size
		byteBuffer.putInt(map.size());

		// Add detail
		for (Remoter remoter : remoterList) {
			remoter.convertInfoToByteBuffer(byteBuffer);
		}

		// Answer get remoter list
		try {
			ops.write(byteBuffer.array());
		} catch (IOException e) {
			// Logout if error
			log.error("[" + id + "] send error", e);
			remoteLogout(id);
		}
	}

	/**
	 * @param socket
	 * @param byteBuffer
	 * @throws java.io.IOException
	 */
	private static void sendCommand(Socket socket, ByteBuffer byteBuffer) throws IOException {
		if (null != socket) {
			// Get out stream
			OutputStream ops = socket.getOutputStream();

			ops.write(byteBuffer.array());
		}
	}

	private static void sendAcceptLogin(VerifyInfo info, OutputStream ops) {
		// TODO:hung
		int id = info.getId();
		Remoter rInfo = map.get(id);
		OutputStream rOps = null;
		try {
			rOps = rInfo.getControlSocket().getOutputStream();
		} catch (IOException e) {
			// Logout
			remoteLogout(id);
			return;
		}

		// Send accept login
		try {
			byte[] acceptBytes = new byte[RDTCommand.AcceptLogin.getSize()];
			ByteUtilLittle.putInt(acceptBytes, 0, RDTCommand.AcceptLogin.getSize());
			// Add command code
			acceptBytes[4] = RDTCommand.AcceptLogin.getCode();
			// Add port type
			acceptBytes[5] = info.getSocketType().getCode();
			// TODO:hung
			// ops.write(acceptBytes);
			rOps.write(acceptBytes);

			log.debug("[{}]-[{}] success", info.getId(), info.getSocketType());
		} catch (IOException e) {
			// Can't send, close socket
			remoteLogout(id);
			log.error("Send error [" + info.getId() + "]-[" + info.getSocketType() + "]", e);
		}
	}

	public static void resetMap() {
		for (Remoter rInfo : map.values()) {
			rInfo.stopControlReceiver();
			rInfo.stopMkForwarder();
			rInfo.stopScreenForwarder();
		}
		map.clear();
	}
}
