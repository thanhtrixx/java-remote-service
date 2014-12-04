/**
 *
 */
package com.l3v.enums;

/**
 * @author Trí
 *
 */
public enum SocketType {
	RemotedCommand((byte) 1),
	RemotingCommand((byte) 1),
	RemotedScreen((byte) 2),
	RemotingScreen((byte) 2),
	RemotedMK((byte) 3),
	RemotingMK((byte) 3),
	Master((byte) 4);

	private final byte code;

	private SocketType(byte code) {
		this.code = code;
	}

	public byte getCode() {
		return code;
	}
}
