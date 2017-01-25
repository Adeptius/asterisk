package com.luxoft.webapplication.utils;


public class MyLogger {


    public static void log(String message, Class clazz ) {
        System.out.println(message);
    }

    public static void printException(Exception e) {
        if (Settings.moreErrorsLogs){
            e.printStackTrace();
        }
    }
}
