package org.blackist.videorecorder.common.util;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * TODO
 *
 * @author LiangLiang.Dong <1075512174@qq.com>.
 * @Date 2017/12/16 20:12.
 */
public class FileUtil {

    public static void writeToLog(String log) {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                String filename = Environment.getExternalStorageDirectory()
                        .getCanonicalPath() + "/Alarm/alarm.txt";
                File logfile = new File(filename);
                if (!logfile.getParentFile().exists()) {
                    //如果目标文件所在的目录不存在，则创建父目录
                    System.out.println("目标文件所在目录不存在，准备创建它！");
                    if (!logfile.getParentFile().mkdirs()) {
                        System.out.println("创建目标文件所在目录失败！");
                        return;
                    }
                }
                //这里就不要用openFileOutput了,那个是往手机内存中写数据的
                FileOutputStream output = new FileOutputStream(filename, true);
                output.write(log.getBytes());
                //将String字符串以字节流的形式写入到输出流中
                output.close();
                //关闭输出流
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
