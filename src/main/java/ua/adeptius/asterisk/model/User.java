package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;


@Entity
@Table(name = "users", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class User implements Serializable{

    private static Logger LOGGER = LoggerFactory.getLogger(User.class.getSimpleName());

    public User() {
    }

    public User(String login, String password, String email, String trackingId) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.trackingId = trackingId;
        outerPhones = new HashSet<>();
        innerPhones = new HashSet<>();
        chainElements = new HashSet<>();
        scenarios = new HashSet<>();
        rules = new HashSet<>();
        sites = new HashSet<>();
        userAudio = new HashSet<>();
    }

    @Id
    @Column(name = "login")
    @JsonProperty
    private String login;


    @Column(name = "password")
    private String password;

    @JsonProperty
    @Column(name = "email")
    private String email;

    @JsonProperty
    @Column(name = "trackingId")
    private String trackingId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nextelLogin", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<AmoAccount> amoAccountSet;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nextelLogin", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<RoistatAccount> roistatAccountSet;

    //    @JoinColumn(name = "busy", referencedColumnName = "login")
    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "busy", fetch = FetchType.EAGER)
    private Set<OuterPhone> outerPhones;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "busy", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<InnerPhone> innerPhones;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
//    @JoinColumn(name = "login", referencedColumnName = "login")
    private Set<Site> sites;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<AmoOperatorLocation> amoOperatorLocations;

    //    @JoinColumn(name = "login", referencedColumnName = "login")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Rule> rules;

    //    @JoinColumn(name = "login", referencedColumnName = "login")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Scenario> scenarios;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<ChainElement> chainElements;

    //    @JoinColumn(name = "login", referencedColumnName = "login")
    @OneToMany(cascade = CascadeType.ALL,  mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<UserAudio> userAudio;

    /**
     * AmoCRM
     */
    @JsonProperty
    @Transient
    public AmoAccount getAmoAccount() {
        if (amoAccountSet == null || amoAccountSet.isEmpty())
            return null;
        else
            return this.amoAccountSet.iterator().next();
    }

    public void setAmoAccount(AmoAccount amoAccount) {
        if (this.amoAccountSet == null)
            this.amoAccountSet = new HashSet<>();
        else
            this.amoAccountSet.clear();

        if (amoAccount == null) {
            return;
        }

        amoAccount.setUser(this);
        this.amoAccountSet.add(amoAccount);
    }

    /**
     * Roistat
     */
    @JsonProperty
    @Transient
    public RoistatAccount getRoistatAccount() {
        if (roistatAccountSet == null || roistatAccountSet.isEmpty())
            return null;
        else
            return this.roistatAccountSet.iterator().next();
    }

    public void setRoistatAccount(RoistatAccount roistatAccount) {
        if (this.roistatAccountSet == null)
            this.roistatAccountSet = new HashSet<>();
        else
            this.roistatAccountSet.clear();

        if (roistatAccount == null) {
            return;
        }

        roistatAccount.setUser(this);
        this.roistatAccountSet.add(roistatAccount);
    }

    /**
     * Sites
     */
    public void removeSite(Site site) {
        site.releaseAllPhones();
        sites.remove(site);
    }

    public Site getSiteByName(String sitename) {
        for (Site site : sites) {
            if (site.getName().equals(sitename)) {
                return site;
            }
        }
        return null;
    }

    public Set<Site> getSites() {
        return Collections.unmodifiableSet(sites);
    }

    public void addSite(Site site) {
        site.setUser(this);
        sites.add(site);
    }

    /**
     * Outer phones
     */

    @Transient
    private HashMap<String, OuterPhone> outerPhonesCache;

    public OuterPhone getOuterPhoneByNumber(String number) {
        if (outerPhonesCache == null) { // если кэш пуст - наполняем его
            outerPhonesCache = new HashMap<>();
            outerPhones.forEach(phone -> outerPhonesCache.put(phone.getNumber(), phone));
        }
        return outerPhonesCache.get(number);
    }

    public Set<OuterPhone> getOuterPhones() {
        return Collections.unmodifiableSet(outerPhones);
    }

    public void setOuterPhones(Set<OuterPhone> outerPhones) { // todo где это используется?
        this.outerPhones = outerPhones;
        outerPhonesCache = null;
    }

    public void addOuterPhones(Collection<OuterPhone> outerPhones) {
        this.outerPhones.addAll(outerPhones);
        for (OuterPhone outerPhone : outerPhones) {
            outerPhone.setBusy(login);
        }
        outerPhonesCache = null;
    }


    public void removeOuterPhones(Collection<OuterPhone> outerPhonesToRemove) {
        this.outerPhones.removeAll(outerPhonesToRemove);
        for (OuterPhone outerPhone : outerPhonesToRemove) {
            outerPhone.setBusy(null);
        }
        outerPhonesCache = null;
    }


    /**
     * Inner Phones
     */


    @Transient
    private HashMap<String, InnerPhone> innerPhonesCache;

    public InnerPhone getInnerPhoneByNumber(String number) {
        if (innerPhonesCache == null) { // если кэш пуст - наполняем его
            innerPhonesCache = new HashMap<>();
            innerPhones.forEach(phone -> innerPhonesCache.put(phone.getNumber(), phone));
        }
        return innerPhonesCache.get(number);
    }

    public Set<InnerPhone> getInnerPhones() {
        return Collections.unmodifiableSet(innerPhones);
    }

//    public void setInnerPhones(Set<InnerPhone> innerPhones) {
//        this.innerPhones = innerPhones;
//        innerPhonesCache = null;
//    }


    public void addInnerPhones(Collection<InnerPhone> innerPhones) {
        this.innerPhones.addAll(innerPhones);
        innerPhonesCache = null;
    }


    public void removeInnerPhones(Collection<InnerPhone> innerPhonesToRemove) {
        this.innerPhones.removeAll(innerPhonesToRemove);
        innerPhonesCache = null;
    }


    /**
     * Operator locations
     */
    @JsonProperty
    @Transient
    public AmoOperatorLocation getOperatorLocation() {
        if (amoOperatorLocations == null || amoOperatorLocations.isEmpty())
            return null;
        else
            return this.amoOperatorLocations.iterator().next();
    }

    public void setAmoOperatorLocations(AmoOperatorLocation amoOperatorLocation) {
        if (this.amoOperatorLocations == null)
            this.amoOperatorLocations = new HashSet<>();
        else
            this.amoOperatorLocations.clear();

        if (amoOperatorLocation == null) {
            return;
        }

        amoOperatorLocation.setUser(this);
//        amoOperatorLocation.setLogin(login);
        this.amoOperatorLocations.add(amoOperatorLocation);
    }


    /**
     * Scenarios
     */

    public Set<Scenario> getScenarios() {
        return Collections.unmodifiableSet(scenarios);
    }

    public Scenario getScenarioById(int id) {
        return scenarios.stream()
                .filter(scenario -> id == scenario.getId())
                .findFirst()
                .orElse(null);
    }

    public Scenario getScenarioByName(String name) {
        return scenarios.stream()
                .filter(scenario -> name.equals(scenario.getName()))
                .findFirst()
                .orElse(null);
    }

    public void addScenario(Scenario scenario) {
        scenario.setUser(this);
        scenarios.add(scenario);
    }

    public void removeScenarioButLeaveIdInPhone(Scenario scenario) { // этот метод нужен при изменении сценария
        List<Rule> rulesInScenario = scenario.getRules(); // прежде чем удалить сценарий - сначала надо удалить все его правила
        rulesInScenario.forEach(rule -> {
            HashMap<Integer, ChainElement> chain = rule.getChain();// прежде чем удалить правило - надо удалить всю цепочку
            chain.values().forEach(element -> removeChainElement(element)); // удалили у пользователя
            removeRule(rule);
        });
        scenarios.remove(scenario);
    }

    public void removeScenario(Scenario scenario) {
        removeScenarioButLeaveIdInPhone(scenario);
        getOuterPhones().stream()
                .filter(phone -> (phone.getScenarioId() != null && phone.getScenarioId() == scenario.getId()))
                .forEach(phone -> phone.setScenarioId(null));
    }


    /**
     * Rules
     */

    public Set<Rule> getAllRules() {
        return Collections.unmodifiableSet(rules);
    }

    void saveInUsersRules(Rule rule) { // todo это должно быть в scenario
        rules.add(rule);
    }

    void removeRule(Rule rule) {
        rules.remove(rule);
    }

    /**
     * Chain
     */
    public Set<ChainElement> getAllChainElements() {
        return Collections.unmodifiableSet(chainElements);
    }

    void saveInUsersChains(ChainElement element) { //todo это должно быть в rule
        chainElements.add(element);
    }

    void removeChainElement(ChainElement element) {
        chainElements.remove(element);
    }

    /**
     * User Audio
     */
    public Set<UserAudio> getUserAudio() {
        return Collections.unmodifiableSet(userAudio);
    }

    public void addUserAudio(UserAudio audio){
        audio.setLogin(login);
        userAudio.add(audio);
    }

    public void removeUserAudio(UserAudio audio){
        userAudio.remove(audio);
    }



    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", trackingId='" + trackingId + '\'' +
                ", amoAccount=" + getAmoAccount() +
                ", roistatAccount=" + getRoistatAccount() +
                ", outerPhones=" + outerPhones +
                ", innerPhones=" + innerPhones +
                ", sites=" + sites +
                ", amoOperatorLocations=" + amoOperatorLocations +
                ", rules=" + rules +
                ", scenarios=" + scenarios +
                ", userAudio=" + getUserAudio() +
                ", outerPhonesCache=" + outerPhonesCache +
                ", innerPhonesCache=" + innerPhonesCache +
                '}';
    }

    public boolean isThatAllUsersSipNumbers(@Nonnull List<String> numbers) {
        for (String number : numbers) {
            if (!isThatUserSipNumber(number)) {
                return false;
            }
        }
        return true;
    }

    public boolean isThatUserSipNumber(@Nonnull String number) {
        return getInnerPhoneByNumber(number) != null;
    }
}
