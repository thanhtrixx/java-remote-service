package com.l3v.entity;

import java.net.Socket;

import com.l3v.enums.SocketType;

public class VerifyInfo {
	private int id;
	private Socket socket;
	private SocketType socketType;

	/**
	 * @param socket
	 * @param socketType
	 */
	public VerifyInfo(Socket socket, SocketType socketType) {
		this.socket = socket;
		this.socketType = socketType;
	}

	/**
	 * @return the id
	 */
	public final int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public final void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the socket
	 */
	public final Socket getSocket() {
		return socket;
	}

	/**
	 * @param socket
	 *            the socket to set
	 */
	public final void setSocket(Socket socket) {
		this.socket = socket;
	}

	/**
	 * @return the socketType
	 */
	public final SocketType getSocketType() {
		return socketType;
	}

	/**
	 * @param socketType
	 *            the socketType to set
	 */
	public final void setSocketType(SocketType socketType) {
		this.socketType = socketType;
	}

}