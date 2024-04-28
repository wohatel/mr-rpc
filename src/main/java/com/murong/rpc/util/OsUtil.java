package com.murong.rpc.util;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 判断系统类型
 *
 * @author yaochuang 2024/01/25 15:53
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OsUtil {

    /**
     * 是否是mac系统
     *
     * @return boolean
     */
    public static boolean isMac() {
        String property = getOs();
        return property.contains("Mac") || property.contains("mac");
    }

    /**
     * 是否是win系统
     *
     * @return boolean
     */
    public static boolean isWindow() {
        String property = getOs();
        return property.contains("Window") || property.contains("window");
    }

    /**
     * 是否是linux系统
     *
     * @return boolean
     */
    public static boolean isLinux() {
        String property = getOs();
        return property.contains("Linux") || property.contains("linux");
    }

    /**
     * 生成临时文件夹
     */
    public static String genTmpDirectory(String subPath) {
        String fileDir = isWindow() ? "C:/Windows/Temp/" : "/tmp";
        File file = new File(fileDir, subPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 生成临时文件
     */
    public static File genTmpFile() {
        String fileDir = isWindow() ? "C:/Windows/Temp/" : "/tmp";
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(fileDir, RandomStringUtils.randomAlphabetic(10));
    }

    /**
     * 生成临时文件
     */
    public static File genTmpFile(String parentDir) {
        String fileDir = parentDir;
        if (StringUtils.isBlank(fileDir)) {
            fileDir = isWindow() ? "C:/Windows/Temp/" : "/tmp";
        }
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(fileDir, RandomStringUtils.randomAlphabetic(10));
    }

    public static String getOs() {
        return System.getProperty("os.name");
    }

    /**
     * 生成随机的子文件夹
     */
    public static String genTmpSubDirectory() {
        return genTmpDirectory(RandomStringUtils.randomAlphabetic(12));
    }

}
