/**
 * DB中IMUsers表操作类
 * @author shuchen
 */

package com.mogujie.tt.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mogujie.tt.entity.User;
import com.mogujie.tt.log.Logger;

public class UserModel {
    private DBHelper helper;
    private Logger logger = Logger.getLogger(UserModel.class);

    @SuppressWarnings("unused")
    private Context context = null;

    /**
     * @author shuchen
     */
    public UserModel(Context context) {
        this.context = context;
        helper = DBHelper.getInstance(context);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
    }

    /**
     * add one user 新增一个用户
     * 
     * @param user
     */
    public void add(User user) {
        if (null == user || null == user.getUserId()) {
            return;
        }
        try {
            this.add(user.getUserId(), user.getName(), user.getNickName(), user.getAvatarUrl(),
                    user.getTitle(), user.getPosition()
                    , user.getRoleStatus(), user.getSex(), user.getDepartId(), user.getJobNum(),
                    user.getTelphone(), user.getEmail(),
                    user.getCreated(), user.getUpdated());
        } catch (SQLException e) {
            logger.e(e.toString());
        }
    }

    /**
     * add list users 新增一坨用户
     * 
     * @param users
     */
    public void adds(List<User> users) {
        try {
            for (User user : users) {
                this.add(user);
            }
        } catch (SQLException e) {
            logger.e(e.toString());
        }
    }

    /**
     * update user's uname 更新用户的名字
     * 
     * @param user
     */
    public void updateUname(User user) {
        if (null == user || null == user.getUserId()) {
            return;
        }
        ContentValues cv = new ContentValues();
        SQLiteDatabase dbMaster;
        cv.put("uname", user.getName());
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.update(DBHelper.TABLE_USERS, cv, DBHelper.COLUMN_USER_NAME + " = ?",
                    new String[] {
                        user.getName()
                    });
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
    }

    /**
     * update user's uname 更新用户的名字
     * 
     * @param user
     */
    private Integer force2update(User user) {

        int msgId = 0;
        if (null == user || null == user.getUserId()) {
            return msgId;
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
            msgId = dbMaster.update(DBHelper.TABLE_USERS, cv,
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
        return msgId;
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
        User oldUser = this.query(user.getUserId());
        if (null == oldUser) {// 没有则添加
            this.add(user);
            return true;
        }

        int msgId;
        if (forced) { // 如果强制更新则强制更新
            msgId = this.force2update(user);
            if (0 < msgId) {
                return true;
            }
            return false;
        }

        if (oldUser.getName().equals(user.getName())
                && oldUser.getAvatarUrl().equals(user.getAvatarUrl())) {
            return true; // 存在且相同直接返回
        }

        msgId = this.force2update(user); // 存在但不相同，则更新
        if (0 < msgId) {
            return true;
        }
        return false;
    }

    /**
     * delete user 删除一个用户
     * 
     * @param user
     */
    public void delete(User user) {
        if (null == user || null == user.getUserId()) {
            return;
        }
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.delete(DBHelper.TABLE_USERS, DBHelper.COLUMN_USER_ID + " == ?",
                    new String[] {
                        user.getUserId()
                    });
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
    }

    /**
     * query all User, return list 查询所有用户
     * 
     * @return List<User>
     */
    public List<User> queryAll() {
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
     * query one User, return user 查询一个用户
     * 
     * @param userId 用户ID
     * @return User | null
     */
    public User query(String userId) {
        User user = null;
        if (null == userId) {
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
     * add one user 新增一个用户
     * 
     * @param userId 用户ID
     * @param uname 用户名
     * @param unick 用户昵称
     * @param avatar 用户头像
     * @param type 用户类型
     * @param created 用户存储时间
     * @param updated 用户更新时间
     * @param user
     */
    protected void add(String userId, String uname, String unick, String avatar, String title,
            String position, int roleStatus, int sex, String departId, int jobNumber,
            String telphone, String email, int created, int updated) {
        int timeNow = (int) (Long.valueOf(System.currentTimeMillis()) / 1000);
        created = (0 == created) ? timeNow : created;
        updated = (0 == updated) ? timeNow : updated;
        SQLiteDatabase dbMaster;
        try {
            dbMaster = helper.getWritableDatabase();
            dbMaster.execSQL(DBHelper.INSERT_USER_SQL, new Object[] {
                    userId, uname, unick, avatar, title, position, roleStatus, sex, departId,
                    jobNumber, telphone, email, created, updated
            });
        } catch (SQLException e) {
            logger.e(e.toString());
        } finally {
            // dbMaster.close();
        }
    }

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

}
