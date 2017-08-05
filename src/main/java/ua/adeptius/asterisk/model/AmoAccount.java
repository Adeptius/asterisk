package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "amo_accounts", schema = "calltrackdb")
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
    @JsonIgnore
    private String nextelLogin;

    @Column(name = "amoLogin")
    private String amoLogin;

    @Column(name = "apiKey")
    private String apiKey;

    @Column(name = "domain")
    private String domain;

    @Column(name = "phoneId")
    @JsonIgnore
    private String phoneId;

    @Column(name = "phoneEnumId")
    @JsonIgnore
    private String phoneEnumId;

    @Column(name = "leadId")
    private int leadId; //TODO Реализовать возможность выбора

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "nextelLogin", referencedColumnName = "login")
    private User user;

//    @Transient




//    public void addBinding(@Nonnull String worker, @Nonnull String phone) {
//        AmoPhoneBinding binding = new AmoPhoneBinding();
//        binding.setWorker(worker);//TODO проверки на то что такое уже есть
//        binding.setPhone(phone);  //TODO проверка внутренний ли это номер пользователя
//        phoneBindings.add(binding);
//    }


//    public void setPhoneBindings(Set<AmoPhoneBinding> phoneBindings) {
//        this.phoneBindings = phoneBindings;
//    }

//    @Nullable
//    public String getWorkersPhone(String workerId){
//        for (AmoPhoneBinding phoneBinding : phoneBindings) {
//            if (phoneBinding.getWorker().equals(workerId)){
//                return phoneBinding.getPhone();
//            }
//        }
//        return null;
//    }

//    @Nullable
//    public String getWorkersId(String phone){
//        for (AmoPhoneBinding phoneBinding : phoneBindings) {
//            if (phoneBinding.getPhone().equals(phone)){
//                return phoneBinding.getWorker();
//            }
//        }
//        return null;
//    }

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
    public String getNextelLogin() {
        return nextelLogin;
    }


    public void setNextelLogin(@Nonnull String nextelLogin) {
        this.nextelLogin = nextelLogin;
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
