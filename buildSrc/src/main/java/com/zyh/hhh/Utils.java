package com.zyh.hhh;

/**
 * description:
 * auhthor:zyh 25052023 245502345@qq.com
 */
public class Utils {

    public final static String ZTAG = " ================>";
    public static void prtln(String tag, String msg){
        System.out.println("====================> "+ tag+"[[[ start ]]]"+" <=======================");
        System.out.println(msg);
        System.out.println("====================> "+ tag+"[[[ end ]]]"+" <=======================\n\n");

    }
    public static void prtln(String msg){
        prtln("",msg);
    }


    public static boolean isWindows() {
        String osName = System.getProperties().getProperty("os.name");
        prtln("osName:: "+osName);
        return osName.toLowerCase().contains("windows");
    }
}
