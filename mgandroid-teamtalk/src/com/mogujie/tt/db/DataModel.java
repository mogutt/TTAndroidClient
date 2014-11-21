/**
 * DB的表操作类
 * @author shuchen
 */

package com.mogujie.tt.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.mogujie.tt.app.IMEntrance;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.IMRecentContact;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.entity.User;
import com.mogujie.tt.log.Logger;

public class DataModel {
    private DBHelper helper;
    private Logger logger = Logger.getLogger(DataModel.class);

    private static String SQL_UPDATE_IMAGE_SAVE_PATH = "UPDATE " + DBHelper.TABLE_EXTRA_IMAGE
            + " SET "
            + DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH + " = ?, "
            + DBHelper.COLUMN_UPDATED + " = ? where "
            + DBHelper.COLUMN_MESSAGE_ID + " = ?";

    private static String SQL_UPDATE_AUDIO_SAVE_PATH = "UPDATE " + DBHelper.TABLE_EXTRA_IMAGE
            + " SET "
            + DBHelper.COLUMN_MESSAGE_EXTRA_AUDIO_SAVE_PATH + " = ?, "
            + DBHelper.COLUMN_UPDATED + " = ? where "
            + DBHelper.COLUMN_MESSAGE_ID + " = ?";

    private static String SQL_UPDATE_IMAGE_SAVE_PATH_URL = "UPDATE "
            + DBHelper.TABLE_EXTRA_IMAGE + " SET `"
            + DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH + "` = ?, `"
            + DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_URL + "` = ? , `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_MESSAGE_ID + "` = ?";

    private static String SQL_UPDATE_AUDIO_SAVE_PATH_URL = "UPDATE "
            + DBHelper.TABLE_EXTRA_AUDIO + " SET `"
            + DBHelper.COLUMN_MESSAGE_EXTRA_AUDIO_SAVE_PATH + "` = ?, `"
            + DBHelper.COLUMN_MESSAGE_EXTRA_AUDIO_URL + "` = ? , `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_MESSAGE_ID + "` = ?";

    private static String SQL_UPDATE_LOAD_STATUS_SINGLE = "UPDATE " + DBHelper.TABLE_MESSAGES
            + " SET `"
            + DBHelper.COLUMN_MESSAGE_STATUS + "` = ?, `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_MESSAGE_ID + "` = ?";

    private static String SQL_UPDATE_READ_STATUS_SINGLE = "UPDATE " + DBHelper.TABLE_MESSAGES
            + " SET `"
            + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` = ?, `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_MESSAGE_ID + "` = ?";

    private static String SQL_UPDATE_LOAD_STATUS_BEFORE = "UPDATE "
            + DBHelper.TABLE_MESSAGES + " SET `"
            + DBHelper.COLUMN_MESSAGE_STATUS + "` = ?, `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
            + DBHelper.COLUMN_RELATE_ID + "` = ? and "
            + DBHelper.COLUMN_MESSAGE_ID + " <= ?";

    private static String SQL_UPDATE_READ_STATUS_BEFORE = "UPDATE "
            + DBHelper.TABLE_MESSAGES + " SET `"
            + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` = ?, `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
            + DBHelper.COLUMN_RELATE_ID + "` = ? and "
            + DBHelper.COLUMN_MESSAGE_ID + " <= ?";

    private static String SQL_UPDATE_LOAD_STATUS_ALL = "UPDATE "
            + DBHelper.TABLE_MESSAGES + " SET `"
            + DBHelper.COLUMN_MESSAGE_STATUS + "status` = ?, `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
            + DBHelper.COLUMN_RELATE_ID + "` = ? and `"
            + DBHelper.COLUMN_MESSAGE_TO_USER_ID + "` = ? ";

    private static String SQL_UPDATE_READ_STATUS_ALL = "UPDATE "
            + DBHelper.TABLE_MESSAGES + " SET `"
            + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` = ?, `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
            + DBHelper.COLUMN_RELATE_ID + "` = ? and `"
            + DBHelper.COLUMN_MESSAGE_TO_USER_ID + "` = ? and `"
            + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` < ? ";

    private static String SQL_UPDATE_LOAD_STATUS_FROM_STATUS_ALL = "UPDATE "
            + DBHelper.TABLE_MESSAGES + " SET `"
            + DBHelper.COLUMN_MESSAGE_STATUS + "` = ?, `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
            + DBHelper.COLUMN_MESSAGE_STATUS + "` = ? ";

    private static String SQL_UPDATE_READ_STATUS_FROM_STATUS_ALL = "UPDATE "
            + DBHelper.TABLE_MESSAGES + " SET `"
            + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` = ?, `"
            + DBHelper.COLUMN_UPDATED + "` = ? where `"
            + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
            + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` = ? ";

    private static String SQL_QUERY_LAST_MSG_ID = "select "
            + DBHelper.COLUMN_MESSAGE_ID + " from "
            + DBHelper.TABLE_MESSAGES
            + DBHelper.LIMIT_ONE_MESSAGE_SUFFIX;

    private static String MSG_COUNT = "msgCount";
    private static String SQL_QUERY_MSG_COUNT = "SELECT COUNT(*)  AS "
            + MSG_COUNT + " FROM "
            + DBHelper.TABLE_MESSAGES;

    private static String SQL_QUERY_MSG_CREATE_TIME = "select " + DBHelper.COLUMN_CREATED
            + " from "
            + DBHelper.TABLE_MESSAGES + " where " + DBHelper.COLUMN_MESSAGE_ID + " = ";

    public static final int MESSAGE_LIMIT_NO = 10000; // 本地消息存储上限
    public static final int MESSAGE_LIMIT_DELETE_NO = 5000; // 若超出存储上限，一次删除消息条数
    private static DataModel instance;
    private static Context mContext = null;

    public static DataModel getInstance() {
        if (instance == null) {
            instance = new DataModel(IMEntrance.getInstance().getContext());
        }
        return instance;
    }

    /**
     * @author shuchen
     */
    private DataModel(Context context) {
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DataModel的步骤放在Activity的onCreate里
        helper = DBHelper.getInstance(context);
    }

    /**
     * 断开数据库
     * 
     * @return void
     */
    public void close() {
        // 断开本地数据库
        try {
            DBHelper.getInstance(mContext).close();
        } catch (SQLException e) {
            logger.e(e.getMessage());
        }
    }

    /* *********************************************************************
     * 新增数据
     * *********************************************************************
     */

    /**
     * 新增一条消息
     * 
     * @param msg 消息体
     * @return msgId 消息存储的唯一ID TODO 暂时用旧的实体类，不用子类强转，程序完成后DB再对应
     */
    public int add(MessageInfo msg) {

//        if (null == msg || null == msg.getMsgFromUserId()
//                || null == msg.getTargetId()
//                || SysConstant.DEFAULT_MESSAGE_ID == msg.getMsgId()) {
//            return SysConstant.DEFAULT_MESSAGE_ID;
//        }
//        int msgId = msg.getMsgId();
//        int dt = msg.getDisplayType();
//        Long timeNow = Long.valueOf(System.currentTimeMillis());
//        int created = (0 == msg.getCreated()) ? (int) (timeNow / 1000) : msg
//                .getCreated();
//        int updated = (0 == msg.getUpdated()) ? (int) (timeNow / 1000) : msg
//                .getUpdated();
//        msg.setCreated(created);
//        msg.setUpdated(updated);
//        SQLiteDatabase dbMaster = null;
//        try {
//            dbMaster = helper.getWritableDatabase();
//            dbMaster.beginTransaction(); // 每条消息分两张表存储，只能用事务来处理了
//
//            dbMaster.execSQL(
//                    DBHelper.INSERT_MESSAGE_SQL,
//                    new Object[] {
//                            msgId, msg.getMsgParentId(),
//                            msg.getOwnerId(), msg.getRelateId(),
//                            msg.getMsgFromUserId(), msg.getTargetId(),
//                            msg.getMsgType(), msg.getDisplayType(),
//                            msg.getMsgOverview(), msg.getMsgLoadState(),
//                            msg.getMsgReadStatus(), created, updated
//                    });
//
//            if (SysConstant.DISPLAY_TYPE_TEXT == dt) {
//                // 文本类消息
//                dbMaster.execSQL(DBHelper.INSERT_MESSAGE_EXTRA_TEXT,
//                        new Object[] {
//                                msgId, msg.getMsgContent(), created,
//                                updated
//                        });
//            } else if (SysConstant.DISPLAY_TYPE_IMAGE == dt) {
//                // 图片消息
//                dbMaster.execSQL(DBHelper.INSERT_MESSAGE_EXTRA_IMAGE,
//                        new Object[] {
//                                msgId, msg.getSavePath(), msg.getUrl(), created, updated
//                        });
//            } else if (SysConstant.DISPLAY_TYPE_AUDIO == dt) {
//                // 语音消息
//                dbMaster.execSQL(DBHelper.INSERT_MESSAGE_EXTRA_AUDIO,
//                        new Object[] {
//                                msgId, msg.getSavePath(), msg.getUrl(),
//                                msg.getPlayTime(), created, updated
//                        });
//            } else {
//                // 未知消息，为扩展做准备
//                msgId = SysConstant.DEFAULT_MESSAGE_ID; // 为defalutMsgId
//                                                        // -1，即未添加成功
//            }
//            dbMaster.setTransactionSuccessful();
//        } catch (SQLException e) {
//            logger.e(e.toString());
//        } finally {
//            if (null != dbMaster) {
//                dbMaster.endTransaction();
//            }
//            // dbMaster.close();
//        }
//
//        return msgId;
    	return 0;
    }

    /**
     * 新增一个用户
     * 
     * @param user
     * @return boolRtn
     */
    public Boolean add(User user) {
        Boolean boolRtn = false;
        if (null == user || null == user.getUserId()) {
            return boolRtn;
        }
        try {
            return this.add(user.getUserId(), user.getName(), user.getNickName(),
                    user.getAvatarUrl(), user.getCreated(), user.getUpdated());
        } catch (SQLException e) {
            logger.e(e.toString());
        }
        return boolRtn;
    }

    /**
     * 新增一个用户
     * 
     * @param userId 用户ID
     * @param uname 用户名
     * @param unick 用户昵称
     * @param avatar 用户头像
     * @param type 用户类型
     * @param created 用户存储时间
     * @param updated 用户更新时间
     * @return boolRtn
     */
    private Boolean add(String userId, String uname, String unick, String avatar,
            int created, int updated) {
        Boolean boolRtn = false;
        int timeNow = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        created = (0 == created) ? timeNow : created;
        updated = (0 == updated) ? timeNow : updated;
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.execSQL(DBHelper.INSERT_USER_SQL, new Object[] {
                    userId, uname, unick, avatar, created, updated
            });
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
        return boolRtn;
    }

    /**
     * 添加一条最近联系记录
     * 
     * @param imRecentContact 最近联系人记录
     * @return boolBtn
     */
    public Boolean add(IMRecentContact imRecentContact) {
        if (null == imRecentContact || null == imRecentContact.getUserId()) {
            return false;
        }
        return this.add(imRecentContact.getUserId(), imRecentContact.getUserId(),
                imRecentContact.getFriendUserId(), imRecentContact.getStatus(),
                imRecentContact.getCreated(), imRecentContact.getUpdated());
    }

    /**
     * 新增一条最近联系记录
     * 
     * @param ownerId 联系人所有者用户ID
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @param status 状态
     * @param created 创建时间
     * @param updated 更新时间
     * @return boolRtn
     */
    private Boolean add(String ownerId, String userId, String friendUserId,
            int status, int created, int updated) {
        Boolean boolRtn = false;
        if (null == ownerId || null == userId || null == friendUserId) {
            return boolRtn;
        }
        int timeNow = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        created = (0 == created) ? timeNow : created;
        updated = (0 == updated) ? timeNow : updated;
        SQLiteDatabase dbMaster = helper.getWritableDatabase();
        try {
            // 一次插入1条，userId小的在前面
            if (0 < userId.compareTo(friendUserId)) {
                dbMaster.execSQL(DBHelper.INSERT_CONTACT_SQL,
                        new Object[] {
                                ownerId, userId, friendUserId, status, created, updated
                        });
            } else {
                dbMaster.execSQL(DBHelper.INSERT_CONTACT_SQL,
                        new Object[] {
                                ownerId, friendUserId, userId, status, created, updated
                        });
            }
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return boolRtn;
    }

    /* *********************************************************************
     * 更新数据
     * *********************************************************************
     */

    /**
     * 更新图片存储路径
     * 
     * @param msgId 消息ID
     * @param status 消息状态
     * @return Boolean
     */
    public Boolean updateMsgImageSavePath(int msgId, String newPath) {
        return updateMsgSavePath(msgId, SysConstant.DISPLAY_TYPE_IMAGE, newPath);
    }

    /**
     * 更新语音存储路径
     * 
     * @param msgId 消息ID
     * @param status 消息状态
     * @return Boolean
     */
    public Boolean updateMsgAudioSavePath(int msgId, String newPath) {
        return updateMsgSavePath(msgId, SysConstant.DISPLAY_TYPE_AUDIO, newPath);
    }

    /**
     * 更新消息中语音或图片等本地文件存储路径
     * 
     * @param msgId 消息ID
     * @param displayType 消息展示类型 语音或图片
     * @param newPath 新的存储路径
     * @return Boolean
     */
    public Boolean updateMsgSavePath(int msgId, int displayType, String newPath) {
        Boolean boolRtn = false;
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            if (SysConstant.DISPLAY_TYPE_AUDIO == displayType) {
                dbMaster.execSQL(DataModel.SQL_UPDATE_AUDIO_SAVE_PATH, new Object[] {
                        newPath,
                        updated, msgId
                });
            } else {
                dbMaster.execSQL(DataModel.SQL_UPDATE_IMAGE_SAVE_PATH, new Object[] {
                        newPath,
                        updated, msgId
                });
            }
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return boolRtn;
    }

    /**
     * 更新某条消息的图片存储路径和链接
     * 
     * @param msgId 消息存储的唯一ID
     * @param savePath 图片保存文件路径
     * @param url 图片的URL
     * @param updated 消息更新时间
     * @return boolRtn 成功返回true，失败返回false
     */
    public Boolean updateImagePathUrlInfo(int msgId, String savePath, String url, int updated) {
        return updatePathUrlInfo(msgId, SysConstant.DISPLAY_TYPE_IMAGE, savePath, url, updated);
    }

    /**
     * 更新某条消息的语音存储路径和链接
     * 
     * @param msgId 消息存储的唯一ID
     * @param savePath 语音保存文件路径
     * @param url 语音的URL
     * @param updated 消息更新时间
     * @return boolRtn 成功返回true，失败返回false
     */
    protected Boolean updateAudioPathUrlInfo(int msgId, String savePath, String url, int updated) {
        return updatePathUrlInfo(msgId, SysConstant.DISPLAY_TYPE_AUDIO, savePath, url, updated);
    }

    /**
     * 更新某条消息的图片或语音存储路径和链接
     * 
     * @param msgId 消息存储的唯一ID
     * @param displayType 消息展示类型 语音或图片
     * @param savePath 语音保存文件路径
     * @param url 语音的URL
     * @param updated 消息更新时间
     * @return boolRtn 成功返回true，失败返回false
     */
    protected Boolean updatePathUrlInfo(int msgId, int displayType, String savePath,
            String url, int updated) {
        Boolean boolRtn = false;
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return boolRtn;
        }
        updated = (0 == updated) ? (int) (Long.valueOf(System
                .currentTimeMillis()) / 1000) : updated;
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 语音信息存在ExtraAudio标中，单表更新即可
            if (SysConstant.DISPLAY_TYPE_AUDIO == displayType) {
                dbMaster.execSQL(DataModel.SQL_UPDATE_AUDIO_SAVE_PATH_URL,
                        new Object[] {
                                savePath, url, updated, msgId
                        });
            } else {
                dbMaster.execSQL(DataModel.SQL_UPDATE_IMAGE_SAVE_PATH_URL,
                        new Object[] {
                                savePath, url, updated, msgId
                        });
            }
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return boolRtn;
    }

    /**
     * 更新单条消息状态
     * 
     * @param msgId 消息ID
     * @param status 消息状态
     * @param statusType 消息状态类型 0:加载状态 1：已读状态
     * @return Boolean
     */
    public Boolean updateMsgStatusByMsgId(int msgId, int status, int statusType) {
        Boolean boolRtn = false;
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            if (0 == statusType) {
                dbMaster.execSQL(DataModel.SQL_UPDATE_LOAD_STATUS_SINGLE, new Object[] {
                        status, updated, msgId
                });
            } else {
                dbMaster.execSQL(DataModel.SQL_UPDATE_READ_STATUS_SINGLE, new Object[] {
                        status, updated, msgId
                });
            }
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return boolRtn;
    }

    /**
     * 更新两个用户之间的某条消息之前（含某条消息）的所有消息状态
     * 
     * @param ownerId 消息所有者ID
     * @param msgId 消息ID，做分界用
     * @param relateId 两个用户之间的联系ID
     * @param status 消息状态
     * @param statusType 消息状态类型 0:加载状态 1：已读状态
     * @return Boolean
     */
    public Boolean updateMsgStatus(String ownerId, int msgId, int relateId,
            int status, int statusType) {
        Boolean boolRtn = false;
        if (null == ownerId || 0 == msgId || 0 == relateId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            if (0 == statusType) {
                dbMaster.execSQL(DataModel.SQL_UPDATE_LOAD_STATUS_BEFORE, new Object[] {
                        status, updated,
                        ownerId, relateId, msgId
                });
            } else {
                dbMaster.execSQL(DataModel.SQL_UPDATE_READ_STATUS_BEFORE, new Object[] {
                        status, updated,
                        ownerId, relateId, msgId
                });
            }
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return boolRtn;
    }

    /**
     * 更新两个用户之间的所有收到的消息状态(加载状态)
     * 
     * @param userId 当前登入用户ID
     * @param relateId 两个用户之间的联系ID
     * @param status 消息状态
     * @param statusType 消息状态类型 0:加载状态 1：已读状态
     * @return Boolean
     */
    public Boolean updateAllMsgStatus(String userId, int relateId, int status, int statusType) {
        Boolean boolRtn = false;
        if (null == userId || 0 == relateId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            if (0 == statusType) {
                dbMaster.execSQL(DataModel.SQL_UPDATE_LOAD_STATUS_ALL, new Object[] {
                        status, updated, userId,
                        relateId, userId
                });
            } else {
                dbMaster.execSQL(DataModel.SQL_UPDATE_READ_STATUS_ALL, new Object[] {
                        status, updated, userId,
                        relateId, userId, status
                });
            }
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return boolRtn;
    }

    /**
     * 更新某用户的所有消息状态从A到B
     * 
     * @param userId 当前登入用户ID
     * @param status 新的消息状态
     * @param oldStatus 旧的消息状态
     * @param statusType 消息状态类型 0:加载状态 1：已读状态
     * @return Boolean
     */
    public Boolean updateAllMsgStatusFromStatus(String userId, int status, int oldStatus,
            int statusType) {
        Boolean boolRtn = false;
        if (null == userId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            if (0 == statusType) {
                dbMaster.execSQL(DataModel.SQL_UPDATE_LOAD_STATUS_FROM_STATUS_ALL,
                        new Object[] {
                                status, updated, userId, oldStatus
                        });
            } else {
                dbMaster.execSQL(DataModel.SQL_UPDATE_READ_STATUS_FROM_STATUS_ALL,
                        new Object[] {
                                status, updated, userId, oldStatus
                        });
            }
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return boolRtn;
    }

    /**
     * update user's info 更新用户或添加用户
     * 
     * @param user
     */
    public Boolean update(User user) {
        return this.update(user, false);
    }

    /**
     * update user's info 更新用户或添加用户
     * 
     * @param user
     */
    public Boolean update(User user, Boolean forced) {
        if (null == user || null == user.getUserId()) {
            return false;
        }
        User oldUser = this.queryUserByUserId(user.getUserId());
        if (null == oldUser) {// 没有则添加
            this.add(user);
            return true;
        }

        int cid;
        if (forced) { // 如果强制更新则强制更新
            cid = this.force2update(user);
            if (0 < cid) {
                return true;
            }
            return false;
        }

        if (oldUser.getName().equals(user.getName())
                && oldUser.getAvatarUrl().equals(user.getAvatarUrl())) {
            return true; // 存在且相同直接返回
        }

        cid = this.force2update(user); // 存在但不相同，则更新
        if (0 < cid) {
            return true;
        }
        return false;
    }

    /**
     * 强制更新用户信息
     * 
     * @param user
     */
    private int force2update(User user) {
        int cid = 0;
        if (null == user || null == user.getUserId()) {
            return cid;
        }
        Long timeNow = Long.valueOf(System.currentTimeMillis());
        SQLiteDatabase dbMaster;
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.COLUMN_USER_ID, user.getUserId());
        cv.put(DBHelper.COLUMN_USER_NAME, user.getName());
        cv.put(DBHelper.COLUMN_USER_NICKNAME, user.getNickName());
        cv.put(DBHelper.COLUMN_USER_AVATAR, user.getAvatarUrl());
        cv.put(DBHelper.COLUMN_UPDATED, timeNow);
        try {
            dbMaster = helper.getWritableDatabase();
            cid = dbMaster.update(DBHelper.TABLE_USERS, cv,
                    DBHelper.COLUMN_USER_ID + " = ? and "
                            + DBHelper.COLUMN_USER_NAME + " = ? and "
                            + DBHelper.COLUMN_USER_NICKNAME + " = ? and "
                            + DBHelper.COLUMN_USER_AVATAR + " = ?  and "
                            + DBHelper.COLUMN_UPDATED + " = ? ",
                    new String[] {
                            user.getUserId(),
                            user.getName(),
                            user.getNickName(),
                            user.getAvatarUrl(),
                            String.valueOf(user.getUpdated())
                    });
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
        return cid;
    }

    /* *********************************************************************
     * 查询数据
     * *********************************************************************
     */

    /**
     * 查询DB中最后一条消息ID
     * 
     * @return msgId
     */
    public int queryLastMsgId() {
        int lastMsgId = SysConstant.DEFAULT_MESSAGE_ID;
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            c = dbSlaver.rawQuery(DataModel.SQL_QUERY_LAST_MSG_ID, null);
            if (c.moveToFirst()) {
                lastMsgId = c.getInt(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_ID));
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

    /**
     * 查询与所有好友的最后一条聊天信息(不含Extra信息)
     * 
     * @return List<MessageInfo>
     */
    @SuppressLint("UseSparseArrays")
    public Map<Integer, MessageInfo> queryAllLastMsg(String ownerId) {

        HashMap<Integer, MessageInfo> msgHM = new HashMap<Integer, MessageInfo>();
        if (null == ownerId) {
            return msgHM;
        }
        SQLiteDatabase dbSlaver = null;
        Cursor c = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            String selectSql = "select * from "
                    + DBHelper.TABLE_MESSAGES + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "' and "
                    + DBHelper.COLUMN_RELATE_ID + " in (select "
                    + DBHelper.COLUMN_RELATE_ID + " from "
                    + DBHelper.TABLE_CONTACTS + ") group by "
                    + DBHelper.COLUMN_RELATE_ID + " order by "
                    + DBHelper.COLUMN_CREATED + " desc , "
                    + DBHelper.COLUMN_MESSAGE_ID + " desc ";
            c = dbSlaver.rawQuery(selectSql, null);
            MessageInfo msg = null;
            while (c.moveToNext()) {
                msg = setMsgBaseInfo(c);
                msgHM.put(msg.getRelateId(), msg);
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return msgHM;
    }

    /**
     * 查询两个用户之间的最后一条消息(不含Extra部分)
     * 
     * @param relateId 两个用户之间的联系ID
     * @return MessageInfo
     */
    public MessageInfo queryLastMsgWithoutExtraByRelateId(String ownerId, int relateId) {
        MessageInfo msg = null;
        if (null == ownerId || 0 == relateId) {
            return msg;
        }
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            String selectSql = "SELECT * FROM " + DBHelper.TABLE_MESSAGES + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "' and "
                    + DBHelper.COLUMN_RELATE_ID + " = " + relateId + " "
                    + DBHelper.LIMIT_ONE_MESSAGE_SUFFIX;
            c = dbSlaver.rawQuery(selectSql, null);
            while (c.moveToNext()) {
                msg = setMsgBaseInfo(c);
                break; // 只有一条，设置完后即退出循环
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return msg;
    }

    /**
     * 根据消息ID查询两个用户之间的某条消息详情(含Extra信息)
     * 
     * @param msgId 消息ID
     * @return MessageInfo
     */
    public MessageInfo queryMsgWithExtraByMsgId(int msgId) {
        MessageInfo msg = null;
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return msg;
        }
        Cursor c = null;
        Cursor ec = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            String querySql = "SELECT * FROM " + DBHelper.TABLE_MESSAGES + " where "
                    + DBHelper.COLUMN_MESSAGE_ID + " = " + msgId;
            c = dbSlaver.rawQuery(querySql, null);
            String queryExtraSql = "";
            while (c.moveToNext()) {
                msg = setMsgBaseInfo(c);
                // 根据消息类型完善消息具体内容
                switch (msg.getDisplayType()) {
                    case SysConstant.DISPLAY_TYPE_TEXT:
                        queryExtraSql = "SELECT * FROM " + DBHelper.TABLE_EXTRA_TEXT + " where "
                                + DBHelper.COLUMN_MESSAGE_ID + " = " + msgId;
                        break;
                    case SysConstant.DISPLAY_TYPE_IMAGE:
                        queryExtraSql = "SELECT * FROM " + DBHelper.TABLE_EXTRA_IMAGE + " where "
                                + DBHelper.COLUMN_MESSAGE_ID + " = " + msgId;
                        break;
                    case SysConstant.DISPLAY_TYPE_AUDIO:
                        queryExtraSql = "SELECT * FROM " + DBHelper.TABLE_EXTRA_AUDIO + " where "
                                + DBHelper.COLUMN_MESSAGE_ID + " = " + msgId;
                        break;
                    default:
                        // 未知消息，为扩展做准备, 暂时用文本消息替代，留个坑，不知道会不会害人
                        queryExtraSql = "SELECT * FROM " + DBHelper.TABLE_EXTRA_TEXT + " where "
                                + DBHelper.COLUMN_MESSAGE_ID + " = " + msgId;
                        break;
                }
                ec = dbSlaver.rawQuery(queryExtraSql, null);
                msg = this.setMsgExtraInfo(msg.getDisplayType(), msg, ec);
                break;
            }
            if (null != ec) {
                ec.close();
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return msg;
    }

    /**
     * 提供给拉取历史消息使用 查询两个用户之间的历史消息
     * 
     * @param relateId 两个用户之间的联系ID
     * @param msgId 起始点消息ID,为0时则从最新一条消息开始
     * @param offset 距离msgId的偏移量
     * @param size 取得的消息条数
     * @return List<MessageInfo>
     */
    public List<MessageInfo> queryHistoryMsg(String ownerId, int relateId,
            int msgId, int offset, int size) {
//        List<MessageInfo> msgList = new ArrayList<MessageInfo>();
//        if (null == ownerId || 0 == relateId) {
//            return msgList;
//        }
//        Cursor c = null;
//        Cursor ec = null;
//        SQLiteDatabase dbSlaver = null;
//        try {
//            dbSlaver = helper.getReadableDatabase();
//            String msgSql;
//            String msgExtraSql;
//            if (0 < msgId) {
//                msgSql = "SELECT * FROM " + DBHelper.TABLE_MESSAGES + " where "
//                        + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "' and "
//                        + DBHelper.COLUMN_RELATE_ID + " = " + relateId + " and "
//                        + DBHelper.COLUMN_MESSAGE_ID + " < " + msgId + " order by "
//                        + DBHelper.COLUMN_MESSAGE_ID + " desc, "
//                        + DBHelper.COLUMN_CREATED + " desc limit " + offset + ", " + size;
//            } else {
//                msgSql = "SELECT * FROM " + DBHelper.TABLE_MESSAGES + " where "
//                        + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "' and "
//                        + DBHelper.COLUMN_RELATE_ID + " = " + relateId + " order by "
//                        + DBHelper.COLUMN_MESSAGE_ID + " desc, "
//                        + DBHelper.COLUMN_CREATED + " desc limit " + offset + ", " + size;
//            }
//            c = dbSlaver.rawQuery(msgSql, null);
//            MessageInfo msg = null;
//            while (c.moveToNext()) {
//                msg = setMsgBaseInfo(c);
//                int dt = msg.getDisplayType();
//                // 根据消息类型完善消息具体内容
//                if (SysConstant.DISPLAY_TYPE_TEXT == dt) {
//                    // 文本类消息
//                    msgExtraSql = "SELECT * FROM " + DBHelper.TABLE_EXTRA_TEXT + " where "
//                            + DBHelper.COLUMN_MESSAGE_ID + " = " + msg.getMsgId();
//                } else if (SysConstant.DISPLAY_TYPE_IMAGE == dt) {
//                    // 图片消息
//                    msgExtraSql = "SELECT * FROM " + DBHelper.TABLE_EXTRA_IMAGE + " where "
//                            + DBHelper.COLUMN_MESSAGE_ID + " = " + msg.getMsgId();
//                } else if (SysConstant.DISPLAY_TYPE_AUDIO == dt) {
//                    // 语音消息
//                    msgExtraSql = "SELECT * FROM " + DBHelper.TABLE_EXTRA_AUDIO + " where "
//                            + DBHelper.COLUMN_MESSAGE_ID + " = " + msg.getMsgId();
//                } else {
//                    // 未知消息，为扩展做准备, 暂时用文本消息替代，留个坑，不知道会不会害人
//                    msgExtraSql = "SELECT * FROM " + DBHelper.TABLE_EXTRA_TEXT + " where "
//                            + DBHelper.COLUMN_MESSAGE_ID + " = " + msg.getMsgId();
//                }
//                ec = dbSlaver.rawQuery(msgExtraSql, null);
//                setMsgExtraInfo(dt, msg, ec);
//                msgList.add(0, msg); // 插在前面，给个正序List
//                if (ec != null)
//                {
//                    ec.close();
//                }
//            }
//
//        } catch (SQLException e) {
//            logger.e(e.toString());
//        } finally {
//            if (null != c) {
//                c.close();
//            }
//            if (null != ec) {
//                ec.close();
//            }
//            // dbSlaver.close();
//        }
//        return msgList;
    	return null;
    }

    /**
     * 查询本地存储的消息条数
     * 
     * @return msgCount
     */
    public int queryMsgCount() {
        int msgCount = 0;
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            c = dbSlaver.rawQuery(DataModel.SQL_QUERY_MSG_COUNT, null);
            while (c.moveToNext()) {
                msgCount = c.getInt(c.getColumnIndex(DataModel.MSG_COUNT));
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return msgCount;
    }

    /**
     * 查询DB中某条消息的创建时间
     * 
     * @return msgId
     */
    private int queryMsgCreatedTime(int msgId) {
        int createdTime = 0;
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            c = dbSlaver.rawQuery(DataModel.SQL_QUERY_MSG_CREATE_TIME + msgId, null);
            if (c.moveToFirst()) {
                createdTime = c.getInt(0);
            }
        } catch (SQLException e) {
//            Logger.getLogger().e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close(); //这里不能关闭哦，因为是在其它DB语句中间操作的，由其它DB操作部分关闭即可
        }

        return createdTime;
    }

    /**
     * 提供给联系人列表中各个联系人未读消息计数提示 查询与某个用户之间的未读计数
     * 
     * @param relateId 两个用户之间的联系ID
     * @return int
     */
    public int queryUnreadCountByRelateId(String ownerId, int relateId) {
        int unreadCount = 0;
        if (null == ownerId || 0 == relateId) {
            return unreadCount;
        }
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            String selectSql = "SELECT COUNT(*)  AS unread FROM "
                    + DBHelper.TABLE_MESSAGES + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "' and "
                    + DBHelper.COLUMN_RELATE_ID + " = " + relateId + " and "
                    + DBHelper.COLUMN_MESSAGE_TO_USER_ID + " = '" + ownerId + "' and "
                    + DBHelper.COLUMN_MESSAGE_READ_STATUS + " = " + SysConstant.MESSAGE_UNREAD;
            c = dbSlaver.rawQuery(selectSql, null);
            while (c.moveToNext()) {
                unreadCount = c.getInt(c.getColumnIndex("unread"));
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return unreadCount;
    }

    /**
     * 暂时未被调用 查询某用户(主要是当前登入用户)所有未读消息计数
     * 
     * @param userId 用户ID
     * @return unreadCount
     */
    public int queryUnreadTotalCountByUserId(String userId) {
        int unreadCount = 0;
        if (TextUtils.isEmpty(userId)) {
            return unreadCount;
        }
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            String querySql = "SELECT COUNT(*)  AS unread FROM "
                    + DBHelper.TABLE_MESSAGES + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + userId + "' and "
                    + DBHelper.COLUMN_MESSAGE_TO_USER_ID + " = '" + userId + "' and "
                    + DBHelper.COLUMN_MESSAGE_READ_STATUS + " = " + SysConstant.MESSAGE_UNREAD;
            c = dbSlaver.rawQuery(querySql, null);
            while (c.moveToNext()) {
                unreadCount = c.getInt(c.getColumnIndex("unread"));
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return unreadCount;
    }

    /**
     * 查询当前登入用户除某用户(主要是当前聊天对象)之外的所有未读消息计数
     * 
     * @param userId 用户ID
     * @param exclUserId
     * @return int
     */
    public int queryUnreadTotalCountExclUserId(String userId, String exclUserId) {
        int unreadCount = 0;
        if (TextUtils.isEmpty(userId) || null == exclUserId) {
            return unreadCount;
        }
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            String sql = "SELECT COUNT(*)  AS unread FROM " + DBHelper.TABLE_MESSAGES + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + userId + "' and "
                    + DBHelper.COLUMN_MESSAGE_TO_USER_ID + " = '" + userId + "' and "
                    + DBHelper.COLUMN_MESSAGE_FROM_USER_ID + " = '" + exclUserId + "' and "
                    + DBHelper.COLUMN_MESSAGE_READ_STATUS + " = " + SysConstant.MESSAGE_UNREAD;
            c = dbSlaver.rawQuery(sql, null);
            while (c.moveToNext()) {
                unreadCount = c.getInt(c.getColumnIndex("unread"));
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return unreadCount;
    }

    /**
     * 查询所有用户
     * 
     * @return List<User>
     */
    public List<User> queryAllUsers() {
        ArrayList<User> users = new ArrayList<User>();
        Cursor c = null;
        SQLiteDatabase dbSlaver;
        try {
            dbSlaver = helper.getReadableDatabase();
            c = dbSlaver.rawQuery(DBHelper.SELECT_ALL_USER_SQL, null);
            User user = null;
            while (c.moveToNext()) {
                user = setUserInfo(c);
                users.add(user);
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return users;
    }

    /**
     * 查询一个用户
     * 
     * @param userId 用户ID
     * @return User | null
     */
    public User queryUserByUserId(String userId) {
        User user = null;
        if ("".equals(userId.trim())) {
            return user;
        }
        SQLiteDatabase dbSlaver;
        Cursor c = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            c = dbSlaver.rawQuery(DBHelper.SELECT_ALL_USER_SQL + " where "
                    + DBHelper.COLUMN_USER_ID + " = '" + userId + "'", null);
            while (c.moveToNext()) {
                user = this.setUserInfo(c);
                break;
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return user;
    }

    /**
     * 获得两个用户之间的联系ID，没有则新增一个
     * 
     * @param ownerId 拥有者用户ID
     * @param userId 用户Id
     * @param friendUserId 好友Id
     * @return Cursor
     */
    public int getRelateId(String ownerId, String userId, String friendUserId) {
        if ("".equals(ownerId) || null == ownerId || null == userId || null == friendUserId) {
            return 0;
        }
        // 获得relateId
        int relateId = queryRelateId(ownerId, userId, friendUserId);
        if (0 == relateId) {
            int timeNow = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
            this.add(ownerId, userId, friendUserId, 0, timeNow, timeNow); // 不存在则新加入一条
            relateId = queryRelateId(ownerId, userId, friendUserId);
        }
        return relateId;
    }

    /**
     * 查询两个用户之间的联系ID
     * 
     * @param userId 用户Id 一定要当前登录用户ID
     * @param friendUserId 好友Id
     * @return relateId
     */
    protected int queryRelateId(String ownerId, String userId, String friendUserId) {
        // 查询relateId
        int relateId = 0;
        if (null == ownerId || null == userId || null == friendUserId) {
            return relateId;
        }
        Cursor c = null;
        SQLiteDatabase dbSlaver = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            // 查询两个用户之间的联系ID，以ID小的为准
            c = dbSlaver.rawQuery(DBHelper.SELECT_ALL_CONTACT_SQL + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "' and  ("
                    + DBHelper.COLUMN_USER_ID + " = '" + userId + "' and "
                    + DBHelper.COLUMN_FRIEND_USER_ID + " = '" + friendUserId
                    + "' ) or ("
                    + DBHelper.COLUMN_USER_ID + " = '" + friendUserId + "' and "
                    + DBHelper.COLUMN_FRIEND_USER_ID + " = '" + userId + "') "
                    + DBHelper.LIMIT_ONE_CONTACT_SUFFIX, null);
            // 遍历取得relateId，虽然最多只有一条记录
            while (c.moveToNext()) {
                relateId = c.getInt(c.getColumnIndex(DBHelper.COLUMN_RELATE_ID));
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return relateId;
    }

    /**
     * 查询所有联系列表
     * 
     * @return List<IMRecentContact>
     */
    public List<IMRecentContact> queryAllContacts() {
        ArrayList<IMRecentContact> imRecentContacts = new ArrayList<IMRecentContact>();
        SQLiteDatabase dbSlaver = null;
        Cursor c = null;
        try {
            dbSlaver = helper.getReadableDatabase();
            c = dbSlaver.rawQuery(DBHelper.SELECT_ALL_CONTACT_SQL, null);
            IMRecentContact imRecentContact = null;
            while (c.moveToNext()) {
                imRecentContact = setContactInfo(c);
                imRecentContacts.add(imRecentContact);
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }

        return imRecentContacts;
    }

    /**
     * 查询所有最近联系人的用户ID
     * 
     * @param ownerId 联系人拥有者ID
     * @return List<String>
     */
    public List<String> queryAllFriendUserId(String ownerId) {
        ArrayList<String> users = new ArrayList<String>();
        if (null == ownerId) {
            return users;
        }
        Cursor c = null;
        SQLiteDatabase dbSlaver = helper.getReadableDatabase();
        try {
            String sql = DBHelper.SELECT_ALL_FRIEND_USERID_SQL + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "'";
            c = dbSlaver.rawQuery(sql, null);
            String fromUserId;
            String toUserId;
            while (c.moveToNext()) {
                fromUserId = c.getString(c.getColumnIndex(DBHelper.COLUMN_USER_ID));
                toUserId = c.getString(c.getColumnIndex(DBHelper.COLUMN_FRIEND_USER_ID));

                if (!ownerId.equals(fromUserId)) {
                    users.add(fromUserId); // 判断不是自己则是好友
                } else {
                    if (!ownerId.equals(toUserId)) {
                        users.add(toUserId); // 判断不是自己则是好友
                    }
                }
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != c) {
                c.close();
            }
            // dbSlaver.close();
        }
        return users;
    }

    /* *********************************************************************
     * 删除数据
     * *********************************************************************
     */

    /**
     * 删除一条消息
     * 
     * @param msg
     * @return Boolean
     */
    public Boolean delete(MessageInfo msg) {
//        if (null != msg && SysConstant.DEFAULT_MESSAGE_ID != msg.getMsgId()) {
//            return deleteMsg(msg.getMsgId(), msg.getDisplayType());
//        }
        return false;
    }

    /**
     * 删除一条消息
     * 
     * @param msg
     * @return Boolean
     */
    public Boolean deleteMsg(int msgId, int displayType) {
        Boolean boolRtn = false;
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return boolRtn;
        }

        SQLiteDatabase dbMaster = null;
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.beginTransaction();
            dbMaster.delete(DBHelper.TABLE_MESSAGES, ""
                    + DBHelper.COLUMN_MESSAGE_ID + " == ?",
                    new String[] {
                        String.valueOf(msgId)
                    });
            if (SysConstant.DISPLAY_TYPE_TEXT == displayType) {
                // 文本类消息
                dbMaster.delete(DBHelper.TABLE_EXTRA_TEXT, ""
                        + DBHelper.COLUMN_MESSAGE_ID + " == ?",
                        new String[] {
                            String.valueOf(msgId)
                        });
            } else if (SysConstant.DISPLAY_TYPE_IMAGE == displayType) {
                // 图片消息
                dbMaster.delete(DBHelper.TABLE_EXTRA_IMAGE, ""
                        + DBHelper.COLUMN_MESSAGE_ID + " == ?",
                        new String[] {
                            String.valueOf(msgId)
                        });
            } else if (SysConstant.DISPLAY_TYPE_AUDIO == displayType) {
                // 语音消息
                dbMaster.delete(DBHelper.TABLE_EXTRA_AUDIO, ""
                        + DBHelper.COLUMN_MESSAGE_ID + " == ?",
                        new String[] {
                            String.valueOf(msgId)
                        });
            } else {
            }

            dbMaster.setTransactionSuccessful();
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != dbMaster) {
                dbMaster.endTransaction();
            }
        }

        return boolRtn;
    }

    /**
     * 删除一个用户
     * 
     * @param user
     */
    public Boolean delete(User user) {
        Boolean boolRtn = false;
        if (null == user || null == user.getUserId()) {
            return boolRtn;
        }
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.delete(DBHelper.TABLE_USERS, DBHelper.COLUMN_USER_ID + " == ?",
                    new String[] {
                        user.getUserId()
                    });
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
        return boolRtn;
    }

    /**
     * 删除一条最近联系记录
     * 
     * @param imRecentContact return boolRtn
     */
    public Boolean delete(IMRecentContact imRecentContact) {
        Boolean boolRtn = false;
        if (null == imRecentContact || null == imRecentContact.getUserId()) {
            return boolRtn;
        }
        SQLiteDatabase dbMaster = null;
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.delete(DBHelper.TABLE_CONTACTS, DBHelper.COLUMN_RELATE_ID + " == ?",
                    new String[] {
                        String.valueOf(imRecentContact.getRelateId())
                    });
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
        return boolRtn;
    }

    /*
     * 如果需要，删除过旧的历史消息
     * @return 返回删除消息的时间分隔线
     */
    public int checkAndDeleteIfNeed() {
        if (MESSAGE_LIMIT_NO < queryMsgCount()) {
            int lastMsgId = queryLastMsgId();
            if (0 < lastMsgId - MESSAGE_LIMIT_DELETE_NO) {
                int createdTime = queryMsgCreatedTime(lastMsgId - MESSAGE_LIMIT_DELETE_NO);
                if (0 < createdTime) {
                    deleteIfTooOld(createdTime);
                    return createdTime;
                }
            }
        }
        return 0;
    }

    /**
     * 删除早于某个时间的所有消息
     * 
     * @param msg
     * @return Boolean
     */
    private Boolean deleteIfTooOld(int createdTime) {
        Boolean boolRtn = false;
        if (0 >= createdTime) {
            return boolRtn;
        }
        SQLiteDatabase dbMaster = null;
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.beginTransaction();
            // 主消息
            dbMaster.delete(DBHelper.TABLE_MESSAGES, ""
                    + DBHelper.COLUMN_CREATED + " < ?",
                    new String[] {
                        String.valueOf(createdTime)
                    });
            // 文本类消息
            dbMaster.delete(DBHelper.TABLE_EXTRA_TEXT, ""
                    + DBHelper.COLUMN_CREATED + " < ?",
                    new String[] {
                        String.valueOf(createdTime)
                    });
            // 图片消息
            dbMaster.delete(DBHelper.TABLE_EXTRA_IMAGE, ""
                    + DBHelper.COLUMN_CREATED + " < ?",
                    new String[] {
                        String.valueOf(createdTime)
                    });
            // 语音消息
            dbMaster.delete(DBHelper.TABLE_EXTRA_AUDIO, ""
                    + DBHelper.COLUMN_CREATED + " < ?",
                    new String[] {
                        String.valueOf(createdTime)
                    });

            dbMaster.setTransactionSuccessful();
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            if (null != dbMaster) {
                dbMaster.endTransaction();
            }
            // dbMaster.close();
        }

        return boolRtn;
    }

    /*
     * 设置消息的共通部分
     */
    private MessageInfo setMsgBaseInfo(Cursor c) {
        int displayType = c.getInt(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_DISPLAY_TYPE));
        MessageInfo ojMsg = null;
        if (SysConstant.DISPLAY_TYPE_TEXT == displayType) {
            // 文本类消息
            ojMsg = new MessageInfo(); // 每次都新建一个对象
        } else if (SysConstant.DISPLAY_TYPE_IMAGE == displayType) {
            // 图片消息
            ojMsg = new MessageInfo(); // 每次都新建一个对象
        } else if (SysConstant.DISPLAY_TYPE_AUDIO == displayType) {
            // 语音消息
            ojMsg = new MessageInfo(); // 每次都新建一个对象
        } else {
            // 未知消息，为扩展做准备
            ojMsg = new MessageInfo(); // 每次都新建一个对象
        }

        //ojMsg.setMsgId(c.getInt(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_ID)));
        ojMsg.setMsgParentId(c.getInt(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_PARENT_ID)));
        ojMsg.setRelateId(c.getInt(c.getColumnIndex(DBHelper.COLUMN_RELATE_ID)));
        ojMsg.setMsgFromUserId(c.getString(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_FROM_USER_ID)));
        ojMsg.setTargetId(c.getString(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_TO_USER_ID)));
        ojMsg.setMsgOverview(c.getString(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_OVERVIEW)));
        ojMsg.setMsgType((byte) c.getInt(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_TYPE)));
        ojMsg.setDisplayType(c.getInt(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_DISPLAY_TYPE)));
        int loadStatus = c.getInt(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_STATUS));
        ojMsg.setMsgLoadState(loadStatus);
        ojMsg.setMsgReadStatus(c.getInt(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_READ_STATUS)));
        ojMsg.setCreated(c.getInt(c.getColumnIndex(DBHelper.COLUMN_CREATED)));
        ojMsg.setUpdated(c.getInt(c.getColumnIndex(DBHelper.COLUMN_UPDATED)));

        return ojMsg;
    }

    /*
     * 根据消息展示类型设置消息对象
     */
    private MessageInfo setMsgExtraInfo(int displayType, MessageInfo ojMsg,
            Cursor c) {
        if (SysConstant.DISPLAY_TYPE_TEXT == displayType) {
            // 文本类消息
            setMsgExtraTextInfo(ojMsg, c);
        } else if (SysConstant.DISPLAY_TYPE_IMAGE == displayType) {
            // 图片消息
            setMsgExtraImageInfo(ojMsg, c);
        } else if (SysConstant.DISPLAY_TYPE_AUDIO == displayType) {
            // 语音消息
            setMsgExtraAudioInfo(ojMsg, c);
        } else {
        }
        return ojMsg;
    }

    /*
     * 设置文本消息特有字段
     */
    private MessageInfo setMsgExtraTextInfo(MessageInfo ojMsg, Cursor c) {
        while (c.moveToNext()) {
            ojMsg.setMsgContent(c.getString(c
                    .getColumnIndex(DBHelper.COLUMN_MESSAGE_EXTRA_TEXT_CONTENT)));
        }
        return ojMsg;
    }

    /*
     * 设置图片消息特有字段
     */
    private MessageInfo setMsgExtraImageInfo(MessageInfo ojMsg, Cursor c) {
        while (c.moveToNext()) {
            ojMsg.setSavePath(c.getString(c
                    .getColumnIndex(DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH)));
            ojMsg.setUrl(c.getString(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_URL)));
        }
        return ojMsg;
    }

    /*
     * 设置语音消息特有字段
     */
    private MessageInfo setMsgExtraAudioInfo(MessageInfo ojMsg, Cursor c) {
        while (c.moveToNext()) {
            ojMsg.setSavePath(c.getString(c
                    .getColumnIndex(DBHelper.COLUMN_MESSAGE_EXTRA_AUDIO_SAVE_PATH)));
            ojMsg.setUrl(c.getString(c.getColumnIndex(DBHelper.COLUMN_MESSAGE_EXTRA_AUDIO_URL)));
            ojMsg.setPlayTime(c.getInt(c
                    .getColumnIndex(DBHelper.COLUMN_MESSAGE_EXTRA_AUDIO_PLAY_TIME)));
        }
        return ojMsg;
    }

    /*
     * 设置用户信息
     */
    private User setUserInfo(Cursor c) {
        User ojUser = new User(); // 每次都新建一个对象
        ojUser.setUserId(c.getString(c.getColumnIndex(DBHelper.COLUMN_USER_ID)));
        ojUser.setName(c.getString(c.getColumnIndex(DBHelper.COLUMN_USER_NAME)));
        ojUser.setNickName(c.getString(c.getColumnIndex(DBHelper.COLUMN_USER_NICKNAME)));
        ojUser.setAvatarUrl(c.getString(c.getColumnIndex(DBHelper.COLUMN_USER_AVATAR)));
        ojUser.setCreated(c.getInt(c.getColumnIndex(DBHelper.COLUMN_CREATED)));
        ojUser.setUpdated(c.getInt(c.getColumnIndex(DBHelper.COLUMN_UPDATED)));

        return ojUser;
    }

    private IMRecentContact setContactInfo(Cursor c) {
        IMRecentContact ojContact = new IMRecentContact(); // 每次都新建一个对象
        ojContact.setRelateId(c.getInt(c.getColumnIndex(DBHelper.COLUMN_RELATE_ID)));
        ojContact.setOwnerId(c.getString(c.getColumnIndex(DBHelper.COLUMN_OWNER_ID)));
        ojContact.setUserId(c.getString(c.getColumnIndex(DBHelper.COLUMN_USER_ID)));
        ojContact.setFriendUserId(c.getString(c.getColumnIndex(DBHelper.COLUMN_FRIEND_USER_ID)));
        ojContact.setStatus(c.getInt(c.getColumnIndex(DBHelper.COLUMN_STATUS)));
        ojContact.setCreated(c.getInt(c.getColumnIndex(DBHelper.COLUMN_CREATED)));
        ojContact.setUpdated(c.getInt(c.getColumnIndex(DBHelper.COLUMN_UPDATED)));

        return ojContact;
    }

}
