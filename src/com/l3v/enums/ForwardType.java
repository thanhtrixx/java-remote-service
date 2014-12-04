package com.l3v.enums;

public enum ForwardType {
	None((byte) 0),
	Command((byte) 1),
	Screen((byte) 2),
	Mk((byte) 3);

	private final byte code;

	private ForwardType(byte code) {
		this.code = code;
	}

	public byte getCode() {
		return code;
	}
}
