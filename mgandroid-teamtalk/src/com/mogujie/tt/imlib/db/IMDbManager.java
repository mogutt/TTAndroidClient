package com.mogujie.tt.imlib.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import com.mogujie.tt.config.ProtocolConstant;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.imlib.IMContactManager;
import com.mogujie.tt.imlib.IMLoginManager;
import com.mogujie.tt.imlib.proto.MessageEntity;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.utils.MessageSplitResult;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class IMDbManager extends SQLiteOpenHelper {

	private static final int DB_VERSION = 3;
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
		String sql = "create table if not exists session_msg ("
				+ "id int auto increment primary key,"
				+ "login_id varchar(50) not null,"
				+ "gmt_created datetime not null,"
				+ "gmt_modified datetime not null,"
				+ "session_id varchar(50) not null," // todo eric 50 is enough?
				+ "from_id varchar(50) not null,"
				+ "to_id varchar(50) not null," + "time int not null,"
				+ "type int not null," + "display_type int not null,"
				+ "content text)";

		logger.d("db#create session_msg table -> sql:%s", sql);

		// todo eric check ret value
		db.execSQL(sql);
	}

	private void createLoginTable(SQLiteDatabase db) {
		logger.d("createLoginTable");
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

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		logger.d("db#db onCreate");

		createMsgTable(db);

		createLoginTable(db);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public synchronized void updatePictureMessagePath(MessageEntity msg) {
		if (!msg.isPictureType()) {
			logger.e("db#msg is not picture");
			return;
		}

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return;
		}

		
		//todo eric use unique message id
		String sqlFormat = "update session_msg set content='%s' where from_id='%s' and time= %d";
		String sql = String.format(sqlFormat, msg.getContent(), msg.fromId, msg.createTime);
		
		logger.d("db#updatePictureMessagePath sql:%s", sql);
		
		db.execSQL(sql);
	}
	public synchronized void saveMsg(MessageEntity msg, boolean sending) {
		logger.d("db#saveMsg");

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return;
		}

		String sessionId = msg.getSessionId(sending);
		if (sessionId == null) {
			return;
		}

		logger.d("db#sessionId:%s", sessionId);

		String sql = String
				.format("insert into %s (login_id, gmt_created, gmt_modified, session_id, from_id, to_id, time, type, display_type, content) values "
						+ "('%s', datetime('now'), datetime('now'), '%s', '%s', '%s', %d, %d, %d, '%s')",
						TABLE_SESSION_MSG, IMLoginManager.instance()
								.getLoginId(), sessionId, msg.fromId, msg.toId,
						msg.createTime, msg.msgType, msg.msgInfo
								.getDisplayType(), msg.getContent());

		logger.d("db#saveSentMsg -> sql:%s", sql);

		// todo eric sql injection
		db.execSQL(sql);
	}

	public synchronized void saveLoginIdentity(String loginId, String pwd) {
		logger.d("db#loginId:%s", loginId);

		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return;
		}

		// todo eric don't use string +, use preparestament
		String sql = String.format(
				"insert into login_identity (login_id, pwd, gmt_created, gmt_modified) values "
						+ "('%s', '%s', datetime('now'), datetime('now'))",
				loginId, pwd);

		logger.d("db#saveLoginIdentity -> sql:%s", sql);

		// todo eric sql injection
		db.execSQL(sql);
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

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			LoginIdentity loginIdentity = new LoginIdentity();
			loginIdentity.loginId = cursor.getString(0);
			loginIdentity.pwd = cursor.getString(1);

			logger.d("db#todo eric remove this log!loginId:%s, pwd:%s",
					loginIdentity.loginId, loginIdentity.pwd);

			return loginIdentity;
		}

		return null;
	}

	public synchronized List<MessageInfo> getHistoryMsg(String sessionId,
			int offset, int count, int firstHistoryMsgTime) {
		logger.d(
				"messageactivity#getMsg sessionid:%s, offset:%d,  count:%d, firstHistoryMsgTime:%d",
				sessionId, offset, count, firstHistoryMsgTime);

		String sql;
		if (firstHistoryMsgTime <= 0) {
			// todo eric use bind
			sql = String
					.format("select time, from_id, to_id, type, display_type, content from %s where login_id = '%s' and session_id = '%s' order by time desc limit %d offset %d",
							TABLE_SESSION_MSG, IMLoginManager.instance()
									.getLoginId(), sessionId, count, offset);
		} else {
			sql = String
					.format("select time, from_id, to_id, type, display_type, content from %s where login_id = '%s' and session_id = '%s' and time < %d order by time desc limit %d offset %d",
							TABLE_SESSION_MSG, IMLoginManager.instance()
									.getLoginId(), sessionId,
							firstHistoryMsgTime, count, offset);

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
			String content = cursor.getString(5);
			logger.d(
					"db#fetch msg from db -> time:%d, fromId:%s, toId:%s, msgType:%d, renderType:%d, content:%s",
					time, fromId, toId, msgType, displayType, content);

			MessageInfo msgInfo = new MessageInfo();
			msgInfo.setCreated(time);
			msgInfo.setMsgFromUserId(fromId);
			msgInfo.setTargetId(toId);
			msgInfo.setMsgType((byte) msgType); // todo eric make it byte
			msgInfo.setDisplayType((byte) displayType);
			msgInfo.setMsgContent(content);

			tryRecoverAudioMsg(msgInfo, content);
			tryRecoverPicMsg(msgInfo, content, displayType);

			// todo eric
			// MessageSplitResult messageSplitResult = new MessageSplitResult(
			// msgInfo, content.getBytes());
			// messageSplitResult.decode();
			// Queue<MessageInfo> splitMsgList =
			// messageSplitResult.getMsgList();
			// for (MessageInfo splitMsg : splitMsgList) {
			// msgList.add(splitMsg);
			// }

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

			MessageEntity.AudioInfo audioInfo = MessageEntity.AudioInfo
					.create(content);
			msgInfo.setPlayTime(audioInfo.getLength());
			msgInfo.setSavePath(audioInfo.getPath());
		}
	}

	private void tryRecoverPicMsg(MessageInfo msgInfo, String content,
			int displayType) {
		if (displayType == SysConstant.DISPLAY_TYPE_IMAGE) {
			logger.d("pic#DISPLAY_TYPE_IMAGE");

			MessageEntity.PicInfo picInfo = MessageEntity.PicInfo
					.create(content);

			if (picInfo != null) {
				// todo eric
				msgInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_SUCCESSED);
				msgInfo.setSavePath(picInfo.getPath());
			} else {
				msgInfo.setMsgLoadState(SysConstant.MESSAGE_STATE_FINISH_FAILED);
			}
		}
	}

}
