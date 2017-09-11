package ua.adeptius.asterisk.test;

/**
 * Методы доступные через JMX Console
 */
public interface OptionsMBean {

    void setProfilingEnabled(boolean profilingEnabled);
    boolean isProfilingEnabled();

    void setItsLinux(boolean itsLinux);
    boolean isItsLinux();

    boolean isUseLocalDb();
    void setUseLocalDb(boolean useLocalDb);

    String getFolderRules();
    String getFolderSips();
    String getServerAddress();
    String getFolderUserMusic();
    String getAsteriskPassword();
    String getAsteriskLogin();
    String getAsteriskUrl();

    int getSecondsToUpdatePhoneOnWebPage();
    void setSecondsToUpdatePhoneOnWebPage(int secondsToUpdatePhoneOnWebPage);

    int getSecondsToRemoveOldPhones();
    void setSecondsToRemoveOldPhones(int secondsToRemoveOldPhones);

    int getMailAntiSpam();
    void setMailAntiSpam(int mailAntiSpam);
}
