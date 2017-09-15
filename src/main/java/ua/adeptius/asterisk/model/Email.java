package ua.adeptius.asterisk.model;


public class Email {

    private String email;
    private String hash;
    private String userLogin;
    private String siteName;
    private EmailType emailType;

    public Email(RegisterQuery registerQuery) {
        this.email = registerQuery.getEmail();
        this.hash = registerQuery.getHash();
        this.userLogin = registerQuery.getLogin();
        emailType = EmailType.REGISTRATION;
    }

    public Email(RecoverQuery recoverQuery) {
        this.email = recoverQuery.getEmail();
        this.hash = recoverQuery.getHash();
        this.userLogin = recoverQuery.getLogin();
        emailType = EmailType.RECOVER;
    }

    public Email(EmailType emailType, String email, String sitename, String userLogin) {
        this.email = email;
        this.emailType = emailType;
        this.siteName = sitename;
        this.userLogin = userLogin;
    }

    public static enum EmailType{
        REGISTRATION,
        RECOVER,
        NO_OUTER_PHONES_LEFT
    }


    public EmailType getEmailType() {
        return emailType;
    }

    public void setEmailType(EmailType emailType) {
        this.emailType = emailType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }
}
