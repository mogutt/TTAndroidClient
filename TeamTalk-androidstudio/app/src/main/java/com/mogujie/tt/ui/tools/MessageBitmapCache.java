
package com.mogujie.tt.ui.tools;

import java.util.LinkedList;
import java.util.List;

import com.mogujie.tt.log.Logger;

import android.graphics.Bitmap;

/***
 * @Desc bitmap缓存类
 * @author yugui
 */
public class MessageBitmapCache {

    public class BitmapMemoryUnit {

        public BitmapMemoryUnit() {

        }

        public BitmapMemoryUnit(String _path, Bitmap _bmp) {
            this.path = _path;
            this.bmp = _bmp;
        }

        public String getPathString() {
            return path;
        }

        public void setPath(String pathString) {
            this.path = pathString;
        }

        public Bitmap getBmp() {
            return bmp;
        }

        public void setBmp(Bitmap bmp) {
            this.bmp = bmp;
        }

        private String path;
        private Bitmap bmp;

    }

    private Logger logger = Logger.getLogger(MessageBitmapCache.class);

    private final int nMaxElemInList = 20;

    private static MessageBitmapCache instance;

    public static synchronized MessageBitmapCache getInstance() {
        if (instance == null) {
            instance = new MessageBitmapCache();
        }
        return instance;
    }

    private MessageBitmapCache() {

    }

//    List<BitmapMemoryUnit> bitmapList = new LinkedList<MessageBitmapCache.BitmapMemoryUnit>();

//    // 以下两个供上层调用的函数，加锁以同步，其调用的私有函数无须加锁
//    // 上层调用
//    public Boolean put(String path, Bitmap bmp) {
//
//        if (path == null || bmp == null || path.equals(""))
//            return false;
//
//        synchronized (bitmapList) {
//            BitmapMemoryUnit bmUnit = IsContains(path);
//            if (bmUnit == null) {
//                // 之前不存在
//                if (bitmapList.size() >= nMaxElemInList) {
//                    removeTail();
//                }
//                bmUnit = new BitmapMemoryUnit(path, bmp);
//                bitmapList.add(0, bmUnit);
//
//            } else {
//                // 已经存在
//                bitmapList.remove(bmUnit);
//                bitmapList.add(0, bmUnit);
//            }
//        }
//
//        return true;
//    }

    // 上层调用
    public Bitmap get(String path) {

        if (path == null || path.equals(""))
            return null;

//        synchronized (bitmapList) {
//            BitmapMemoryUnit bmuUnit = IsContains(path);
//            if (bmuUnit != null) {
//                logger.d("match in cache");
////                bitmapList.remove(bmuUnit);
////                bitmapList.add(0, bmuUnit);
//                return bmuUnit.getBmp();
//
//            } else {
                logger.d("not match in cache");
                Bitmap bmp = ImageTool.createImageThumbnail(path);
                return bmp;
//                if (put(path, bmp)) {
//                    return bmp;
//                } else {
//                    return null;
//                }
//            }
//        }
    }

//    private BitmapMemoryUnit IsContains(String path) {
//
//        if (path == null || path.equals("")) {
//            return null;
//        }
//
//        for (BitmapMemoryUnit bmUnit : bitmapList) {
//            if (bmUnit != null) {
//                if (bmUnit.getPathString().equals(path)) {
//                    return bmUnit;
//                }
//            }
//        }
//
//        return null;
//    }
//
//    private void removeTail() {
//        while (true) {
//            int sz = bitmapList.size();
//            if (sz >= nMaxElemInList) {
//                bitmapList.remove(sz - 1);
//            } else {
//                break;
//            }
//        }
//    }
}
