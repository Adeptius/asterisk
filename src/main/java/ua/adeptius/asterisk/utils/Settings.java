package ua.adeptius.asterisk.utils;



public class Settings {

    public static String asteriskAdress;
    public static String asteriskLogin;
    public static String asteriskPassword;

    public static String dbAdress;
    public static String dbLogin;
    public static String dbPassword;
    public static String dbTableName;

    public static String standartNumber;
    public static int phoneTimeToRemoveInSeconds;

    public static boolean moreErrorsLogs;
    public static boolean showPhoneRepeatedRequest;
    public static boolean showSendingMailLogs;

    public static String accessControlAllowOrigin;


    static {
        asteriskAdress = "194.44.37.30";
        asteriskLogin = "adeptius";
        asteriskPassword = "ccb6f130f89de0bab95df361669e32ba";

        dbAdress = "localhost:3306/sys";
        dbLogin = "user";
        dbPassword = "1234";
        dbTableName = "asterisk";

        standartNumber = "5555555";
        phoneTimeToRemoveInSeconds = 40;

        moreErrorsLogs = true;
        showPhoneRepeatedRequest = false;
        showSendingMailLogs = false;

        accessControlAllowOrigin = "http://e404.ho.ua";
    }
}
