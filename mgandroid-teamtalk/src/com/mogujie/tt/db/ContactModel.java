/**
 * DB中IMRecentContact表的操作类
 * @author shuchen
 */

package com.mogujie.tt.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mogujie.tt.entity.IMRecentContact;
import com.mogujie.tt.entity.RecentInfo;
import com.mogujie.tt.log.Logger;

public class ContactModel {
    private DBHelper helper;
    private Logger logger = Logger.getLogger(ContactModel.class);
    @SuppressWarnings("unused")
    private Context context = null;

    /**
     * @author shuchen
     */
    public ContactModel(Context context) {
        this.context = context;
        helper = DBHelper.getInstance(context);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
    }

    /**
     * query last one imMessage, return cursor 获得两个用户之间的联系ID，没有则新增一个
     * 
     * @param ownerId 拥有者用户ID
     * @param userId 用户Id
     * @param friendUserId 好友Id
     * @return Cursor
     */
    public int getRelateId(String ownerId, String userId, String friendUserId) {
        if (null == ownerId || null == userId || null == friendUserId) {
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
     * query relateId, return relateId 查询两个用户之间的联系ID
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
     * add one imRecentContact 添加一条最近联系记录
     * 
     * @param imRecentContact
     */
    protected void add(IMRecentContact imRecentContact) {
        if (null == imRecentContact || null == imRecentContact.getUserId()) {
            return;
        }
        try {
            this.add(imRecentContact.getUserId(), imRecentContact.getUserId(),
                    imRecentContact.getFriendUserId(), imRecentContact.getStatus(),
                    imRecentContact.getCreated(), imRecentContact.getUpdated());
        } catch (SQLException e) {
            logger.e(e.toString());
        }
    }

    /**
     * add list imRecentContacts 新增一坨最近联系记录
     * 
     * @param imRecentContacts
     */
    protected void adds(List<IMRecentContact> imRecentContacts) {
        try {
            for (IMRecentContact imRecentContact : imRecentContacts) {
                this.add(imRecentContact);
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        }
    }

    /**
     * delete imRecentContact 删除一个最近联系记录
     * 
     * @param imRecentContact
     */
    public void delete(IMRecentContact imRecentContact) {
        if (null == imRecentContact || null == imRecentContact.getUserId()) {
            return;
        }
        SQLiteDatabase dbMaster = null;
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.delete(DBHelper.TABLE_CONTACTS, DBHelper.COLUMN_RELATE_ID + " == ?",
                    new String[] {
                        String.valueOf(imRecentContact.getRelateId())
                    });
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
    }

    /**
     * query all IMRecentContact, return list 查询所有联系ID
     * 
     * @return List<IMRecentContact>
     */
    public List<IMRecentContact> queryAll() {
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
     * query all User, return list 查询最近联系人的用户信息
     * 
     * @return List<RecentInfo>
     */
    public List<RecentInfo> queryAllFriendUserId(String ownerId) {
        ArrayList<RecentInfo> users = new ArrayList<RecentInfo>();
        if (null == ownerId) {
            return users;
        }
        Cursor c = null;
        SQLiteDatabase dbSlaver = helper.getReadableDatabase();
        try {
            String sql = DBHelper.SELECT_ALL_FRIEND_USERID_SQL + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "'";
            c = dbSlaver.rawQuery(sql, null);
            RecentInfo userInfo;
            String fromUserId;
            String toUserId;
            while (c.moveToNext()) {
                userInfo = new RecentInfo(); // 为了对象不同，每次都新生成对象
                fromUserId = c.getString(c.getColumnIndex(DBHelper.COLUMN_USER_ID));
                toUserId = c.getString(c.getColumnIndex(DBHelper.COLUMN_FRIEND_USER_ID));

                if (!ownerId.equals(fromUserId)) {
                    userInfo.setUserId(fromUserId); // 判断不是自己则是好友,只能有
                } else {
                    if (!ownerId.equals(toUserId)) {
                        userInfo.setUserId(toUserId); // 判断不是自己则是好友
                    }
                }

                users.add(userInfo);
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

    public List<String> queryFriendsIdList(String ownerId) {
        ArrayList<String> idList = new ArrayList<String>();
        if (null == ownerId) {
            return idList;
        }
        Cursor c = null;
        SQLiteDatabase dbSlaver = helper.getReadableDatabase();
        try {
            String sql = DBHelper.SELECT_ALL_FRIEND_USERID_SQL + " where "
                    + DBHelper.COLUMN_OWNER_ID + " = '" + ownerId + "'";
            c = dbSlaver.rawQuery(sql, null);
            String userId = null;
            String fromUserId;
            String toUserId;
            while (c.moveToNext()) {
                fromUserId = c.getString(c.getColumnIndex(DBHelper.COLUMN_USER_ID));
                toUserId = c.getString(c.getColumnIndex(DBHelper.COLUMN_FRIEND_USER_ID));

                if (!ownerId.equals(fromUserId)) {
                    userId = fromUserId; // 判断不是自己则是好友,只能有
                } else {
                    if (!ownerId.equals(toUserId)) {
                        userId = toUserId; // 判断不是自己则是好友
                    }
                }
                if (null != userId) {
                    idList.add(userId);
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
        return idList;
    }

    /**
     * add one imRecentContact 新增一条最近联系记录
     * 
     * @param ownerId 联系人所有者用户ID
     * @param userId 用户ID
     * @param friendUserId 好友用户ID
     * @param status 状态
     * @param created 创建时间
     * @param updated 更新时间
     * @param imRecentContact
     */
    protected void add(String ownerId, String userId, String friendUserId,
            int status, int created, int updated) {
        if (null == ownerId || null == userId || null == friendUserId) {
            return;
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
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
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
