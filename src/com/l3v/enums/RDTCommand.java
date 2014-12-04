/**
 * 
 */
package com.l3v.enums;

/**
 * @author Trí
 *
 */
public enum RDTCommand {

	None((byte)0,0),
	Login((byte)1,9),
	AcceptLogin((byte)2,6),
	Logout((byte)3,9),
	SendData((byte)4,0),
	Error((byte)5,0),
	CheckOnline((byte)6,0),
	RequestRemote((byte)7,89),
	AcceptRemote((byte)8,21),
	DenyRemote((byte)9,9),
	EndRemote((byte)10,5),
	GetRemoterList((byte)99,4);

	private final byte code;
	private final int size;

	private RDTCommand(byte code, int size) {
		this.code = code;
		this.size = size;
	}

	public byte getCode() {
		return code;
	}

	/**
	 * @return the size
	 */
	public final int getSize() {
		return size;
	}

	public static RDTCommand parseFromCode(byte code) {
		// switch (code) {
		// case 0:
		// default:
		// return RDTcpCommand.None;
		// case 1:
		// return RDTcpCommand.Login;
		// case 2:
		// return RDTcpCommand.AcceptLogin;
		// case 3:
		// return RDTcpCommand.Logout;
		// case 4:
		// return RDTcpCommand.SendData;
		// case 5:
		// return RDTcpCommand.Error;
		// case 6:
		// return RDTcpCommand.CheckOnline;
		// case 7:
		// return RDTcpCommand.RequestRemote;
		// case 8:
		// return RDTcpCommand.AcceptRemote;
		// case 9:
		// return RDTcpCommand.DenyRemote;
		// case 10:
		// return RDTcpCommand.EndRemote;
		// }
		for (RDTCommand command : RDTCommand.values()) {
			if (command.getCode() == code) {
				return command;
			}
		}
		return RDTCommand.None;
	}
}
