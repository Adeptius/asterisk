package ua.adeptius.asterisk.utils;


public class MyLogger {


    public static void log(String message, Class clazz ) {
        System.out.println(message);
    }

    public static void printException(Exception e) {
        if (Settings.showDetailedErrorsLogs){
            e.printStackTrace();
        }
    }
}
