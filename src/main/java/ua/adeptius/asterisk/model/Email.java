package ua.adeptius.asterisk.model;


public class Email {

    private String email;
    private String key;
    private String userLogin;
    private EmailType emailType;

//    public Email(String email, String key, String userLogin) {
//        this.email = email;
//        this.key = key;
//        this.userLogin = userLogin;
//    }

    public Email(PendingUser pendingUser) {
        this.email = pendingUser.getEmail();
        this.key = pendingUser.getKey();
        this.userLogin = pendingUser.getLogin();
        emailType = EmailType.REGISTRATION;
    }

    public static enum EmailType{
        REGISTRATION
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }
}
