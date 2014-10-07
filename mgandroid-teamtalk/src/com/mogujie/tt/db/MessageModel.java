/**
 * DB中IMMessage表操作类
 * @author shuchen
 */

package com.mogujie.tt.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.entity.MessageInfo;
import com.mogujie.tt.log.Logger;

/**
 * 
 */
public class MessageModel {
    private DBHelper helper;
    private Logger logger = Logger.getLogger(MessageModel.class);

    /**
     * @author shuchen
     */
    public MessageModel(Context context) {
        helper = DBHelper.getInstance(context);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
    }

    // /**
    // * 获得消息唯一序列号
    // */
    // private int getSequNo() {
    // return queryLastMsgId();
    // }

    /**
     * 新增一条消息
     * 
     * @param msg 消息体
     * @return msgId 消息存储的唯一ID TODO 暂时用旧的实体类，不用子类强转，程序完成后DB再对应
     */
    public int add(MessageInfo msg) {

//        if (null == msg || null == msg.getMsgFromUserId()
//                || null == msg.getTargetId()
//               /* || SysConstant.DEFAULT_MESSAGE_ID == msg.getMsgId()*/) {
//            return SysConstant.DEFAULT_MESSAGE_ID;
//        }
//        int msgId = msg.getMsgId();
//        int dt = msg.getDisplayType();
//        Long timeNow = Long.valueOf(System.currentTimeMillis());
//
//        int created = (0 == msg.getCreated()) ? (int) (timeNow / 1000) : msg
//                .getCreated();
//        int updated = (0 == msg.getUpdated()) ? (int) (timeNow / 1000) : msg
//                .getUpdated();
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
     * 更新图片存储路径
     * 
     * @param msgId 消息ID
     * @param status 消息状态
     * @return Boolean
     */
    public Boolean updateMsgImageSavePath(int msgId, String newPath) {
        Boolean boolRtn = false;
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            String updateSql = "UPDATE " + DBHelper.TABLE_EXTRA_IMAGE + " SET "
                    + DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH + " = ?, "
                    + DBHelper.COLUMN_UPDATED + " = ? where "
                    + DBHelper.COLUMN_MESSAGE_ID + " = ?";
            dbMaster.execSQL(updateSql, new Object[] {
                    newPath,
                    updated, msgId
            });
            // Logger.getLogger(MessageDB.class).d(
            // "UPDATE " + DBHelper.TABLE_EXTRA_IMAGE + " SET "
            // + DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH + " = " + newPath
            // + "where "
            // + DBHelper.COLUMN_MESSAGE_ID + " = " + msgId);
            boolRtn = true;
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return boolRtn;
    }

    /**
     * 更新某条消息状态(加载状态)
     * 
     * @param msgId 消息ID
     * @param status 消息状态
     * @return Boolean
     */
    public Boolean updateStatusByMsgId(int msgId, int status) {
        Boolean boolRtn = false;
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            String updateSql = "UPDATE " + DBHelper.TABLE_MESSAGES + " SET `"
                    + DBHelper.COLUMN_MESSAGE_STATUS + "` = ?, `"
                    + DBHelper.COLUMN_UPDATED + "` = ? where `"
                    + DBHelper.COLUMN_MESSAGE_ID + "` = ?";
            dbMaster.execSQL(updateSql, new Object[] {
                    status, updated, msgId
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
     * 更新某条消息状态(是否已读或展现)
     * 
     * @param msgId 消息ID
     * @param readStatus 消息是否已读或展现状态
     * @return Boolean
     */
    public Boolean updateReadStatusByMsgId(int msgId, int readStatus) {
        Boolean boolRtn = false;
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            String updateSql = "UPDATE " + DBHelper.TABLE_MESSAGES + " SET `"
                    + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` = ?, `"
                    + DBHelper.COLUMN_UPDATED + "` = ? where `"
                    + DBHelper.COLUMN_MESSAGE_ID + "` = ?";
            dbMaster.execSQL(updateSql, new Object[] {
                    readStatus, updated, msgId
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
     * 更新两个用户之间的某条消息之前（含某条消息）的所有消息状态(加载状态)
     * 
     * @param msgId 消息ID，做分界用
     * @param relateId 两个用户之间的联系ID
     * @param status 消息状态
     * @return Boolean
     */
    public Boolean updateStatusBefore(String ownerId, int msgId, int relateId,
            int status) {
        Boolean boolRtn = false;
        if (null == ownerId || 0 == msgId || 0 == relateId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            String updateSql = "UPDATE "
                    + DBHelper.TABLE_MESSAGES + " SET `"
                    + DBHelper.COLUMN_MESSAGE_STATUS + "` = ?, `"
                    + DBHelper.COLUMN_UPDATED + "` = ? where `"
                    + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
                    + DBHelper.COLUMN_RELATE_ID + "` = ? and "
                    + DBHelper.COLUMN_MESSAGE_ID + " <= ?";
            dbMaster.execSQL(updateSql, new Object[] {
                    status, updated,
                    ownerId, relateId, msgId
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
     * 更新两个用户之间的某条消息之前（含某条消息）的所有消息状态(是否已读或展现)
     * 
     * @param msgId 消息ID，做分界用
     * @param relateId 两个用户之间的联系ID
     * @param status 消息状态
     * @return Boolean
     */
    public Boolean updateReadStatusBefore(String ownerId, int msgId, int relateId,
            int readStatus) {
        Boolean boolRtn = false;
        if (null == ownerId || SysConstant.DEFAULT_MESSAGE_ID == msgId || 0 == relateId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            String updateSql = "UPDATE "
                    + DBHelper.TABLE_MESSAGES + " SET `"
                    + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` = ?, `"
                    + DBHelper.COLUMN_UPDATED + "` = ? where `"
                    + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
                    + DBHelper.COLUMN_RELATE_ID + "` = ? and "
                    + DBHelper.COLUMN_MESSAGE_ID + " <= ?";
            dbMaster.execSQL(updateSql, new Object[] {
                    readStatus, updated,
                    ownerId, relateId, msgId
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
     * 更新两个用户之间的所有收到的消息状态(加载状态)
     * 
     * @param userId 当前登入用户ID
     * @param relateId 两个用户之间的联系ID
     * @param status 消息状态
     * @return Boolean
     * @warnning 不能全部设置，需要根据消息的类型，但是在这里判断破坏了db的封装， 目前先这样做 by语鬼，待 @舒沉 回来之后添加接口
     */
    public Boolean updateStatus(String userId, int relateId, int status) {
        Boolean boolRtn = false;
        if (null == userId || 0 == relateId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            String updateSql = "UPDATE "
                    + DBHelper.TABLE_MESSAGES + " SET `"
                    + DBHelper.COLUMN_MESSAGE_STATUS + "status` = ?, `"
                    + DBHelper.COLUMN_UPDATED + "` = ? where `"
                    + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
                    + DBHelper.COLUMN_RELATE_ID + "` = ? and `"
                    + DBHelper.COLUMN_MESSAGE_TO_USER_ID + "` = ? ";
            dbMaster.execSQL(updateSql, new Object[] {
                    status, updated, userId,
                    relateId, userId
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
     * 更新两个用户之间的所有收到的消息状态(是否已读或展现)
     * 
     * @param userId 当前登入用户ID
     * @param relateId 两个用户之间的联系ID
     * @param readStatus 消息状态(是否已读或展现)
     * @return Boolean
     * @warnning 不能全部设置，需要根据消息的类型，但是在这里判断破坏了db的封装， 目前先这样做 by语鬼，待 @舒沉 回来之后添加接口
     */
    public Boolean updateReadStatus(String userId, int relateId, int readStatus) {
        Boolean boolRtn = false;
        if (null == userId || 0 == relateId) {
            return boolRtn;
        }
        int updated = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 消息状态只在Messages主表中存在，所以单表更新即可
            String updateSql = "UPDATE "
                    + DBHelper.TABLE_MESSAGES + " SET `"
                    + DBHelper.COLUMN_MESSAGE_READ_STATUS + "` = ?, `"
                    + DBHelper.COLUMN_UPDATED + "` = ? where `"
                    + DBHelper.COLUMN_OWNER_ID + "` = ? and `"
                    + DBHelper.COLUMN_RELATE_ID + "` = ? and `"
                    + DBHelper.COLUMN_MESSAGE_TO_USER_ID + "` = ? and "
                    + DBHelper.COLUMN_MESSAGE_READ_STATUS + " < ? ";
            dbMaster.execSQL(updateSql, new Object[] {
                    readStatus, updated, userId,
                    relateId, userId, readStatus
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
     * 更新某条消息的图片存储路径和链接
     * 
     * @param msgId 消息存储的唯一ID
     * @param savePath 图片保存文件路径
     * @param url 图片的URL
     * @param updated 消息更新时间
     * @return msgId 成功返回消息ID，失败返回0
     */
    public int updateImagePathUrlInfo(int msgId, String savePath, String url,
            Byte type, int updated) {
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return msgId;
        }
        updated = (0 == updated) ? (int) (Long.valueOf(System
                .currentTimeMillis()) / 1000) : updated;
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 语音信息存在ExtraImage表中，单表更新即可
            String updateSql = "UPDATE "
                    + DBHelper.TABLE_EXTRA_IMAGE + " SET `"
                    + DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_SAVE_PATH + "` = ?, `"
                    + DBHelper.COLUMN_MESSAGE_EXTRA_IMAGE_URL + "` = ? , `"
                    + DBHelper.COLUMN_UPDATED + "` = ? where `"
                    + DBHelper.COLUMN_MESSAGE_ID + "` = ?";
            dbMaster.execSQL(updateSql, new Object[] {
                    savePath, url, updated,
                    msgId
            });
            Logger.getLogger(MessageModel.class).d(
                    updateSql + "save path = " + savePath + " url =" + url
                            + "msgId = " + msgId);
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return msgId;
    }

    /**
     * 更新某条消息的语音存储路径和链接
     * 
     * @param msgId 消息存储的唯一ID
     * @param savePath 语音保存文件路径
     * @param url 语音的URL
     * @param updated 消息更新时间
     * @return msgId 成功返回消息ID，失败返回0
     */
    protected int updateAudioPathUrlInfo(int msgId, String savePath,
            String url, Byte type, int updated) {
        if (SysConstant.DEFAULT_MESSAGE_ID == msgId) {
            return msgId;
        }
        updated = (0 == updated) ? (int) (Long.valueOf(System
                .currentTimeMillis()) / 1000) : updated;
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            // 语音信息存在ExtraAudio标中，单表更新即可
            String updateSql = "UPDATE "
                    + DBHelper.TABLE_EXTRA_AUDIO + " SET `"
                    + DBHelper.COLUMN_MESSAGE_EXTRA_AUDIO_SAVE_PATH + "` = ?, `"
                    + DBHelper.COLUMN_MESSAGE_EXTRA_AUDIO_URL + "` = ? , `"
                    + DBHelper.COLUMN_UPDATED + "` = ? where `"
                    + DBHelper.COLUMN_MESSAGE_ID + "` = ?";
            dbMaster.execSQL(updateSql, new Object[] {
                    savePath, url, updated,
                    msgId
            });
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }

        return msgId;
    }

    /**
     * 删除一条消息
     * 
     * @param msg
     * @return Boolean
     */
    public Boolean delete(MessageInfo msg) {
//        Boolean boolRtn = false;
//        if (null == msg || SysConstant.DEFAULT_MESSAGE_ID == msg.getMsgId()) {
//            return boolRtn;
//        }
//        SQLiteDatabase dbMaster = null;
//        try {
//            dbMaster = helper.getWritableDatabase();
//            dbMaster.beginTransaction();
//            int msgId = msg.getMsgId();
//            int dt = msg.getDisplayType();
//            dbMaster.delete(DBHelper.TABLE_MESSAGES, ""
//                    + DBHelper.COLUMN_MESSAGE_ID + " == ?",
//                    new String[] {
//                        String.valueOf(msgId)
//                    });
//            if (SysConstant.DISPLAY_TYPE_TEXT == dt) {
//                // 文本类消息
//                dbMaster.delete(DBHelper.TABLE_EXTRA_TEXT, ""
//                        + DBHelper.COLUMN_MESSAGE_ID + " == ?",
//                        new String[] {
//                            String.valueOf(msgId)
//                        });
//            } else if (SysConstant.DISPLAY_TYPE_IMAGE == dt) {
//                // 图片消息
//                dbMaster.delete(DBHelper.TABLE_EXTRA_IMAGE, ""
//                        + DBHelper.COLUMN_MESSAGE_ID + " == ?",
//                        new String[] {
//                            String.valueOf(msgId)
//                        });
//            } else if (SysConstant.DISPLAY_TYPE_AUDIO == dt) {
//                // 语音消息
//                dbMaster.delete(DBHelper.TABLE_EXTRA_AUDIO, ""
//                        + DBHelper.COLUMN_MESSAGE_ID + " == ?",
//                        new String[] {
//                            String.valueOf(msgId)
//                        });
//            } else {
//            }
//
//            dbMaster.setTransactionSuccessful();
//            boolRtn = true;
//        } catch (SQLException e) {
//            logger.e(e.toString());
//        } finally {
//            if (null != dbMaster) {
//                dbMaster.endTransaction();
//            }
//            // dbMaster.close();
//        }
//
//        return boolRtn;
    	return false;
    }

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
            c = dbSlaver.rawQuery("select " + DBHelper.COLUMN_MESSAGE_ID
                    + " from " + DBHelper.TABLE_MESSAGES
                    + DBHelper.LIMIT_ONE_MESSAGE_SUFFIX, null);
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
    public Map<Integer, MessageInfo> queryLastOne(String ownerId) {

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
     * 根据消息ID查询两个用户之间的某条消息详情(含Extra信息)
     * 
     * @param msgId 消息ID
     * @return MessageInfo
     */
    public MessageInfo queryByMsgId(int msgId) {
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
            while (c.moveToNext()) {
                msg = setMsgBaseInfo(c);
                String queryExtraSql = "SELECT * FROM " + DBHelper.TABLE_MESSAGES + " where "
                        + DBHelper.COLUMN_MESSAGE_ID + " = " + msgId;
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
     * 查询两个用户之间的最后一条消息(不含Extra部分)
     * 
     * @param relateId 两个用户之间的联系ID
     * @return MessageInfo
     */
    public MessageInfo queryLastOneByRelateId(String ownerId, int relateId) {
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
     * @return int
     */
    public int queryUnreadTotalCountByUserId(String userId) {
        int unreadCount = 0;
        if (null == userId) {
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
     * 提供给聊天界面上未读消息计数提示 查询当前登入用户除某用户(主要是当前聊天对象)之外的所有未读消息计数
     * 
     * @param userId 用户ID
     * @param exclUserId
     * @return int
     */
    public int queryUnreadTotalCountExclUserId(String userId, String exclUserId) {
        int unreadCount = 0;
        if (null == userId || null == exclUserId) {
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
            // 未知消息，为扩展做准备
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

}
