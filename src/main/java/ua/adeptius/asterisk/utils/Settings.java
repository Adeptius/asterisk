package ua.adeptius.asterisk.utils;



public class Settings {

    public static String asteriskAdress;
    public static String asteriskLogin;
    public static String asteriskPassword;

    public static String dbAdress;
    public static String dbLogin;
    public static String dbPassword;
    public static String dbTableName;
    public static String dbColumnPhoneName;
    public static String dbColumnGoogleIdName;
    public static String dbColumnTimeToDieName;

    public static String standartNumber;
    public static int phoneTimeToRemoveInSeconds;

    public static boolean showDetailedErrorsLogs;
    public static boolean showPhoneRepeatedRequest;
    public static boolean showSendingMailLogs;
    public static boolean showAllCallsToAsterisk;
    public static boolean showAllListOfFreeNumbers;

    public static String accessControlAllowOrigin;
    public static String googleAnalyticsTrackingId;
    public static String googleAnalyticsCategoryName;
    public static String googleAnalyticsEventName;


    static {
        asteriskAdress = "194.44.37.30";
        asteriskLogin = "adeptius";
        asteriskPassword = "ccb6f130f89de0bab95df361669e32ba";

        dbAdress = "localhost:3306/sys";
        dbLogin = "user";
        dbPassword = "1234";
        dbTableName = "asterisk";
        dbColumnPhoneName = "phone";
        dbColumnGoogleIdName = "googleid";
        dbColumnTimeToDieName = "time_left";

        standartNumber = "5555555";
        phoneTimeToRemoveInSeconds = 40;

        showDetailedErrorsLogs = false;
        showPhoneRepeatedRequest = false;
        showSendingMailLogs = false;
        showAllCallsToAsterisk = true;
        showAllListOfFreeNumbers = true;

        accessControlAllowOrigin = "http://e404.ho.ua";
        googleAnalyticsTrackingId = "UA-88866926-1";
        googleAnalyticsCategoryName = "calltracking";
        googleAnalyticsEventName = "new call";
    }
}
