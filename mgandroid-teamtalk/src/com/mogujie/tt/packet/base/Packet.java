package com.mogujie.tt.packet.base;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Downloader.Response;

/**
 * 协议包基类，子类必须实现{@link #encode()}和{@link #decode(DataBuffer)}
 * 
 * @author dolphinWang
 * @time 2014/04/30
 */
public abstract class Packet {

	protected Request mRequest;

	protected Response mResponse;

	protected boolean mNeedMonitor;

	public void setNeedMonitor(boolean bNeedMonitor) {
		mNeedMonitor = bNeedMonitor;
	}

	public int getSequenceNo() {
		return (int) (mRequest.mHeader.getReserved());
	}

	public boolean getNeedMonitor() {
		return mNeedMonitor;
	}

	public Response getResponse() {
		return mResponse;
	}

	public void setRequest(Request request) {
		mRequest = request;
	}

	public Request getRequest() {
		return mRequest;
	}

	/**
	 * 把Request数据结构编码成一个DataBuffer，必须先调用setRequest
	 */
	public abstract DataBuffer encode();

	/**
	 * 把DataBuffer解包构造一个Response对象，getResponse函数必须在调用完decode函数之后才能得到真实的包
	 */
	public abstract void decode(DataBuffer buffer);

	/**
	 * 请求包的数据结构基类，子类可以继承后添加属于自己的字段
	 */
	public static class Request {
		/*** 成员变量 ***/
		protected Header mHeader;

		public Header getHeader() {
			return mHeader;
		}

		public void setHeader(Header header) {
			mHeader = header;
		}
	}

	public static class Ack extends Request {

	}

	public static class Notify extends Response {

	}

	/**
	 * 应答包的数据结构基类，子类可以继承后添加属于自己的字段
	 */
	public static class Response {
		/*** 成员变量 ***/
		protected Header mHeader;

		public Header getHeader() {
			return mHeader;
		}

		public void setHeader(Header header) {
			mHeader = header;
		}
	}

	public static byte[] getUtf8Bytes(String content) {
		if (content == null) {
			return null;
		}

		return content.getBytes(Charset.forName("utf8"));
	}

	public static int getStringLen(String content) {
		if (content == null || content.isEmpty()) {
			return 4 + 0;
		}

		return 4 + getUtf8Bytes(content).length;
	}

	public static int getIntLen(int a) {
		return 4;
	}

	public static int getStringListLen(List<String> stringList) {
		int len = 4; // cnt size
		for (String content : stringList) {
			len += getStringLen(content);
		}

		return len;
	}

	public static void writeStringList(List<String> stringList,
			DataBuffer bodyBuffer) {
		bodyBuffer.writeInt(stringList.size());

		for (String content : stringList) {
			bodyBuffer.writeString(content);
		}
	}

	public static List<String> readStringList(DataBuffer buffer) {
		List<String> stringList = new ArrayList<String>();
		int cnt = buffer.readInt();
		for (int i = 0; i < cnt; ++i) {
			String content = buffer.readString();
			stringList.add(content);
		}

		return stringList;
	}

}
