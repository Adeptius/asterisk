package ua.adeptius.asterisk.model;


import java.util.ArrayList;

public class TelephonyCustomer extends Customer{

    public final CustomerType type = CustomerType.TELEPHONY;
    private ArrayList<String> innerPhones = new ArrayList<>();
    private ArrayList<String> outerPhones = new ArrayList<>();

    public TelephonyCustomer(String name, String eMail, String googleAnalyticsTrackingId, String password,
                             ArrayList<String> innerPhones, ArrayList<String> outerPhones) {
        super(name, eMail, googleAnalyticsTrackingId, password);
        this.innerPhones = innerPhones;
        this.outerPhones = outerPhones;
    }


    public ArrayList<String> getInnerPhones() {
        return innerPhones;
    }

    public void setInnerPhones(ArrayList<String> innerPhones) {
        this.innerPhones = innerPhones;
    }

    public ArrayList<String> getOuterPhones() {
        return outerPhones;
    }

    public void setOuterPhones(ArrayList<String> outerPhones) {
        this.outerPhones = outerPhones;
    }

    @Override
    public String toString() {
        return "TelephonyCustomer{" +
                "name='" + name + '\'' +
                ", eMail='" + eMail + '\'' +
                ", googleAnalyticsTrackingId='" + googleAnalyticsTrackingId + '\'' +
                ", password='" + password + '\'' +
                ", rules=" + rules +
                ", innerPhones=" + innerPhones +
                ", outerPhones=" + outerPhones +
                "} ";
    }
}
