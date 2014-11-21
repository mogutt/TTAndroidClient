package com.mogujie.tt.imlib.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.imlib.IMUnAckMsgManager;
import com.mogujie.tt.imlib.proto.MessageEntity;
import com.mogujie.tt.log.Logger;

public class IMDbManager extends SQLiteOpenHelper {

	private static final int DB_VERSION = 4;
	private static final String TABLE_SESSION_MSG = "session_msg";

	private Logger logger = Logger.getLogger(IMDbManager.class);

	private static IMDbManager inst;

	public static synchronized IMDbManager instance(Context ctx) {
		if (inst == null) {
			inst = new IMDbManager(ctx, "tt.db", null, DB_VERSION);
		}

		return inst;
	}

	public IMDbManager(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	private void createMsgTable(SQLiteDatabase db) {
		logger.d("db#createMsgTable");

		String sql = "create table if not exists session_msg ("
				+ "id int auto increment primary key,"
				+ "login_id varchar(50) not null,"
				+ "msg_id varchar(50) not null,"
				+ "gmt_created datetime not null,"
				+ "gmt_modified datetime not null,"
				+ "session_id varchar(50) not null,"
				+ "session_type int not null,"
				+ "from_id varchar(50) not null,"
				+ "to_id varchar(50) not null," + "time int not null,"
				+ "type int not null," + "display_type int not null,"
				+ "talker_id varchar(50) not null,"
				+ "status int default 2 not null,"
				+ "int_reserved1 int default 0,"
				+ "int_reserved2 int default 0,"
				+ "string_reserved1 text default '',"
				+ "string_reserved2 text default ''," + "content text)";

		logger.d("db#create session_msg table -> sql:%s", sql);

		db.execSQL(sql);
	}

	private void createLoginTable(SQLiteDatabase db) {
		logger.d("db#createLoginTable");

		String sql = "create table if not exists login_identity ("
				+ "id int auto increment primary key,"
				+ "login_id varchar(50) not null,"
				+ "pwd varchar(50) not null,"
				+ "gmt_created datetime not null,"
				+ "gmt_modified datetime not null)";

		logger.d("db#create login_identity table -> sql:%s", sql);

		// todo eric check ret value
		db.execSQL(sql);
	}

	private void createConfigurationTable(SQLiteDatabase db) {
		logger.d("db#config#createConfigurationTable");

		String sql = "create table if not exists configuration ("
				+ "id int auto increment primary key,"
				+ "category varchar(50) not null,"
				+ "key varchar(50) not null," + "value text not null,"
				+ "gmt_created datetime not null,"
				+ "gmt_modified datetime not null)";

		logger.d("db#create configuration table -> sql:%s", sql);

		// todo eric check ret value
		db.execSQL(sql);
	}

	private void createSessionLastMsgTable(SQLiteDatabase db) {
		logger.d("db#config#createSessionLastMsgTable");

		String sql = "create table if not exists session_last_msg_table ("
				+ "id int auto increment primary key,"
				+ "login_id varchar(50) not null,"
				+ "gmt_created datetime not null,"
				+ "gmt_modified datetime not null,"
				+ "talker_id varchar(50) not null,"
				+ "session_id varchar(50) not null,"
				+ "session_type int not null," + "time int not null)";

		logger.d("db#create session last msg table -> sql:%s", sql);

		// todo eric check ret value
		db.execSQL(sql);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		logger.d("db#db onCreate");

		createMsgTable(db);

		createLoginTable(db);

		createConfigurationTable(db);

		createSessionLastMsgTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public synchronized void deleteMsg(String msgId) {
		logger.d("db#deleteMsg, msgId:%s", msgId);

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			logger.e("db#getWritableDatabase failed");
			return;
		}

		String sql = String.format("delete from session_msg where msg_id == '%s'", msgId);
		logger.d("db#sql:%s", sql);

		db.execSQL(sql);
	}

	public synchronized void saveMsg(MessageInfo msg, boolean sending) {

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			logger.e("db#getWritableDatabase failed");
			return;
		}

		saveMsgImpl(msg, sending, db);
	}

	public synchronized void saveMsgs(List<MessageInfo> msgList, boolean sending) {
		logger.d("db#saveMsgs, size:%d", msgList.size());

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			logger.e("db#getWritableDatabase failed");
			return;
		}


		//without using transaction, the speed of inserting 100 messages is very slow
		db.beginTransaction();
		try {
			for (MessageInfo msg : msgList) {
				saveMsgImpl(msg, sending, db);
			}
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			logger.e("db#save msgs exception:%s", e.getMessage());
		}
		finally {
			db.endTransaction();
			logger.i("db#saveMsgs transaction ok");
		}
	}

	public synchronized void saveMsgImpl(MessageInfo msg, boolean sending,
			SQLiteDatabase db) {
		logger.d("db#saveMsg, msg:%s", msg);

		//todo eric workaround
		if (TextUtils.isEmpty(msg.talkerId)) {
//			logger.e("talkerid#empty talkerid, callstack:%s", Log.getStackTraceString(new Throwable()));
			logger.e("talkerid#empty talkerid");
			msg.talkerId = msg.fromId;
		}

		String sessionId = msg.getSessionId(sending);
		logger.d("db#sessionId:%s", sessionId);

		// String sql =
		// String.format("insert into %s (login_id, msg_id, gmt_created, gmt_modified, session_id, session_type, from_id, to_id, time, type, display_type, status, content) values "
		// +
		// "('%s', '%s', datetime('now'), datetime('now'), '%s', %d, '%s', '%s', %d, %d, %d, %d, '%s')",
		// TABLE_SESSION_MSG, IMLoginManager.instance().getLoginId(), msg.msgId,
		// sessionId, msg.sessionType, msg.fromId, msg.toId, msg.createTime,
		// msg.type, msg.getDisplayType(), msg.getMsgLoadState(),
		// msg.getContent());
		//
		// logger.d("db#saveMsg -> sql:%s", sql);
		//
		// db.execSQL(sql);

		// todo eric, consider sql injection risk thoroughly
		String sql = "insert into session_msg (login_id, msg_id, gmt_created, gmt_modified, session_id, session_type, from_id, to_id, time, type,talker_id, display_type, status, content) values "
				+ "(?, ?, datetime('now'), datetime('now'), ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

		logger.d("db#saveMsg -> sql:%s", sql);

		//todo eric use id to reference login id
		db.execSQL(sql, new Object[]{IMLoginManager.instance().getLoginId(),
				msg.msgId, sessionId, msg.sessionType, msg.fromId, msg.toId,
				msg.createTime, msg.type, msg.talkerId, msg.getDisplayType(),
				msg.getMsgLoadState(), msg.getContent()});

	}

	public synchronized void updateSessionLastMsg(MessageInfo msg) {
		logger.d("db#repeat#updateSessionLastMsg msg:%s", msg);

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			logger.e("db#repeat#getWritableDatabase failed");
			return;
		}

		db.beginTransaction();
		try {

			String sqlFmt = "delete from session_last_msg_table where login_id = ? and talker_id = ? and session_id = ? and session_type = ?";
			logger.d("db#repeat#sqlFmt:%s", sqlFmt);

			String loginId = IMLoginManager.instance().getLoginId();
			db.execSQL(sqlFmt, new Object[]{loginId, msg.talkerId,
					msg.sessionId, msg.sessionType});

			sqlFmt = "insert into session_last_msg_table (login_id, talker_id, session_id, session_type, time, gmt_created, gmt_modified) values (?, ?, ?, ?, ?, datetime('now'), datetime('now'))";
			logger.d("db#repeat#sqlFmt:%s", sqlFmt);

			db.execSQL(sqlFmt, new Object[]{loginId, msg.talkerId,
					msg.sessionId, msg.sessionType, msg.getOriginalTime()});

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			logger.i("db#repeat#updateSessionLastMsg transaction ok");
		}
	}

	public synchronized int getLastSessionMsgTime(MessageInfo oneSessionMsg) {
		logger.d("db#repeat#getLastSessionMsg -> oneSessionMsg:%s", oneSessionMsg);

		SQLiteDatabase db = getReadableDatabase();
		if (db == null) {
			logger.e("db#repeat#db is null");
			return 0;
		}

		//todo eric sessionType is still unclear when finding unread message
		String sql = "select time from session_last_msg_table where login_id = ? and talker_id = ? and session_id = ? order by gmt_created desc limit 1";
		logger.d("db#repeat#sql:%s", sql);

		Cursor cursor = db.rawQuery(sql, new String[]{
				IMLoginManager.instance().getLoginId(), oneSessionMsg.talkerId,oneSessionMsg.sessionId});
		logger.d("db#repeat#cursor:%s", cursor);

		if (cursor == null) {
			logger.d("db#repeat#no last session msg record in db");

			return 0;
		}

		for (cursor.moveToFirst(); !cursor.isAfterLast(); /* cursor.moveToNext() */) {
			int time = cursor.getInt(0);

			logger.d("db#repeat#last session time is %d", time);

			return time;
		}

		return 0;
	}

	public synchronized void updatePictureMessagePath(MessageInfo msg) {
		if (!msg.isImage()) {
			logger.e("db#msg is not picture");
			return;
		}

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return;
		}

		// todo eric use bind
		String sqlFormat = "update session_msg set content='%s', status=%d where msg_id='%s'";
		String sql = String.format(sqlFormat, msg.getContent(), msg.getMsgLoadState(), msg.msgId);

		logger.d("db#updatePictureMessagePath sql:%s", sql);

		db.execSQL(sql);
	}

	public synchronized void updateMessageStatus(MessageInfo msg) {
		logger.d("db#updateMessageStatus msg:%s", msg);

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return;
		}

		String sqlFormat = "update session_msg set status=%d where msg_id='%s'";
		String sql = String.format(sqlFormat, msg.getMsgLoadState(), msg.msgId);

		logger.d("db#upateMessageStatus sql:%s", sql);

		db.execSQL(sql);
	}

	public synchronized void updateMessageContent(MessageInfo msg) {
		logger.d("db#updateMessageContent msg:%s", msg);

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return;
		}

		String sqlFormat = "update session_msg set content=? where msg_id=?";

		logger.d("db#upateMessageStatus sql:%s", sqlFormat);

		db.execSQL(sqlFormat, new Object[]{msg.getContent(), msg.msgId});
	}

	public synchronized void saveLoginIdentity(String loginId, String pwd) {
		logger.d("db#loginId:%s", loginId);

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return;
		}

		// todo eric don't use string +, use preparestament
		String sql = String.format("insert into login_identity (login_id, pwd, gmt_created, gmt_modified) values "
				+ "('%s', '%s', datetime('now'), datetime('now'))", loginId, pwd);

		logger.d("db#saveLoginIdentity -> sql:%s", sql);

		// todo eric sql injection
		db.execSQL(sql);
	}

	public synchronized void updateConfiguration(String category, String key,
			String value) {
		logger.d("db#config#updateConfiguration -> category:%s, key:%s, value:%s", category, key, value);

		if (category == null || key == null) {
			logger.e("db#config#invalid args");
			return;
		}

		if (value == null) {
			value = "";
		}

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return;
		}

		db.beginTransaction();
		try {

			String sqlFmt = "delete from configuration where category = ? and key = ?";
			logger.d("db#config#sqlFmt:%s", sqlFmt);

			db.execSQL(sqlFmt, new Object[]{category, key});

			sqlFmt = "insert into configuration (category, key, value, gmt_created, gmt_modified) values (?, ?, ?, datetime('now'), datetime('now'))";
			logger.d("db#config#sqlFmt:%s", sqlFmt);

			db.execSQL(sqlFmt, new Object[]{category, key, value});

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			logger.i("db#config#updateConfiguration transaction ok");
		}
	}

	public synchronized String getConfiguration(String category, String key) {
		logger.d("db#config#getConfiguration category:%s, key:%s", category, key);

		String sql = "select value from configuration where category = ? and key = ? order by gmt_created desc limit 1";
		logger.d("db#config#sql:%s", sql);
		SQLiteDatabase db = getReadableDatabase();
		if (db == null) {
			logger.e("db#db is null");
			return null;
		}

		Cursor cursor = db.rawQuery(sql, new String[]{category, key});

		for (cursor.moveToFirst(); !cursor.isAfterLast(); /* cursor.moveToNext() */) {
			String value = cursor.getString(0);

			logger.d("db#config#value:%s", value);

			return value;
		}

		return null;
	}

	public class LoginIdentity {
		public String loginId;
		public String pwd;
	}

	public synchronized LoginIdentity loadLoginIdentity() {
		// todo eric optimization
		String sql = "select login_id, pwd from login_identity order by gmt_created desc limit 1";
		logger.d("db#loadLoginIdentity -> sql:%s", sql);
		SQLiteDatabase db = getReadableDatabase();
		if (db == null) {
			logger.e("db#db is null");
			return null;
		}

		Cursor cursor = db.rawQuery(sql, null);

		for (cursor.moveToFirst(); !cursor.isAfterLast(); /* cursor.moveToNext() */) {
			LoginIdentity loginIdentity = new LoginIdentity();
			loginIdentity.loginId = cursor.getString(0);
			loginIdentity.pwd = cursor.getString(1);

			logger.d("db#loginId:%s", loginIdentity.loginId);

			return loginIdentity;
		}

		return null;
	}

	private int refreshMessageStatus(String msgId, int dbStatus) {
		if (dbStatus != SysConstant.MESSAGE_STATE_FINISH_SUCCESSED) {
			// if it's loading status, but not in unack list, make it failed
			// status
			// todo eric, picture loading status has 2 steps, handle the
			// first step(uploading step)
			// status = SysConstant.MESSAGE_STATE_FINISH_FAILED;
			MessageInfo unackMsg = IMUnAckMsgManager.instance().get(msgId);
			if (unackMsg != null) {
				dbStatus = unackMsg.getMsgLoadState();
			} else {
				if (dbStatus == SysConstant.MESSAGE_STATE_LOADDING) {
					// for image, the status is unload
					// for unfinished audio or text messages, make the status
					// failed
					dbStatus = SysConstant.MESSAGE_STATE_FINISH_FAILED;
				} else if (dbStatus == SysConstant.MESSAGE_STATE_UNLOAD) {
					//do nothing, so image message can still be fetched from the network
				}
			}
		}

		return dbStatus;
	}

	//	public synchronized MessageInfo getLastSessionMsg(String sessionId, int sessionType) {
	//		logger.d("db#getLastSessionMsg sessionId:%s, sessionType:%d", sessionId, sessionType);
	//		
	//		String sqlFormat = String.format("select time, content from session_msg where login_id = ? and session_id = ? and session_type = ? order by time desc limit 1", TABLE_SESSION_MSG, IMLoginManager.instance().getLoginId(), sessionId, sessionType);
	//		logger.d("db#sql %s", sqlFormat);
	//
	//		SQLiteDatabase db = getReadableDatabase();
	//		if (db == null) {
	//			logger.e("db#db is null");
	//			return null;
	//		}
	//
	//		Cursor cursor = db.rawQuery(sqlFormat, null);
	//
	//		List<MessageInfo> msgList = new ArrayList<MessageInfo>();
	//
	//		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	//			int time = cursor.getInt(0);
	//			String fromId = cursor.getString(1);
	//			String toId = cursor.getString(2);
	//			int msgType = cursor.getInt(3);
	//			int displayType = cursor.getInt(4);
	//			int status = cursor.getInt(5);
	//			String content = cursor.getString(6);
	//			String msgId = cursor.getString(7);
	//			status = refreshMessageStatus(msgId, status);
	//
	//			logger.d("db#fetch msg from db -> time:%d, fromId:%s, toId:%s, msgType:%d, renderType:%d, status:%d, content:%s, msgId:%s", time, fromId, toId, msgType, displayType, status, content, msgId);
	//
	//			MessageInfo msgInfo = new MessageInfo();
	//			msgInfo.msgId = msgId;
	//			msgInfo.setCreated(time);
	//			msgInfo.setMsgFromUserId(fromId);
	//			msgInfo.setTargetId(toId);
	//			msgInfo.setMsgType((byte) msgType); // todo eric make it byte
	//			msgInfo.setDisplayType((byte) displayType);
	//			msgInfo.setMsgLoadState(status);
	//			msgInfo.setMsgContent(content);
	//			msgInfo.sessionId = sessionId;
	//			msgInfo.sessionType = sessionType;
	//
	//			tryRecoverAudioMsg(msgInfo, content);
	//			tryRecoverPicMsg(msgInfo, content, displayType);
	//
	//			msgList.add(msgInfo);
	//		}
	//
	//		// todo eric
	//		Collections.reverse(msgList);
	//
	//		return msgList;
	//	}

	public synchronized List<MessageInfo> getHistoryMsg(String sessionId,
			int sessionType, int offset, int count, int firstHistoryMsgTime) {
		logger.d("db#getMsg sessionid:%s, sessionType:%d, offset:%d,  count:%d, firstHistoryMsgTime:%d", sessionId, sessionType, offset, count, firstHistoryMsgTime);

		// todo eric use bind
		String sql;
		if (firstHistoryMsgTime <= 0) {
			sql = String.format("select time, from_id, to_id, type,display_type, status, content, msg_id ,talker_id from %s where login_id = '%s' and session_id = '%s' and session_type = %d order by time desc limit %d offset %d", TABLE_SESSION_MSG, IMLoginManager.instance().getLoginId(), sessionId, sessionType, count, offset);
		} else {
			sql = String.format("select time, from_id, to_id, type,display_type, status, content, msg_id ,talker_id from %s where login_id = '%s' and session_id = '%s' and session_type = %d and time < %d order by time desc limit %d offset %d", TABLE_SESSION_MSG, IMLoginManager.instance().getLoginId(), sessionId, sessionType, firstHistoryMsgTime, count, offset);

		}
		logger.d("db#sql %s", sql);

		SQLiteDatabase db = getReadableDatabase();
		if (db == null) {
			logger.e("db#db is null");
			return null;
		}

		Cursor cursor = db.rawQuery(sql, null);

		List<MessageInfo> msgList = new ArrayList<MessageInfo>();

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int time = cursor.getInt(0);
			String fromId = cursor.getString(1);
			String toId = cursor.getString(2);
			int msgType = cursor.getInt(3);
			int displayType = cursor.getInt(4);
			int status = cursor.getInt(5);
			String content = cursor.getString(6);
			String msgId = cursor.getString(7);
			String talkerId = cursor.getString(8);
			status = refreshMessageStatus(msgId, status);

			logger.d("db#fetch msg from db -> time:%d, fromId:%s, toId:%s, msgType:%d, renderType:%d, status:%d, content:%s, msgId:%s", time, fromId, toId, msgType, displayType, status, content, msgId);

			MessageInfo msgInfo = new MessageInfo();
			msgInfo.msgId = msgId;
			msgInfo.setCreated(time);
			msgInfo.setMsgFromUserId(fromId);
			msgInfo.setTargetId(toId);
			msgInfo.setMsgType((byte) msgType); // todo eric make it byte
			msgInfo.setDisplayType((byte) displayType);
			msgInfo.setMsgLoadState(status);
			msgInfo.setMsgContent(content);
			msgInfo.sessionId = sessionId;
			msgInfo.sessionType = sessionType;
			msgInfo.talkerId = talkerId;

			tryRecoverAudioMsg(msgInfo, content);
			tryRecoverPicMsg(msgInfo, content, displayType);

			msgList.add(msgInfo);
		}

		// todo eric
		Collections.reverse(msgList);

		return msgList;
	}

	private void tryRecoverAudioMsg(MessageInfo msgInfo, String content) {
		if (msgInfo.getMsgType() == ProtocolConstant.MSG_TYPE_P2P_AUDIO
				|| msgInfo.getMsgType() == ProtocolConstant.MSG_TYPE_GROUP_AUDIO) {
			logger.d("db#audio#tryRecoverAudioMsg");

			MessageEntity.AudioInfo audioInfo = MessageEntity.AudioInfo.create(content);
			msgInfo.setPlayTime(audioInfo.getLength());
			msgInfo.setSavePath(audioInfo.getPath());
			msgInfo.setMsgReadStatus(audioInfo.getReadStatus());
		}
	}

	private void tryRecoverPicMsg(MessageInfo msgInfo, String content,
			int displayType) {
		if (displayType == SysConstant.DISPLAY_TYPE_IMAGE) {
			logger.d("pic#DISPLAY_TYPE_IMAGE");

			MessageEntity.PicInfo picInfo = MessageEntity.PicInfo.create(content);

			if (picInfo != null) {
				msgInfo.setSavePath(picInfo.getPath());
				msgInfo.setUrl(picInfo.getUrl());
			}
		}
	}

}
