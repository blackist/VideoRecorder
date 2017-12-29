package org.blackist.videorecorder.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO
 *
 * @author LiangLiang.Dong <1075512174@qq.com>.
 * @Date 2017/12/16 20:09.
 */
public class DateUtil {

    public static String currentTime() {
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateformat1.format(new Date());
    }

    public static String currentDateTime() {
        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return dateformat1.format(new Date());
    }
}
