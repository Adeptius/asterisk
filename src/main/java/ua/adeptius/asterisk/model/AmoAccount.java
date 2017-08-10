package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
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


    @Column(name = "nextelLogin", insertable = false, updatable = false)
    private String nextelLogin;

    @JsonProperty
    @Column(name = "amoLogin")
    private String amoLogin;

    @JsonProperty
    @Column(name = "apiKey")
    private String apiKey;

    @JsonProperty
    @Column(name = "domain")
    private String domain;

    @Column(name = "phoneId")
    private String phoneId;

    @Column(name = "phoneEnumId")
    private String phoneEnumId;

    @Column(name = "leadId")
    private int leadId;

    @ManyToOne
    @JoinColumn(name = "nextelLogin", referencedColumnName = "login")
    private User user;


//    public void addBinding(@Nonnull String worker, @Nonnull String phone) {
//        AmoPhoneBinding binding = new AmoPhoneBinding();
//        binding.setWorker(worker);
//        binding.setPhone(phone);
//        phoneBindings.add(binding);
//    }


//    public void setPhoneBindings(Set<AmoPhoneBinding> phoneBindings) {
//        this.phoneBindings = phoneBindings;
//    }

    @Nullable
    public String getWorkersPhone(String workerId){
        AmoOperatorLocation operatorLocation = user.getOperatorLocation();
        if (operatorLocation == null) {
            return null;
        }
        return operatorLocation.getAmoUserIdAndInnerNumber().get(workerId);


//        for (AmoPhoneBinding phoneBinding : phoneBindings) {
//            if (phoneBinding.getWorker().equals(workerId)){
//                return phoneBinding.getPhone();
//            }
//        }
//        return null;
    }

    @Nullable
    public String getWorkersId(String phone){
        AmoOperatorLocation operatorLocation = user.getOperatorLocation();
        if (operatorLocation == null) {
            return null;
        }
        return operatorLocation.getInnerNumberAndAmoUserId().get(phone);
//        for (AmoPhoneBinding phoneBinding : phoneBindings) {
//            if (phoneBinding.getPhone().equals(phone)){
//                return phoneBinding.getWorker();
//            }
//        }
//        return null;
    }

//    public Set<AmoPhoneBinding> getPhoneBindings() {
//        return phoneBindings;
//    }

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

//    public Set<AmoOperatorLocation> getOperatorLocations() {
//        return operatorLocations;
//    }

//    public void setOperatorLocations(Set<AmoOperatorLocation> operatorLocations) {
//        this.operatorLocations = operatorLocations;
//    }

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
//                ", operatorLocations=" + operatorLocations +
                '}';
    }
}
