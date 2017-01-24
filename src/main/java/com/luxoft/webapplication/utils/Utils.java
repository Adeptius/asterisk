package com.luxoft.webapplication.utils;


public class Utils {

    public static String getFakeGoogleId(){
        long a =1000000000 + (long) (Math.random()*8999999999L);
        long b =1000000000 + (long) (Math.random()*8999999999L);
        return a + "." + b;
    }

}
