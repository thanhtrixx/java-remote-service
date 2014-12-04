/**
 *
 */
package com.l3v.entity;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.l3v.common.Worker;
import com.l3v.enums.ForwardType;
import com.l3v.enums.RoleType;
import com.l3v.forwarder.Forwarder;
import com.l3v.receiver.ControlReceiver;

/**
 * @author Trí
 *
 */
public class Remoter implements Cloneable {

	private static final Logger log = LogManager.getLogger(Remoter.class);

	private int id;
	private RoleType type;

	private boolean requestRemote = false;
	private boolean acceptRemote = false;

	private Socket controlSocket;
	private Socket screenSocket;
	private Socket mkSocket;

	private ControlReceiver controlReceiver;

	/**
	 * Forwarder
	 */
	private Forwarder screenForwarder;
	private Forwarder mkForwarder;

	private Remoter partner;

	/**
	 * @param id
	 * @param type
	 * @param controlSocket
	 */
	public Remoter(int id, RoleType type, Socket controlSocket, ControlReceiver controlReceiver) {
		this.id = id;
		this.type = type;
		this.controlSocket = controlSocket;
		this.controlReceiver = controlReceiver;
	}

	public void closeControlSocket() {
		closeSocket(controlSocket);
		controlSocket = null;

		log.debug("[{}]", id);
	}

	public void closeScreenSocket() {
		closeSocket(screenSocket);
		screenSocket = null;

		// Close partner's socket if exist
		if (null != partner) {
			closeSocket(partner.getScreenSocket());
			partner.screenSocket = null;
		}

		log.debug("[{}]", id);
	}

	public void closeMkSocket() {
		closeSocket(mkSocket);
		mkSocket = null;

		// Close partner's socket if exist
		if (null != partner) {
			closeSocket(partner.getMkSocket());
			partner.mkSocket = null;
		}

		log.debug("[{}]", id);
	}

	public void stopControlReceiver() {
		stopWorker(controlReceiver);
		controlReceiver = null;
	}

	public void startScreenForwarder() {
		// Determine source and destination
		Remoter source = RoleType.Remoting == type ? partner : this;
		Remoter dest = RoleType.Remoted == type ? partner : this;
		// Create forwarder

		Forwarder screenForwarder = new Forwarder(source.id, source.screenSocket, dest.id, dest.screenSocket,
				ForwardType.Screen);
		// Set to map
		setScreenForwarder(screenForwarder);
		// Start forwarder
		screenForwarder.run();
	}

	public void startMKForwarder() {
		// Determine source and destination
		Remoter source = RoleType.Remoted == type ? partner : this;
		Remoter dest = RoleType.Remoting == type ? partner : this;
		// Create forwarder

		Forwarder mkForwarder = new Forwarder(source.id, source.mkSocket, dest.id, dest.mkSocket, ForwardType.Mk);
		// Set forwarder
		setMkForwarder(mkForwarder);
		// Start forwarder
		mkForwarder.run();
	}

	public void stopScreenForwarder() {
		stopWorker(screenForwarder);
		setScreenForwarder(null);
		log.info("[{}] stopping", id);
	}

	public void stopMkForwarder() {
		stopWorker(mkForwarder);
		setMkForwarder(null);
		log.info("[{}] stopping", id);
	}

	public boolean connectPartner(Remoter partner) {
		if (null == partner) {
			return false;
		}

		partner.partner = this;
		this.partner = partner;

		return true;
	}

	public Remoter disconnectPartner() {
		if (null == this.partner) {
			return null;
		}

		Remoter partner = this.partner;
		// Disconnect at partner before
		partner.partner = null;
		this.partner = null;

		return partner;
	}

	private void closeSocket(Socket socket) {
		if (null != socket) {
			try {
				socket.close();
			} catch (IOException e) {
				log.error("Can't [" + id + "] socket. Detail: {}", e);
			}
		}
	}

	/**
	 * @param worker
	 */
	private void stopWorker(Worker worker) {
		if (null != worker) {
			worker.stop();
		}
	}

	/**
	 * @return the requestRemote
	 */
	public final boolean isRequestRemote() {
		return requestRemote;
	}

	/**
	 * @param requestRemote
	 *            the requestRemote to set
	 */
	public final void setRequestRemote(boolean requestRemote) {
		this.requestRemote = requestRemote;
	}

	/**
	 * @return the acceptRemote
	 */
	public final boolean isAcceptRemote() {
		return acceptRemote;
	}

	/**
	 * @param acceptRemote
	 *            the acceptRemote to set
	 */
	public final void setAcceptRemote(boolean acceptRemote) {
		this.acceptRemote = acceptRemote;
	}

	/**
	 * @return the controlSocket
	 */
	public final Socket getControlSocket() {
		return controlSocket;
	}

	/**
	 * @param controlSocket
	 *            the controlSocket to set
	 */
	public final void setControlSocket(Socket controlSocket) {
		this.controlSocket = controlSocket;
	}

	/**
	 * @return the screenSocket
	 */
	public final Socket getScreenSocket() {
		return screenSocket;
	}

	/**
	 * @param screenSocket
	 *            the screenSocket to set
	 */
	public final void setScreenSocket(Socket screenSocket) {
		this.screenSocket = screenSocket;
	}

	/**
	 * @return the mkSocket
	 */
	public final Socket getMkSocket() {
		return mkSocket;
	}

	/**
	 * @param mkSocket
	 *            the mkSocket to set
	 */
	public final void setMkSocket(Socket mkSocket) {
		this.mkSocket = mkSocket;
	}

	/**
	 * @return the controlReceiver
	 */
	public final ControlReceiver getControlReceiver() {
		return controlReceiver;
	}

	/**
	 * @param controlReceiver
	 *            the controlReceiver to set
	 */
	public final void setControlReceiver(ControlReceiver controlReceiver) {
		this.controlReceiver = controlReceiver;
	}

	/**
	 * @return the screenForwarder
	 */
	public final Forwarder getScreenForwarder() {
		return screenForwarder;
	}

	/**
	 * @param screenForwarder
	 *            the screenForwarder to set
	 */
	private void setScreenForwarder(Forwarder screenForwarder) {
		this.screenForwarder = screenForwarder;

		// Set partner's forwarder if exist
		if (partner != null) {
			partner.screenForwarder = screenForwarder;
		}
	}

	/**
	 * @return the mkForwarder
	 */
	public Forwarder getMkForwarder() {
		return mkForwarder;
	}

	/**
	 * @param mkForwarder
	 *            the mkForwarder to set
	 */
	private final void setMkForwarder(Forwarder mkForwarder) {
		this.mkForwarder = mkForwarder;

		// Set partner's forwarder if exist
		if (partner != null) {
			partner.mkForwarder = mkForwarder;
		}
	}

	/**
	 * @return the id
	 */
	public final int getId() {
		return id;
	}

	/**
	 * @return the type
	 */
	public final RoleType getType() {
		return type;
	}

	/**
	 * @return the partner
	 */
	public final Remoter getPartner() {
		return partner;
	}

	/**
	 * @param partner
	 *            the partner to set
	 */
	public final void setPartner(Remoter partner) {
		this.partner = partner;
	}

	public final void convertInfoToByteBuffer(ByteBuffer byteBuffer) {
		byteBuffer.putInt(id);
		byteBuffer.put((byte) (RoleType.Remoted == type ? 0 : RoleType.Remoting == type ? 1 : 2));
		byteBuffer.put((byte) (requestRemote ? 1 : 0));
		byteBuffer.put((byte) (acceptRemote ? 1 : 0));
		byteBuffer.put((byte) (null == screenSocket ? 0 : 1));
		byteBuffer.put((byte) (null == mkSocket ? 0 : 1));
		byteBuffer.put((byte) (null == screenForwarder ? 0 : 1));
		byteBuffer.put((byte) (null == mkForwarder ? 0 : 1));
		byteBuffer.putInt(null == partner ? 0 : partner.id);
	}
}
