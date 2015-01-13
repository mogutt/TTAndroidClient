package com.mogujie.tt.imlib.proto;

public class MsgAckEntity {
	public int seqNo;
	public String fromId;

	@Override
	public String toString() {
		return String.format("seqNo:%d, fromId:%s", seqNo, fromId);
	}

}
