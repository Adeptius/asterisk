package com.luxoft.webapplication.utils;



public class Settings {

    public static String asteriskIp;
    public static String asteriskLogin;
    public static String asteriskPassword;


    public static String standartNumber;
    public static boolean moreLogs;
    public static int phoneTimeToRemoveInSeconds;


    static {
        asteriskIp = "194.44.37.30";
        asteriskLogin = "adeptius";
        asteriskPassword = "ccb6f130f89de0bab95df361669e32ba";



        standartNumber = "5555555";
        moreLogs = true;
        phoneTimeToRemoveInSeconds = 40;
    }
}
