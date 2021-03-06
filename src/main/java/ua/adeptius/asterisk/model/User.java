package ua.adeptius.asterisk.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.telephony.*;
import ua.adeptius.asterisk.monitor.Scheduler;

import javax.annotation.Nonnull;
import javax.persistence.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;


@Entity
@Table(name = "users", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class User implements Serializable {

    private static Logger LOGGER = LoggerFactory.getLogger(User.class.getSimpleName());

    public User() {
    }

    public User(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
        outerPhones = new HashSet<>();
        innerPhones = new HashSet<>();
        chainElements = new HashSet<>();
        scenarios = new HashSet<>();
        rules = new HashSet<>();
        sites = new HashSet<>();
        userAudio = new HashSet<>();
        phoneGroups = new HashSet<>();
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
    @Column(name = "user_phone_number")
    private String userPhoneNumber;

    @JsonProperty
    @Column(name = "first_name")
    private String firstName;

    @JsonProperty
    @Column(name = "last_name")
    private String lastName;

    @JsonProperty
    @Column(name = "middle_name")
    private String middleName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nextelLogin", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<AmoAccount> amoAccountSet;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nextelLogin", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<RoistatAccount> roistatAccountSet;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "busy", fetch = FetchType.EAGER)
    private Set<OuterPhone> outerPhones;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "busy", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<InnerPhone> innerPhones;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Site> sites;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<AmoOperatorLocation> amoOperatorLocations;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Rule> rules;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Scenario> scenarios;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<ChainElement> chainElements;

    //    @JoinColumn(name = "login", referencedColumnName = "login")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<UserAudio> userAudio;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "login", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<PhoneGroup> phoneGroups;

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


    public void addOuterPhones(Collection<OuterPhone> outerPhones) {
        this.outerPhones.addAll(outerPhones);
        for (OuterPhone outerPhone : outerPhones) {
            outerPhone.setBusy(login);
        }
        outerPhonesCache = null;
        // изменено количество внешних номеров. Обновляем Кэши
//        Scheduler.reloadOuterOnNextScheduler();
    }


    public void removeOuterPhones(Collection<OuterPhone> outerPhonesToRemove) {
        this.outerPhones.removeAll(outerPhonesToRemove);
        for (OuterPhone outerPhone : outerPhonesToRemove) {
            outerPhone.setBusy(null);
        }
        outerPhonesCache = null;
        // изменено количество внешних номеров. Обновляем Кэши
        Scheduler.rewriteRulesFilesForThisUserAtNextScheduler(this);
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

    public Stream<InnerPhone> getInnerPhonesStream() {
        return innerPhones.stream();
    }

    public void addInnerPhones(Collection<InnerPhone> innerPhones) {
        this.innerPhones.addAll(innerPhones);
        innerPhonesCache = null;
    }

    public void removeInnerPhones(Collection<InnerPhone> innerPhonesToRemove) {
        this.innerPhones.removeAll(innerPhonesToRemove);
        innerPhonesCache = null;
        // так как у пользователя удалён сип номер - он мог остаться в каких-то правилах.
        // потом этот номер, в принципе может попасть другому пользователю.

//        innerPhonesToRemove.stream().map(InnerPhone::getNumber).forEach(number -> {
//            chainElements.forEach(chainElement -> chainElement.getToList().remove(number));
//        });

        //удалять как бы надо, но есть вероятность оставить при этом пустую цепочку, что везде всё потом поламает.
//        for (InnerPhone innerPhone : innerPhonesToRemove) {
//            String number = innerPhone.getNumber();
//            for (ChainElement chainElement : chainElements) {
//                chainElement.removeFromToList(number);
//            }
//        }
    }



    /**
     * Phone Group
     */

    public Set<PhoneGroup> getPhoneGroups() {
        return Collections.unmodifiableSet(phoneGroups);
    }

    public PhoneGroup getPhoneGroupByName(String name) {
        return phoneGroups.stream().filter(pg -> pg.getName().equals(name)).findFirst().orElse(null);
    }

//    public void setPhoneGroups(Set<PhoneGroup> phoneGroups) {
//        this.phoneGroups = phoneGroups;
//    }

    public void addPhoneGroup(PhoneGroup phoneGroup){
        phoneGroup.setUser(this);
        phoneGroups.add(phoneGroup);
//        phoneGroups.remove(phoneGroup);
    }

    public void removePhoneGroup(PhoneGroup phoneGroup){
        phoneGroups.remove(phoneGroup);
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
        Scheduler.reloadDialPlanForThisUserAtNextScheduler(this);
    }

    /**
    * Этот метод нужен при изменении сценария. Он оставляет айдишник сценария в телефоне,
    * что бы после создания нового сценария (взамен удалённому) он по-прежнему ссылался на него.
     * Если надо полностью удалить сценарий - используется метод removeScenario()
    **/
    public void removeScenarioButLeaveIdInPhone(Scenario scenario) {
        List<Rule> rulesInScenario = scenario.getRules(); // прежде чем удалить сценарий - сначала надо удалить все его правила

        for (Rule rule : rulesInScenario) {
            HashMap<Integer, ChainElement> chain = rule.getChain();// прежде чем удалить правило - надо удалить всю цепочку

            for (ChainElement element : chain.values()) {
                removeChainElement(element);// удалили у пользователя
            }
            removeRule(rule);
        }

//        rulesInScenario.forEach(rule -> {
//            HashMap<Integer, ChainElement> chain = rule.getChain();// прежде чем удалить правило - надо удалить всю цепочку
//            chain.values().forEach(element -> removeChainElement(element)); // удалили у пользователя
//            removeRule(rule);
//        });
        scenarios.remove(scenario);
    }

    public void removeScenario(Scenario scenario) {
        removeScenarioButLeaveIdInPhone(scenario);
        getOuterPhones().stream()
                .filter(phone -> (phone.getScenarioId() != null && phone.getScenarioId() == scenario.getId()))
                .forEach(phone -> phone.setScenarioId(null));
        Scheduler.reloadDialPlanForThisUserAtNextScheduler(this);
    }


    /**
     * Rules
     */

    public Set<Rule> getAllRules() {
        return Collections.unmodifiableSet(rules);
    }

    public Set<Rule> getRules() { // TODO УБРАТЬ!!!!!
        return rules;
    }

    public void saveInUsersRules(Rule rule) {
        rules.add(rule);
    }

    public void setRules(Set<Rule> rules) {
        this.rules = rules;
    }

    private void removeRule(Rule rule) {
        rules.remove(rule);
    }

    /**
     * Chain
     */
    public Set<ChainElement> getAllChainElements() {
        return Collections.unmodifiableSet(chainElements);
    }

    public void saveInUsersChains(ChainElement element) {
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

    public void addUserAudio(UserAudio audio) {
        audio.setLogin(login);
        userAudio.add(audio);
    }

    public void removeUserAudio(UserAudio audio) {
        userAudio.remove(audio);
    }



//    public boolean isThatAllUsersSipNumbers(@Nonnull List<String> numbers) {
//        for (String number : numbers) {
//            if (!isThatUserSipNumber(number)) {
//                return false;
//            }
//        }
//        return true;
//    }

    public boolean isThatUserSipNumber(@Nonnull String number) {
        return getInnerPhoneByNumber(number) != null;
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

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
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



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return login != null ? login.equals(user.login) : user.login == null;
    }

    @Override
    public int hashCode() {
        return login != null ? login.hashCode() : 0;
    }
}
