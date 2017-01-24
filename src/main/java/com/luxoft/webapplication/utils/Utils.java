package com.luxoft.webapplication.utils;


public class Utils {

    public static String getFakeGoogleId(){
        long i = (long) (Math.random()*1000000);
        return String.valueOf(i);
    }

}
