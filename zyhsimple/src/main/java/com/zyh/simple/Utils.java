package com.zyh.simple;

public class Utils {
    public static String ZTAG ="zyh------------->   ";
    public static boolean isWindows() {
        return System.getProperties().getProperty("os.name").toLowerCase().contains("windows");
    }
}
