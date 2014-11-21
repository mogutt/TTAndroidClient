/**
 * IM本地数据存储
 */

package com.mogujie.tt.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mogujie.tt.log.Logger;

/**
 * @author shuchen
 */
public class DBHelper extends SQLiteOpenHelper {
    private final Logger logger = Logger.getLogger(DBHelper.class);

    private static final String DATABASE_NAME = "MG_TT.db";
    // private static final String DATABASE_NAME="/sdcard/moguim.db";
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_USERS = "Users"; // 用户信息表
    public static final String TABLE_CONTACTS = "Contacts"; // 最近联系人信息表
    public static final String TABLE_MESSAGES = "Messages"; // 消息主表
    public static final String TABLE_EXTRA_TEXT = "ExtraText"; // 文本消息附加表
    public static final String TABLE_EXTRA_IMAGE = "ExtraImage"; // 图片消息附加表
    public static final String TABLE_EXTRA_AUDIO = "ExtraAudio"; // 语音消息附加表

    // 列名
    public static final String COLUMN_ID = "id"; // 自增id列
    public static final String COLUMN_USER_ID = "userId"; // 用户ID列
    public static final String COLUMN_USER_NAME = "uname"; // 用户名字列
    public static final String COLUMN_USER_NICKNAME = "unick"; // 用户昵称列
    public static final String COLUMN_USER_AVATAR = "avtar"; // 用户头像url列
    public static final String COLUMN_USER_TITLE = "title";
    public static final String COLUMN_USER_POSITION = "position";
    public static final String COLUMN_USER_ROLE_STATUS ="role_status";
    public static final String COLUMN_USER_SEX = "sex";
    public static final String COLUMN_USER_DEPART_ID = "depart_id";
    public static final String COLUMN_USER_JOB_NUMBER = "job_number";
    public static final String COLUMN_USER_TELPHONE = "telphone";
    public static final String COLUMN_USER_EMAIL = "email";

    public static final String COLUMN_RELATE_ID = "relateId"; // 两个联系人唯一联系ID
    public static final String COLUMN_OWNER_ID = "ownerId"; // 联系人所属者ID
    public static final String COLUMN_FRIEND_USER_ID = "friendUserId"; // 好友用户ID
    public static final String COLUMN_STATUS = "status"; // 联系人当前状态

    public static final String COLUMN_MESSAGE_ID = "msgId"; // 消息存储唯一ID
    public static final String COLUMN_MESSAGE_PARENT_ID = "msgParentId"; // 消息存储ID，当图文混排时，该字段有用，为该图文消息的第一条消息的ID，否则为-1
    public static final String COLUMN_MESSAGE_FROM_USER_ID = "fromUserId"; // 消息发送者ID
    public static final String COLUMN_MESSAGE_TO_USER_ID = "toUserId"; // 消息接受者ID
    public static final String COLUMN_MESSAGE_TYPE = "msgType"; // 消息类型 图文消息 1 ；
                                                                // 语音消息 100
    public static final String COLUMN_MESSAGE_DISPLAY_TYPE = "displayType"; // 消息展示类型
                                                                            // 文本,
                                                                            // 图片,
                                                                            // 语音,
                                                                            // 商品详情或更多扩展类型
    public static final String COLUMN_MESSAGE_OVERVIEW = "overview"; // 消息预览
    public static final String COLUMN_MESSAGE_STATUS = "status"; // 消息加载状态
    public static final String COLUMN_MESSAGE_READ_STATUS = "readStatus"; // 消息是否已读或已展现
    public static final String COLUMN_MESSAGE_TALKERID = "talkerId";

    public static final String COLUMN_MESSAGE_EXTRA_MSG_ID = "msgId"; // 附加消息表中消息对应的消息唯一ID
    public static final String COLUMN_MESSAGE_EXTRA_TEXT_CONTENT = "content"; // 文本消息内容
    public static final String COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH = "savePath"; // 图片保存路径
    public static final String COLUMN_MESSAGE_EXTRA_IMAGE_URL = "url"; // 图片原始URL
    public static final String COLUMN_MESSAGE_EXTRA_AUDIO_SAVE_PATH = "savePath"; // 语音保存路径
    public static final String COLUMN_MESSAGE_EXTRA_AUDIO_URL = "url"; // 语音原始URL
    public static final String COLUMN_MESSAGE_EXTRA_AUDIO_PLAY_TIME = "playTime"; // 语音播放时长
    public static final String COLUMN_MESSAGE_EXTRA_GOODS_ID = "goodsID"; // 商品ID
    // public static final String COLUMN_MESSAGE_EXTRA_GOODS_IID = "iid"; //
    // 商品ID，Item
    // Id（类似与goodsID，但作用不一）
    public static final String COLUMN_MESSAGE_EXTRA_GOODS_TITLE = "title"; // 商品名字
    public static final String COLUMN_MESSAGE_EXTRA_GOODS_NOW_PRICE = "nowPrice"; // 商品现价
    public static final String COLUMN_MESSAGE_EXTRA_GOODS_OLD_PRICE = "oldPrice"; // 商品原价
    public static final String COLUMN_MESSAGE_EXTRA_GOODS_URL = "goodsUrl"; // 商品的URL
    public static final String COLUMN_MESSAGE_EXTRA_GOODS_IMAGE_URL = "imgUrl"; // 商品的图片URL

    public static final String COLUMN_CREATED = "created"; // 创建时间列
    public static final String COLUMN_UPDATED = "updated"; // 更新时间列

    public static final String TEMP_SUFFIX = "_temp_suffix"; // 数据库更新时，表名后缀

    public final String CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                    + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_USER_ID + " VARCHAR(20) NOT NULL, "
                    + COLUMN_USER_NAME + " VARCHAR(20) NOT NULL, "
                    + COLUMN_USER_NICKNAME + " VARCHAR(20) NOT NULL, "
                    + COLUMN_USER_AVATAR + " VARCHAR(1024) NOT NULL, "
                    + COLUMN_USER_TITLE + " VARCHAR(256) NOT NULL, "
                    + COLUMN_USER_POSITION + " VARCHAR(256) NOT NULL, "
                    + COLUMN_USER_ROLE_STATUS + " INTEGER DEFAULT 0, "
                    + COLUMN_USER_SEX + " INTEGER DEFAULT 0, "
                    + COLUMN_USER_DEPART_ID + " VARCHAR(64) NOT NULL, "
                    + COLUMN_USER_JOB_NUMBER + " INTEGER DEFAULT 0, "
                    + COLUMN_USER_TELPHONE + " VARCHAR(64) NOT NULL, "
                    + COLUMN_USER_EMAIL + " VARCHAR(64) NOT NULL, "
                    + COLUMN_CREATED + " INTEGER DEFAULT 0, "
                    + COLUMN_UPDATED + " INTEGER DEFAULT 0);";

    public final String CREATE_CONTACTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS + " ("
                    + COLUMN_RELATE_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_OWNER_ID + " VARCHAR(20) NOT NULL, "
                    + COLUMN_USER_ID + " VARCHAR(20) NOT NULL, "
                    + COLUMN_FRIEND_USER_ID + " VARCHAR(20) NOT NULL, "
                    + COLUMN_STATUS + " INTEGER  NOT NULL DEFAULT 0,"
                    + COLUMN_CREATED + " INTEGER DEFAULT 0, "
                    + COLUMN_UPDATED + " INTEGER DEFAULT 0);";

    public final String CREATE_MESSAGES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MESSAGES + " ("
                    + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_MESSAGE_ID + " INTEGER NOT NULL, "
                    + COLUMN_MESSAGE_PARENT_ID + " INTEGER NOT NULL DEFAULT -1, "
                    + COLUMN_OWNER_ID + " VARCHAR(20) NOT NULL, "
                    + COLUMN_RELATE_ID + " INTEGER NOT NULL, "
                    + COLUMN_MESSAGE_FROM_USER_ID + " VARCHAR(20) NOT NULL, "
                    + COLUMN_MESSAGE_TO_USER_ID + " VARCHAR(20) NOT NULL, "
                    + COLUMN_MESSAGE_TALKERID + " VARCHAR(20) NOT NULL, "
                    + COLUMN_MESSAGE_TYPE + " INTEGER NOT NULL DEFAULT 1, "
                    + COLUMN_MESSAGE_DISPLAY_TYPE + " INTEGER NOT NULL DEFAULT 7, "
                    + COLUMN_MESSAGE_OVERVIEW + " VARCHAR(1024), "
                    + COLUMN_MESSAGE_STATUS + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_MESSAGE_READ_STATUS + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_CREATED + " INTEGER DEFAULT 0, "
                    + COLUMN_UPDATED + " INTEGER DEFAULT 0);";

    public final String CREATE_EXTRA_TEXT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EXTRA_TEXT + " ("
                    + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_MESSAGE_ID + " INTEGER NOT NULL, "
                    + COLUMN_MESSAGE_EXTRA_TEXT_CONTENT + " VARCHAR(1024) NOT NULL, "
                    + COLUMN_CREATED + " INTEGER DEFAULT 0, "
                    + COLUMN_UPDATED + " INTEGER DEFAULT 0);";

    public final String CREATE_EXTRA_IMAGE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EXTRA_IMAGE + " ("
                    + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_MESSAGE_ID + " INTEGER NOT NULL, "
                    + COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH + " VARCHAR(256), "
                    + COLUMN_MESSAGE_EXTRA_IMAGE_URL + " VARCHAR(1024), "
                    + COLUMN_CREATED + " INTEGER DEFAULT 0, "
                    + COLUMN_UPDATED + " INTEGER DEFAULT 0);";

    public final String CREATE_EXTRA_AUDIO =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EXTRA_AUDIO + " ("
                    + COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_MESSAGE_ID + " INTEGER NOT NULL, "
                    + COLUMN_MESSAGE_EXTRA_AUDIO_SAVE_PATH + " VARCHAR(256), "
                    + COLUMN_MESSAGE_EXTRA_AUDIO_URL + " VARCHAR(1024), "
                    + COLUMN_MESSAGE_EXTRA_AUDIO_PLAY_TIME + " INTEGER NOT NULL DEFAULT 0, "
                    + COLUMN_CREATED + " INTEGER DEFAULT 0, "
                    + COLUMN_UPDATED + " INTEGER DEFAULT 0);";

    // UserDB
    public final static String INSERT_USER_SQL = "INSERT INTO " + TABLE_USERS + "(`"
            + COLUMN_USER_ID + "`, `"
            + COLUMN_USER_NAME + "`, `"
            + COLUMN_USER_NICKNAME + "`, `"
            + COLUMN_USER_AVATAR + "`, `"
            + COLUMN_USER_TITLE + "`, `"
            + COLUMN_USER_POSITION + "`, `"
            + COLUMN_USER_ROLE_STATUS + "`, `"
            + COLUMN_USER_SEX + "`, `"
            + COLUMN_USER_DEPART_ID + "`, `"
            + COLUMN_USER_JOB_NUMBER + "`, `"
            + COLUMN_USER_TELPHONE + "`, `"
            + COLUMN_USER_EMAIL + "`, `"
            + COLUMN_CREATED + "`, `"
            + COLUMN_UPDATED + "`)  VALUES(?,?,?,?,?, ?,?,?,?, ?,?, ?, ?, ?)";
    public final static String SELECT_ALL_USER_SQL = "SELECT * FROM " + TABLE_USERS;

    // ContactDB
    public final static String INSERT_CONTACT_SQL = "INSERT INTO " + TABLE_CONTACTS + "(`"
            + COLUMN_OWNER_ID + "`, `"
            + COLUMN_USER_ID + "`, `"
            + COLUMN_FRIEND_USER_ID + "`, `"
            + COLUMN_STATUS + "`, `"
            + COLUMN_CREATED + "`, `"
            + COLUMN_UPDATED + "`)  VALUES(?, ?, ?, ?, ?, ?)";
    public final static String SELECT_ALL_CONTACT_SQL = "SELECT * FROM " + TABLE_CONTACTS;
    public final static String SELECT_ALL_FRIEND_USERID_SQL = "SELECT * FROM " + TABLE_CONTACTS;
    public final static String LIMIT_ONE_CONTACT_SUFFIX = " ORDER BY " + COLUMN_RELATE_ID
            + " asc limit 1";

    // MessageDB
    public final static String INSERT_MESSAGE_SQL = "INSERT INTO "
            + TABLE_MESSAGES + "(`"
            + COLUMN_MESSAGE_ID + "`, `"
            + COLUMN_MESSAGE_PARENT_ID + "`, `"
            + COLUMN_OWNER_ID + "`, `"
            + COLUMN_RELATE_ID + "`, `"
            + COLUMN_MESSAGE_FROM_USER_ID + "`, `"
            + COLUMN_MESSAGE_TO_USER_ID + "`, `"
            + COLUMN_MESSAGE_TYPE + "`, `"
            + COLUMN_MESSAGE_DISPLAY_TYPE + "`, `"
            + COLUMN_MESSAGE_OVERVIEW + "`, `"
            + COLUMN_MESSAGE_STATUS + "`, `"
            + COLUMN_MESSAGE_READ_STATUS + "`, `"
            + COLUMN_CREATED + "`, `"
            + COLUMN_UPDATED + "`) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public final static String INSERT_MESSAGE_EXTRA_TEXT = "INSERT INTO "
            + TABLE_EXTRA_TEXT + "(`"
            + COLUMN_MESSAGE_ID + "`, `"
            + COLUMN_MESSAGE_EXTRA_TEXT_CONTENT + "`, `"
            + COLUMN_CREATED + "`, `"
            + COLUMN_UPDATED + "`) VALUES(?, ?, ?, ?)";

    public final static String INSERT_MESSAGE_EXTRA_IMAGE = "INSERT INTO "
            + TABLE_EXTRA_IMAGE + "(`"
            + COLUMN_MESSAGE_ID + "`, `"
            + COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH + "`, `"
            + COLUMN_MESSAGE_EXTRA_IMAGE_URL + "`, `"
            + COLUMN_CREATED + "`, `"
            + COLUMN_UPDATED + "`) VALUES(?, ?, ?, ?, ?)";

    public final static String INSERT_MESSAGE_EXTRA_AUDIO = "INSERT INTO "
            + TABLE_EXTRA_AUDIO + "(`"
            + COLUMN_MESSAGE_ID + "`, `"
            + COLUMN_MESSAGE_EXTRA_AUDIO_SAVE_PATH + "`, `"
            + COLUMN_MESSAGE_EXTRA_AUDIO_URL + "`, `"
            + COLUMN_MESSAGE_EXTRA_AUDIO_PLAY_TIME + "`, `"
            + COLUMN_CREATED + "`, `"
            + COLUMN_UPDATED + "`) VALUES(?, ?, ?, ?, ?, ?)";

    public final static String LIMIT_ONE_MESSAGE_SUFFIX = " ORDER BY "
            + COLUMN_MESSAGE_ID + " desc , "
            + COLUMN_CREATED + " desc limit 1";

    private static DBHelper instance;

    public static DBHelper getInstance(Context context) {
        if (null == instance) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static int getVersion() {
        return DATABASE_VERSION;
    }

    // 数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(this.CREATE_USERS);
        db.execSQL(this.CREATE_CONTACTS);
        db.execSQL(this.CREATE_MESSAGES);
        db.execSQL(this.CREATE_EXTRA_TEXT);
        db.execSQL(this.CREATE_EXTRA_IMAGE);
        db.execSQL(this.CREATE_EXTRA_AUDIO);
    }

    // 如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 更改数据库版本的操作 暂时先直接删除旧表
        logger.d("moguimdb onUpgrade DB");
        // updateTable(db); //升级 测试可用
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_EXTRA_TEXT);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_EXTRA_IMAGE);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_EXTRA_AUDIO);

        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        logger.d("moguimdb onOpen DB");
        super.onOpen(db);
        // TODO 每次成功打开数据库后首先被执行
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        logger.d("moguimdb onDowngrade DB");
        onUpgrade(db, oldVersion, newVersion);
    }

    @SuppressWarnings("unused")
    private void updateTable(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            logger.d("moguimdb onUpgrade DB table start");
            // rename the table
            String alterMessageTableSql = getAlterTableSql(DBHelper.TABLE_MESSAGES);
            db.execSQL(alterMessageTableSql);
            logger.d("moguimdb onUpgrade DB alter table");

            // creat table
            db.execSQL(CREATE_MESSAGES); // TODO 升级时改成新表名
            logger.d("moguimdb onUpgrade DB create new table");

            // load data
            logger.d("moguimdb onUpgrade DB load data");
            String selectColumns = getColumnNames(db, DBHelper.TABLE_MESSAGES + TEMP_SUFFIX); // 记得用旧表名哦
            String updateMessagesSql = "insert into " + DBHelper.TABLE_MESSAGES + " ("
                    + selectColumns + ") "
                    + "select " + selectColumns + "" + " " + " from " + DBHelper.TABLE_MESSAGES
                    + TEMP_SUFFIX;
            logger.d("moguimdb onUpgrade DB updateMessagesSql : " + updateMessagesSql);
            db.equals(updateMessagesSql);

            // drop the oldtable
            logger.d("moguimdb onUpgrade DB drop old table");
            String dropMessageTableSql = getDropTableSql(DBHelper.TABLE_MESSAGES + TEMP_SUFFIX);
            db.execSQL(dropMessageTableSql);
            logger.d("moguimdb onUpgrade DB table end");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            logger.d("moguimdb onUpgrade DB error with reason: " + e.toString());
        } finally {
            db.endTransaction();
        }

    }

    private String getAlterTableSql(String oldTableName) {
        return "alter table " + oldTableName + " rename to " + oldTableName + TEMP_SUFFIX;
    }

    private String getDropTableSql(String tableName) {
        return "drop table if exists " + tableName;
    }

    // 获取升级前表中的字段
    protected String getColumnNames(SQLiteDatabase db, String tableName)
    {
        StringBuffer sbSelect = null;
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (null != c) {
                int columnIndex = c.getColumnIndex("name");
                if (-1 == columnIndex) {
                    return null;
                }

                int index = 0;
                int pos = c.getCount() + 1; // 并标记最后一个不加逗号，
                                            // 由于index从0开始，所有这里不用加2,只加1
                sbSelect = new StringBuffer(c.getCount() + 2); // //字段总列数，增加2列需要加2
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    sbSelect.append(c.getString(columnIndex));
                    index++;
                    if (index < pos) {
                        sbSelect.append(",");
                    }
                }
            }
        } catch (Exception e) {
            logger.e(e.getMessage());
        } finally {
            if (null != c) {
                c.close();
            }
        }

        return sbSelect.toString();
    }

    /**
     * 查询DB中某表的最后一条ID
     * 
     * @return msgId
     */
    public int queryLastInsertId(String tableName) {
        int lastMsgId = 0;
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = getReadableDatabase();
            c = dbSlaver.rawQuery("select last_insert_rowid() from "
                    + tableName, null);
            if (c.moveToFirst()) {
                lastMsgId = c.getInt(0);
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close(); //这里不能关闭哦，因为是在其它DB语句中间操作的，由其它DB操作部分关闭即可
        }

        return lastMsgId;
    }

}
