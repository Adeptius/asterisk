package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "amo_accounts", schema = "calltrackdb")
public class AmoAccount {

    @Id
    @Column(name = "nextelLogin")
    private String nextelLogin;

    @Column(name = "amoLogin")
    private String amoLogin;

    @Column(name = "apiKey")
    private String apiKey;

    @Column(name = "domain")
    private String domain;

    @Column(name = "phoneId")
    private String phoneId;

    @Column(name = "phoneEnumId")
    private String phoneEnumId;

    @Column(name = "leadId")
    private int leadId;

    @JsonIgnore
    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    public int getLeadId() {
        return leadId;
    }

    public void setLeadId(int leadId) {
        this.leadId = leadId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNextelLogin() {
        return nextelLogin;
    }

    public void setNextelLogin(String nextelLogin) {
        this.nextelLogin = nextelLogin;
    }

    public String getAmoLogin() {
        return amoLogin;
    }

    public void setAmoLogin(String amoLogin) {
        this.amoLogin = amoLogin;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getPhoneEnumId() {
        return phoneEnumId;
    }

    public void setPhoneEnumId(String phoneEnumId) {
        this.phoneEnumId = phoneEnumId;
    }

    @Override
    public String toString() {
        return "AmoAccount{" +
                "nextelLogin='" + nextelLogin + '\'' +
                ", amoLogin='" + amoLogin + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", domain='" + domain + '\'' +
                ", phoneId='" + phoneId + '\'' +
                ", phoneEnumId='" + phoneEnumId + '\'' +
                ", user=" + user.getLogin() +
                '}';
    }
}
