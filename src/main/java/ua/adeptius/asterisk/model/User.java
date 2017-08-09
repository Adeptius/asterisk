package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;


@Entity
@Table(name = "users", schema = "calltrackdb")
@JsonAutoDetect(
        creatorVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        fieldVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        getterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        setterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE
)
public class User {

    private static Logger LOGGER = LoggerFactory.getLogger(User.class.getSimpleName());

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "nextelLogin", referencedColumnName = "login")
    private Set<AmoAccount> amoAccountSet;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "nextelLogin", referencedColumnName = "login")
    private Set<RoistatAccount> roistatAccountSet;

    // при ALL удаляется внешний телефон при удалении пользователя
    @JsonProperty // при DETACH не становится null site_name при вызове site.releaseAllPhones
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "busy", referencedColumnName = "login")
    private Set<OuterPhone> outerPhones;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "busy", referencedColumnName = "login")
    private Set<InnerPhone> innerPhones;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "login", referencedColumnName = "login")
    private Set<Site> sites;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "login", referencedColumnName = "login")
    private Set<AmoOperatorLocation> amoOperatorLocations;

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

    public void removeSite(Site site){
        site.releaseAllPhones();
        getSites().remove(site);
    }


    public Set<OuterPhone> getOuterPhones() {
        return outerPhones;
    }

    public void setOuterPhones(Set<OuterPhone> outerPhones) {
        this.outerPhones = outerPhones;
    }

    public Set<InnerPhone> getInnerPhones() {
        return innerPhones;
    }

    public void setInnerPhones(Set<InnerPhone> innerPhones) {
        this.innerPhones = innerPhones;
    }

    public Set<Site> getSites() {
        if (sites == null) {
            return new HashSet<>();
        }
        return sites;
    }

    public void setSites(Set<Site> sites) {
        this.sites = sites;
    }

    public static void setLOGGER(Logger LOGGER) {
        User.LOGGER = LOGGER;
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

    public Set<AmoOperatorLocation> getAmoOperatorLocations() {
        return amoOperatorLocations;
    }

    public void setAmoOperatorLocations(Set<AmoOperatorLocation> amoOperatorLocations) {
        this.amoOperatorLocations = amoOperatorLocations;
    }


    @Override
    public String toString() {
        return "User{" +
                "\n login='" + login + '\'' +
                "\n password='" + password + '\'' +
                "\n email='" + email + '\'' +
                "\n trackingId='" + trackingId + '\'' +
                "\n amoAccountSet=" + amoAccountSet +
                "\n roistatAccountSet=" + roistatAccountSet +
                "\n outerPhones=" + outerPhones +
                "\n innerPhones=" + innerPhones +
                "\n operatorLocations=" + amoOperatorLocations +
                "\n sites=" + sites +
                "\n}";
    }

    public Site getSiteByName(String sitename) {
        for (Site site : sites) {
            if (site.getName().equals(sitename)) {
                return site;
            }
        }
        return null;
    }
    /**
     * Scenario
     */

//    public List<Scenario> getScenarios() {
//        return scenarios;
//    }
//
//    public void setScenarios(List<Scenario> scenarios) {
//        this.scenarios = scenarios;
//    }
//
//    public void addScenario(Scenario newScenario) throws ScenarioConflictException {
//        if (getScenarios().stream().map(Scenario::getName).anyMatch(s -> s.equals(newScenario.getName()))) {
//            throw new ScenarioConflictException("Scenario with such name already present");
//        }
//        if (newScenario.getStatus() == ScenarioStatus.ACTIVATED){
//            throw new ScenarioConflictException("Can't add activated scenario");
//        }
//        getScenarios().add(newScenario);
//    }
//
//    public void activateScenario(int id) throws ScenarioConflictException {
//        Scenario scenario;
//        try {
//            scenario = getScenarioById(id);
//        } catch (NoSuchElementException e) {
//            throw new ScenarioConflictException("Сценарий c id " + id + " не найден");
//        }
//
//        List<String> numbers = scenario.getFromList(); // Это список номеров нового сценария.
//        // нужно по каждому номеру, содержащимся в нём найти его сценарии и каждый проверить на конфликты с новым сценарием.
//        for (String number : numbers) {
//            Set<Scenario> scenariosByNumber = getActivatedScenariosByOuterPhoneNumber(number);
//            for (Scenario userScenario : scenariosByNumber) {
//                if (!userScenario.isThisScenarioCompatibleWith(scenario)) { // если один из существующих сценариев не совместим с новым.
//                    throw new ScenarioConflictException("For number '" + number + "' assigned active scenario '"
//                            + userScenario.getName() + "' that has time conflict with '" + scenario.getName() + "'");
//                }
//            }
//        }
//        scenario.setStatus(ScenarioStatus.ACTIVATED);
//    }
//
//    public void deactivateScenario(int id) throws NoSuchElementException {
//        getScenarioById(id).setStatus(ScenarioStatus.DEACTIVATED);
//    }
//
//    public Scenario getScenarioById(int id) throws NoSuchElementException {
//        return getScenarios().stream().filter(scenario -> scenario.getId() == id).findFirst().get();
//    }
//
//    private Set<Scenario> getActivatedScenariosByOuterPhoneNumber(String number) {
//        return getScenariosByOuterPhoneNumber(number).stream().filter(scenario -> scenario.getStatus() == ScenarioStatus.ACTIVATED).collect(Collectors.toSet());
//    }
//
//    private Set<Scenario> getDeactivatedScenariosByOuterPhoneNumber(String number) {
//        return getScenariosByOuterPhoneNumber(number).stream().filter(scenario -> scenario.getStatus() == ScenarioStatus.DEACTIVATED).collect(Collectors.toSet());
//    }
//
//    private Set<Scenario> getScenariosByOuterPhoneNumber(String number) {
//        Set<Scenario> foundedScenarios = new HashSet<>(); // сюда ложим сценарии, которые связаны с данным номером
//        for (Scenario scenario : scenarios) {
//            List<String> phones = scenario.getFromList();// взяли у сценария список его номеров.
//            for (String phone : phones) {
//                if (phone.equals(number)) { // если есть совпадение
//                    foundedScenarios.add(scenario); // ложим сценарий в найденные
//                    break; // прерываем цикл и переходим к следующему сценарию
//                }
//            }
//        }
//        return foundedScenarios;
//    }

//    @JsonIgnore
//    public List<String> getAvailableNumbers() {
//        List<String> numbers = new ArrayList<>();
//        if (telephony != null) {
//            numbers.addAll(telephony.getAvailableNumbers());
//        }
//        if (tracking != null) {
//            numbers.addAll(tracking.getAvailableNumbers());
//        }
//        return numbers;
//    }
//
//    public boolean isThatUsersOuterNumber(@Nonnull List<String> numbers) {
//        for (String number : numbers) {
//            if (isThatUsersOuterNumber(number)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean isThatUsersOuterNumber(@Nonnull String number) {
//        List<String> allOuterPhones = new ArrayList<>();
//        if (getTelephony() != null) {
//            allOuterPhones.addAll(getTelephony().getOuterPhonesList());
//        }
//        if (getTracking() != null) {
//            allOuterPhones.addAll(getTracking().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList()));
//        }
//        return allOuterPhones.stream().anyMatch(s -> s.equals(number));
//    }
//
//    public boolean isThatUsersInnerNumber(@Nonnull List<String> numbers) {
//        for (String number : numbers) {
//            if (isThatUsersInnerNumber(number)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public boolean isThatUsersInnerNumber(@Nonnull String number) {
//        if (getTelephony() != null) {
//            return getTelephony().getInnerPhonesList().stream().anyMatch(s -> s.equals(number));
//        } else {
//            return false;
//        }
//    }
//
//
}
