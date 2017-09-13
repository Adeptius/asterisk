package ua.adeptius.asterisk.dao;

/**
 * Методы доступные через JMX Console
 */
public interface SettingsMBean {

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

    boolean isShowProfilingResultNow();
    void setShowProfilingResultNow(boolean showProfilingResultNow);

    boolean isSenderAmoWSMessageSenderEnabled();
    void setSenderAmoWSMessageSenderEnabled(boolean senderAmoWSMessageSenderEnabled);
    boolean isSenderAmoCallSenderEnabled();
    void setSenderAmoCallSenderEnabled(boolean senderAmoSenderEnabled);
}
