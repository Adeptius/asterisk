package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
//import javax.persistence.*;
import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Entity
@Table(name = "amo_accounts", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class AmoAccount implements Serializable {

    public AmoAccount() {
    }

    public AmoAccount(String amoLogin, String apiKey, String domain) {
        this.amoLogin = amoLogin;
        this.apiKey = apiKey;
        this.domain = domain;
    }

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;


    @Column(name = "nextel_login")
    private String nextelLogin;

    @JsonProperty
    @Column(name = "amo_login")
    private String amoLogin;

    @JsonProperty
    @Column(name = "api_key")
    private String apiKey;

    @JsonProperty
    @Column(name = "domain")
    private String domain;

    @Column(name = "phone_id")
    private String phoneId;

    @Column(name = "phone_enum_id")
    private String phoneEnumId;

    @Column(name = "api_user_id")
    private String apiUserId;

    @Column(name = "lead_id")
    private int leadId;//todo хрень

    @JsonProperty
    @Column(name = "cling")
    private Boolean cling;

    @ManyToOne
    @JoinColumn(name = "nextel_login", insertable = false, updatable = false)
    private User user;

    @Nullable
    public String getWorkersPhone(String workerId){
        AmoOperatorLocation operatorLocation = user.getOperatorLocation();
        if (operatorLocation == null) {
            return null;
        }
        return operatorLocation.getAmoUserIdAndInnerNumber().get(workerId);
    }

    @Nullable
    public String getWorkersId(String phone){
        AmoOperatorLocation operatorLocation = user.getOperatorLocation();
        if (operatorLocation == null) {
            return null;
        }
        return operatorLocation.getInnerNumberAndAmoUserId().get(phone);
    }

    public int getLeadId() {
        return leadId;
    }

    @Nullable
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null){
            nextelLogin = user.getLogin();
        }
        this.user = user;
    }

    public String getApiUserId() {
        return apiUserId;
    }

    public void setApiUserId(String apiUserId) {
        this.apiUserId = apiUserId;
    }

    @Nonnull
    public String getAmoLogin() {
        return amoLogin;
    }

    public void setAmoLogin(@Nonnull String amoLogin) {
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

    public boolean isCling() {
        if (cling == null){
            return false;
        }
        return cling;
    }

    public void setCling(boolean cling) {
        this.cling = cling;
    }

    @Override
    public String toString() {
        return "AmoAccount{" +
                "id=" + id +
                ", nextelLogin='" + nextelLogin + '\'' +
                ", amoLogin='" + amoLogin + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", domain='" + domain + '\'' +
                ", phoneId='" + phoneId + '\'' +
                ", phoneEnumId='" + phoneEnumId + '\'' +
                ", leadId=" + leadId +
                ", user=" + user.getLogin() +
                '}';
    }
}
