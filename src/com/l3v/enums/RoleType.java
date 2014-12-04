/**
 *
 */
package com.l3v.enums;

/**
 * @author Trí
 *
 */
public enum RoleType {
	None((byte) 0),
	Remoting((byte) 1),
	Remoted((byte) 2),
	Master((byte) 3);

	private final byte code;

	private RoleType(byte code) {
		this.code = code;
	}

	public byte getCode() {
		return code;
	}
}
