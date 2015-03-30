
package com.mogujie.tt.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

import com.mogujie.tt.config.SysConstant;

/**
 * @Description
 * @author Nana
 * @date 2014-4-10
 */
public class StringUtil {
    public static String getMd5(String input) {
        String output = null;
        if (input != null && input.length() > 0)
            try {
                MessageDigest messagedigest = MessageDigest.getInstance("MD5");
                messagedigest.update(input.getBytes(), 0, input.length());
                output = String.format(SysConstant.MD5_KEY, new BigInteger(1,
                        messagedigest.digest()));
            } catch (Exception exception) {
            }
        return output;
    }

    public static String getUUID() {
        String str = UUID.randomUUID().toString();
        return str;
    }

    public static String getRandomNum(int type) {
        long number = -1;
        switch (type) {
            case SysConstant.RANDOM_TYPE_FILENAME:
                number = Math
                        .round(Math.random()
                                * (SysConstant.RANDOM_FILE_MARK_MAX - SysConstant.RANDOM_FILE_MARK_MIN)
                                + SysConstant.RANDOM_FILE_MARK_MIN);
                break;
            case SysConstant.RANDOM_TYPE_MSEESAGE_REQUESTNO:
                number = Math
                        .round(Math.random()
                                * (SysConstant.RANDOM_MSG_REQUESTNO_MAX - SysConstant.RANDOM_MSG_REQUESTNO_MIN)
                                + SysConstant.RANDOM_MSG_REQUESTNO_MIN);
                break;
        }

        return String.valueOf(number);
    }

    public static String getSmallerImageLink(String imgLink) {
        String path = imgLink + "_" + SysConstant.WEB_IMAGE_MIN_WIDTH + "x"
                + SysConstant.WEB_IMAGE_MIN_HEIGHT + SysConstant.DEFAULT_IMAGE_FORMAT;
        return path;
    }

}
