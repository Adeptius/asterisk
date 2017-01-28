package ua.adeptius.asterisk.model;


import java.util.List;

public class Site {

    public Site(String name, String accessControlAllowOrigin, List<Phone> phones, String standartNumber, String googleAnalyticsTrackingId, String eMail) {
        this.name = name;
        this.accessControlAllowOrigin = accessControlAllowOrigin;
        this.phones = phones;
        this.standartNumber = standartNumber;
        this.googleAnalyticsTrackingId = googleAnalyticsTrackingId;
        this.eMail = eMail;
    }

    private String name;
    private String accessControlAllowOrigin;
    private List<Phone> phones;
    private String standartNumber;
    private String googleAnalyticsTrackingId;
    private String eMail;

    public String getName() {
        return name;
    }

    public String getAccessControlAllowOrigin() {
        return accessControlAllowOrigin;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public String getStandartNumber() {
        return standartNumber;
    }

    public String getGoogleAnalyticsTrackingId() {
        return googleAnalyticsTrackingId;
    }

    public String geteMail() {
        return eMail;
    }

    @Override
    public String toString() {
        return "Site{" +
                "name='" + name + '\'' +
                ", accessControlAllowOrigin='" + accessControlAllowOrigin + '\'' +
                ", phones=" + phones +
                ", standartNumber='" + standartNumber + '\'' +
                ", googleAnalyticsTrackingId='" + googleAnalyticsTrackingId + '\'' +
                ", eMail='" + eMail + '\'' +
                '}';
    }
}
