package ua.adeptius.asterisk.test;

import org.springframework.stereotype.Component;

@Component
public class Options implements OptionsMBean {

    private boolean profilingEnabled;
    private boolean itsLinux;
    private boolean useLocalDb;
    private int secondsToUpdatePhoneOnWebPage = 5;
    private int secondsToRemoveOldPhones = 7;
    private int mailAntiSpam = 60;


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
