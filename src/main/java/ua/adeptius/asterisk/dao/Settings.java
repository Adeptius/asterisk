package ua.adeptius.asterisk.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Settings implements SettingsMBean {

    private static Logger LOGGER =  LoggerFactory.getLogger(Settings.class.getSimpleName());

    private static HashMap<String, String> map = new HashMap<>();

    public static void load(Class clazz){
        LOGGER.trace("Загрузка файла настроек config.properties");
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
            LOGGER.error("Ошибка загрузки config.properties", e);
            throw new RuntimeException("Ошибка загрузки config.properties");
        }
    }

    private static void initSettings(Properties prop) {
        for (final String name: prop.stringPropertyNames())
            map.put(name, prop.getProperty(name));
    }

    public static boolean getSettingBoolean(String category) {
        return Boolean.parseBoolean(map.get(category.toString()));
    }

    public static void setSetting(String key, String value){
        map.put(key, value);
        Properties prop = new Properties();
        try {
            String filename = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"config.properties";
            OutputStream output = new FileOutputStream(filename);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                prop.setProperty(entry.getKey(), entry.getValue());
            }
            prop.store(output, null);
//            System.out.println(key + " " + value);
        } catch (Exception io) {
            io.printStackTrace();
        }
    }

    public static String getSetting(String name){
        return map.get(name);
    }

    private boolean profilingEnabled;
    private boolean itsLinux;
    private boolean remoteServerIsUp;
    private boolean useLocalDb = false;
    private boolean showProfilingResultNow;
    private int secondsToUpdatePhoneOnWebPage = 5;
    private int secondsToRemoveOldPhones = 7;
    private int mailAntiSpam = 60;

    private boolean callToAmoWSEnabled = true;
    private boolean callToAmoEnabled = true;
    private boolean callToGoogleAnalyticsEnabled = true;
    private boolean callToRoistatEnabled = true;
    private boolean callToDBEnabled = true;


    public boolean isCallToDBEnabled() {
        return callToDBEnabled;
    }

    public void setCallToDBEnabled(boolean callToDBEnabled) {
        this.callToDBEnabled = callToDBEnabled;
    }

    public boolean isCallToRoistatEnabled() {
        return callToRoistatEnabled;
    }

    public void setCallToRoistatEnabled(boolean callToRoistatEnabled) {
        this.callToRoistatEnabled = callToRoistatEnabled;
    }

    public boolean isCallToGoogleAnalyticsEnabled() {
        return callToGoogleAnalyticsEnabled;
    }

    public void setCallToGoogleAnalyticsEnabled(boolean callToGoogleAnalyticsEnabled) {
        this.callToGoogleAnalyticsEnabled = callToGoogleAnalyticsEnabled;
    }

    public boolean isCallToAmoWSEnabled() {
        return callToAmoWSEnabled;
    }

    public void setCallToAmoWSEnabled(boolean callToAmoWSEnabled) {
        this.callToAmoWSEnabled = callToAmoWSEnabled;
    }

    public boolean isCallToAmoEnabled() {
        return callToAmoEnabled;
    }

    public void setCallToAmoEnabled(boolean senderAmoSenderEnabled) {
        this.callToAmoEnabled = senderAmoSenderEnabled;
    }

    public boolean isRemoteServerIsUp() {
        return remoteServerIsUp;
    }

    public void setRemoteServerIsUp(boolean remoteServerIsUp) {
        this.remoteServerIsUp = remoteServerIsUp;
    }

    public boolean isShowProfilingResultNow() {
        return showProfilingResultNow;
    }

    public void setShowProfilingResultNow(boolean showProfilingResultNow) {
        this.showProfilingResultNow = showProfilingResultNow;
    }

    public int getSecondsToUpdatePhoneOnWebPage() {
        return secondsToUpdatePhoneOnWebPage;
    }

    public void setSecondsToUpdatePhoneOnWebPage(int secondsToUpdatePhoneOnWebPage) {
        this.secondsToUpdatePhoneOnWebPage = secondsToUpdatePhoneOnWebPage;
    }

    public int getSecondsToRemoveOldPhones() {
        return secondsToRemoveOldPhones;
    }

    public void setSecondsToRemoveOldPhones(int secondsToRemoveOldPhones) {
        this.secondsToRemoveOldPhones = secondsToRemoveOldPhones;
    }

    public int getMailAntiSpam() {
        return mailAntiSpam;
    }

    public void setMailAntiSpam(int mailAntiSpam) {
        this.mailAntiSpam = mailAntiSpam;
    }

    public boolean isItsLinux() {
        return itsLinux;
    }

    public void setItsLinux(boolean itsLinux) {
        this.itsLinux = itsLinux;
    }

    public boolean isUseLocalDb() {
        return useLocalDb;
    }

    public void setUseLocalDb(boolean useLocalDb) {
        this.useLocalDb = useLocalDb;
    }

    public boolean isProfilingEnabled() {
        return profilingEnabled;
    }

    public void setProfilingEnabled(boolean profilingEnabled) {
        this.profilingEnabled = profilingEnabled;
    }

    public String getDbUsername() {
        if (useLocalDb){
            return "root";
        }else {
            return "adapteus";
        }
    }

    public String getDbPassword() {
        if (useLocalDb){
            return "357159";
        }else {
            return "adapteus4k";
        }
    }

    public String getDbUrl() {
        if (useLocalDb){
            return "jdbc:mysql://localhost:3306/";
        }else {
            return "jdbc:mysql://cstat.nextel.com.ua:3306/";
        }
    }

    public String getAsteriskPassword() {
        return "ccb6f130f89de0bab95df361669e32ba";
    }

    public String getAsteriskLogin() {
        return "adeptius";
    }

    public String getAsteriskUrl() {
        return "cstat.nextel.com.ua";
    }

    public String getServerAddress() {
        if (itsLinux){
            return "cstat.nextel.com.ua:8443";
        }else {
            return "adeptius.pp.ua:8443";
        }
    }

    public String getFolderUserMusic() {
        if (itsLinux){
            return "/var/lib/asterisk/sounds/user/";
        }else {
            return "D:\\home\\adeptius\\tomcat\\usermusic\\";
        }
    }

    public String getFolderSips() {
        if (itsLinux){
            return "/etc/asterisk/sip_clients/";
        }else {
            return "D:\\home\\adeptius\\tomcat\\sips\\";
        }
    }

    public String getFolderRules() {
        if (itsLinux){
            return "/var/www/html/admin/modules/core/etc/clients/";
        }else {
            return "D:\\home\\adeptius\\tomcat\\rules\\";
        }
    }
}
