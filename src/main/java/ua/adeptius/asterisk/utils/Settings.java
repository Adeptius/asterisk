package ua.adeptius.asterisk.utils;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

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

    public static void load(Class clazz){
        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            InputStream inputStream = clazz.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
                initSettings(prop);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initSettings(Properties prop) {
        asteriskAdress = prop.getProperty("asteriskAdress");
        asteriskLogin = prop.getProperty("asteriskLogin");
        asteriskPassword = prop.getProperty("asteriskPassword");

        dbAdress = prop.getProperty("dbAdress");
        dbLogin = prop.getProperty("dbLogin");
        dbPassword = prop.getProperty("dbPassword");
        dbTableName = prop.getProperty("dbTableName");
        dbColumnPhoneName = prop.getProperty("dbColumnPhoneName");
        dbColumnGoogleIdName = prop.getProperty("dbColumnGoogleIdName");
        dbColumnTimeToDieName = prop.getProperty("dbColumnTimeToDieName");

        standartNumber = prop.getProperty("standartNumber");
        phoneTimeToRemoveInSeconds = Integer.parseInt(prop.getProperty("phoneTimeToRemoveInSeconds"));

        showDetailedErrorsLogs = prop.getProperty("showDetailedErrorsLogs").equals("true");
        showPhoneRepeatedRequest = prop.getProperty("showPhoneRepeatedRequest").equals("true");
        showSendingMailLogs = prop.getProperty("showSendingMailLogs").equals("true");
        showAllCallsToAsterisk = prop.getProperty("showAllCallsToAsterisk").equals("true");
        showAllListOfFreeNumbers = prop.getProperty("showAllListOfFreeNumbers").equals("true");

        accessControlAllowOrigin = prop.getProperty("accessControlAllowOrigin");
        googleAnalyticsTrackingId = prop.getProperty("googleAnalyticsTrackingId");
        googleAnalyticsCategoryName = prop.getProperty("googleAnalyticsCategoryName");
        googleAnalyticsEventName = prop.getProperty("googleAnalyticsEventName");
    }
}
