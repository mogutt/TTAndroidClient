
package com.mogujie.tt.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.mogujie.tt.R;
import com.mogujie.tt.config.SysConstant;
import com.mogujie.tt.log.Logger;
import com.mogujie.tt.ui.tools.PhotoHandler;

public class CommonUtil {
    /**
     * @Description 判断是否是顶部activity
     * @param context
     * @param activityName
     * @return
     */
    public static boolean isTopActivy(Context context, String activityName) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cName = am.getRunningTasks(1).size() > 0 ? am
                .getRunningTasks(1).get(0).topActivity : null;

        if (null == cName)
            return false;
        return cName.getClassName().equals(activityName);
    }

    /**
     * @Description 判断存储卡是否存在
     * @return
     */
    public static boolean checkSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }

        return false;
    }

    /**
     * @Description 获取sdcard可用空间的大小
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getSDFreeSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long blockSize = sf.getBlockSize();
        long freeBlocks = sf.getAvailableBlocks();
        // return freeBlocks * blockSize; //单位Byte
        // return (freeBlocks * blockSize)/1024; //单位KB
        return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
    }

    /**
     * @Description 获取sdcard容量
     * @return
     */
    @SuppressWarnings({
            "deprecation", "unused"
    })
    private static long getSDAllSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        long blockSize = sf.getBlockSize();
        long allBlocks = sf.getBlockCount();
        // 返回SD卡大小
        // return allBlocks * blockSize; //单位Byte
        // return (allBlocks * blockSize)/1024; //单位KB
        return (allBlocks * blockSize) / 1024 / 1024; // 单位MB
    }

    public static byte[] intToBytes(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));
        }
        return b;
    }

    public static byte[] float2byte(float f) {

        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }

        return dest;

    }

    /**
     * 将byte数组转换为int数据
     * 
     * @param b 字节数组
     * @return 生成的int数据
     */
    public static int byteArray2int(byte[] b) {
        return (((int) b[0]) << 24) + (((int) b[1]) << 16)
                + (((int) b[2]) << 8) + b[3];
    }

    /**
     * @Description 判断是否是url
     * @param text
     * @return
     */
    private static String matchUrl(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        Pattern p = Pattern.compile(
                "[http]+[://]+[0-9A-Za-z:/[-]_#[?][=][.][&]]*",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    /**
     * @Description 返回匹配到的URL
     * @param text
     * @param cmpUrl
     * @return
     */
    private static String getMatchUrl(String text, String cmpUrl) {
        String url = matchUrl(text);
        if (url != null && url.contains(cmpUrl)) {
            return url;
        } else {
            return null;
        }
    }

    /**
     * @Description 链接跳转处理
     * @param content
     */
    public static void skipLink(Context cxt, String content) {
//        String detailUrl = "";
//        String orderUrl = "";
//        String httpUrl = "";
//        if (!TextUtils.isEmpty(detailUrl = getMatchUrl(content, SysConstant.DETAIL_HOST))) {
//            toDetailPage(cxt, getGoodsId(detailUrl));
//        } else if (!TextUtils.isEmpty(orderUrl = getMatchUrl(content, SysConstant.ORDER_HOST))) {
//            toOrderPage(cxt, getOrderId(orderUrl));
//            // toWebPage(cxt, orderUrl);
//        } else if (!TextUtils.isEmpty(httpUrl = matchUrl(content))) {
//            toWebPage(cxt, httpUrl);
//        }
    }

    /**
     * @Description 用iid跳转到主客户端的detail页面
     * @param context
     * @param iid
     */
    public static void toDetailPage(Context context, String iid) {
        if (TextUtils.isEmpty(iid)) {
            Toast.makeText(context,
                    context.getResources().getString(R.string.invalid_detail_url),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String uriStr = "mgjclient://detail?iid=" + iid;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
        context.startActivity(i);
    }

    /**
     * @Description 跳转到订单页
     * @param context
     * @param orderId
     */
    private static void toOrderPage(Context context, String orderId) {
        if (TextUtils.isEmpty(orderId)) {
            Toast.makeText(context,
                    context.getResources().getString(R.string.invalid_order_url),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String uriStr = "mgjclient://order?orderId=" + orderId;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
        context.startActivity(i);
    }

    /**
     * @Description web页面跳转
     * @param ctx
     * @param url
     */
    public static void toWebPage(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String uriStr = "mgjclient://web?url=" + url;
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
        context.startActivity(i);
    }

    /**
     * @Description
     * @param 获取订单id url
     *            http://www.mogujie.com/trade/order/detail4buyer?orderId=
     *            32990496
     * @return
     */
    private static String getOrderId(String url) {
        String id = url.substring(url.lastIndexOf("=") + 1);
        return id;
    }

    /**
     * @Description 获取商品详情信息中的id信息
     * @param url http://shop.mogujie.com/detail/16mg5iw/
     *            http://shop.mogujie.com/detail/16mg5iw?asdf
     *            http://shop.mogujie.com/detail/16mg5iw
     * @return
     */
    private static String getGoodsId(String url) {
//        String[] strArray = url.split(SysConstant.DETAIL_URL);
//        String id = null;
//        if (strArray.length == 2) {
//            id = strArray[1];
//            int pSplit = id.indexOf("/");
//            int pParam = id.indexOf("?");
//            if (pSplit > 0) {
//                id = id.substring(0, pSplit);
//            } else if (pParam > 0) {
//                id = id.substring(0, pParam);
//            }
//        }
//        return id;
    	return null;
    }

    public static String getImageSavePath(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        final File folder = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + "MGJ-IM"
                + File.separator
                + "images");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder.getAbsolutePath() + File.separator + fileName;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     * 
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
            String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String getImagePath(Context context, Intent data) {
        Uri originalUri = data.getData();
        String path = PhotoHandler.getInstance(context).getImagePathFromUri(
                originalUri);

        return path;
    }

//    @TargetApi(19)
//    /**
//     * 这个函数只有19以上会被调用到
//     * @param data
//     */
//    public static String getImagePathAboveKITKAT(Context context, Intent data) {
//        final Uri uri = data.getData();
//        String path = null;
//        if (DocumentsContract.isDocumentUri(context, uri)) {
//            if (CommonUtil.isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                if ("primary".equalsIgnoreCase(type)) {
//                    path = Environment.getExternalStorageDirectory() + "/"
//                            + split[1];
//                }
//            } else if (CommonUtil.isDownloadsDocument(uri)) {
//                final String id = DocumentsContract.getDocumentId(uri);
//                final Uri contentUri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"),
//                        Long.valueOf(id));
//
//                path = CommonUtil
//                        .getDataColumn(context, contentUri, null, null);
//            } else if (CommonUtil.isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                Uri contentUri = null;
//                if ("image".equals(type)) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//
//                final String selection = "_id=?";
//                final String[] selectionArgs = new String[] {
//                        split[1]
//                };
//
//                path = CommonUtil.getDataColumn(context, contentUri, selection,
//                        selectionArgs);
//            }
//        }// MediaStore (and general)
//        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//
//            // Return the remote address
//            if (CommonUtil.isGooglePhotosUri(uri)) {
//                path = uri.getLastPathSegment();
//            } else {
//                path = CommonUtil.getDataColumn(context, uri, null, null);
//            }
//        }
//        // File
//        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            path = uri.getPath();
//        }
//
//        // 如果实在获取不到path，那就用旧方法再来一次
//        if (TextUtils.isEmpty(path)) {
//            path = PhotoHandler.getInstance(context).getImagePathFromUri(uri);
//        }
//
//        return path;
//    }

    public static int getDefaultPannelHeight(Context context) {
        if (context != null) {
            int size = (int) (getElementSzie(context) * 5.5);
            return size;
        } else {
            return 300;
        }
    }

    public static int getAudioBkSize(int sec, Context context) {
        int size = getElementSzie(context) * 3;
        if (sec <= 0) {
            return -1;
        } else if (sec <= 2) {
            return size;
        } else if (sec <= 8) {
            return (int) (size + ((float) ((sec - 2) / 6.0)) * size);
        } else if (sec <= 60) {
            return (int) (2 * size + ((float) ((sec - 8) / 52.0)) * size);
        } else {
            return -1;
        }
    }

    public static int getElementSzie(Context context) {
        if (context != null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int screenHeight = px2dip(dm.heightPixels, context);
            int screenWidth = px2dip(dm.widthPixels, context);
            int size = screenWidth / 6;
            if (screenWidth >= 800) {
                size = 60;
            } else if (screenWidth >= 650) {
                size = 55;
            } else if (screenWidth >= 600) {
                size = 50;
            } else if (screenHeight <= 400) {
                size = 20;
            } else if (screenHeight <= 480) {
                size = 25;
            } else if (screenHeight <= 520) {
                size = 30;
            } else if (screenHeight <= 570) {
                size = 35;
            } else if (screenHeight <= 640) {
                if (dm.heightPixels <= 960) {
                    size = 35;
                } else if (dm.heightPixels <= 1000) {
                    size = 45;
                }
            }
            return size;
        }
        return 40;
    }

    private static int px2dip(float pxValue, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static boolean isInIm(Context context) {
        try {
            if (context instanceof Activity) {
                ActivityManager am = (ActivityManager) context
                        .getSystemService(Context.ACTIVITY_SERVICE);
                ComponentName cn = am.getRunningTasks(1).size() > 0 ? am
                        .getRunningTasks(1).get(0).topActivity : null;
//                Logger.getLogger().d(cn.getClassName());
                if (cn != null && cn.getClassName().contains(SysConstant.APPLICATION_PACKAGE_NAME)) {
                    return true;
                }
                return false;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static int getImageMessageItemDefaultWidth(Context context) {
        return CommonUtil.getElementSzie(context) * 5;
    }

    public static int getImageMessageItemDefaultHeight(Context context) {
        return CommonUtil.getElementSzie(context) * 7;
    }

    public static int getImageMessageItemMinWidth(Context context) {
        return CommonUtil.getElementSzie(context) * 3;
    }

    public static int getImageMessageItemMinHeight(Context context) {
        return CommonUtil.getElementSzie(context) * 3;
    }

    public static String getMd5Path(String url, int type) {
        if (null == url) {
            return null;
        }
        String path = getSavePath(type) + StringUtil.getMd5(url)
                + SysConstant.DEFAULT_IMAGE_FORMAT;
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return path;
    }

    public static String getAudioSavePath(String userId) {
        String path = getSavePath(SysConstant.FILE_SAVE_TYPE_AUDIO) + userId
                + "_" + String.valueOf(System.currentTimeMillis())
                + SysConstant.DEFAULT_AUDIO_SUFFIX;
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return path;
    }

    public static String getSavePath(int type) {
        String path;
        String floder = (type == SysConstant.FILE_SAVE_TYPE_IMAGE) ? "images"
                : "audio";
        if (CommonUtil.checkSDCard()) {
            path = Environment.getExternalStorageDirectory().toString()
                    + File.separator + "MGJ-IM" + File.separator + floder
                    + File.separator;

        } else {
            path = Environment.getDataDirectory().toString() + File.separator
                    + "MGJ-IM" + File.separator + floder + File.separator;
        }
        return path;
    }

    /**
     * @Description 隐藏软键盘
     * @param activity
     */
    public static void hideInput(Activity activity) {
        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
