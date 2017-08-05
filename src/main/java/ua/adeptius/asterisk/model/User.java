package ua.adeptius.asterisk.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.exceptions.ScenarioConflictException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;
import java.util.stream.Collectors;


@Entity
@Table(name = "users", schema = "calltrackdb")
public class User {

    private static Logger LOGGER = LoggerFactory.getLogger(User.class.getSimpleName());

    @Id
    @Column(name = "login")
    private String login;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "trackingId")
    private String trackingId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "nextelLogin", referencedColumnName = "login")

    private Set<AmoAccount> amoAccountSet;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "nextelLogin", referencedColumnName = "login")
    private Set<RoistatAccount> roistatAccountSet;

    @OneToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
    @JoinColumn(name = "busy", referencedColumnName = "login")
    private Set<OuterPhone> outerPhones;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "busy", referencedColumnName = "login")
    private Set<InnerPhone> innerPhones;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "login", referencedColumnName = "login")
    private Set<Site> sites;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "login", referencedColumnName = "login")
    private Set<AmoOperatorLocation> operatorLocations;

    @Transient
    public AmoAccount getAmoAccount() {
        if (amoAccountSet == null || amoAccountSet.isEmpty())
            return null;
        else
            return this.amoAccountSet.iterator().next();
    }

    public void setAmoAccount(AmoAccount amoAccount) {
        // add the item as the (only) item.
        if (this.amoAccountSet == null)
            this.amoAccountSet = new HashSet<AmoAccount>();
        else
            this.amoAccountSet.clear();

        if (amoAccount == null) {
            return;
        }

        this.amoAccountSet.add(amoAccount);
    }

    @Transient
    public RoistatAccount getRoistatAccount() {
        if (roistatAccountSet == null || roistatAccountSet.isEmpty())
            return null;
        else
            return this.roistatAccountSet.iterator().next();
    }

    public void setRoistatAccount(RoistatAccount roistatAccount) {
        // add the item as the (only) item.
        if (this.roistatAccountSet == null)
            this.roistatAccountSet = new HashSet<RoistatAccount>();
        else
            this.roistatAccountSet.clear();

        if (roistatAccountSet == null) {
            return;
        }

        //todo может здесь сразу привязывать юзера к ройстату?
        this.roistatAccountSet.add(roistatAccount);
    }

    public static Logger getLOGGER() {
        return LOGGER;
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

    public Set<AmoOperatorLocation> getOperatorLocations() {
        return operatorLocations;
    }

    public void setOperatorLocations(Set<AmoOperatorLocation> operatorLocations) {
        this.operatorLocations = operatorLocations;
    }


    //    public AmoAccount getAmoAccount() {
//        return amoAccount;
//    }
//
//    public void setAmoAccount(AmoAccount amoAccount) {
//        this.amoAccount = amoAccount;
//    }

//    public RoistatAccount getRoistatAccount() {
//        return roistatAccount;
//    }

//    public void setRoistatAccount(RoistatAccount roistatAccount) {
//        this.roistatAccount = roistatAccount;
//    }

//    public List<OuterPhone> getOuterPhones() {
//        return outerPhones;
//    }
//
//    public void setOuterPhones(List<OuterPhone> outerPhones) {
//        this.outerPhones = outerPhones;
//    }
//
//    public List<InnerPhone> getInnerPhones() {
//        return innerPhones;
//    }
//
//    public void setInnerPhones(List<InnerPhone> innerPhones) {
//        this.innerPhones = innerPhones;
//    }


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
                "\n operatorLocations=" + operatorLocations +
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


    //    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
//    @PrimaryKeyJoinColumn
//    private Tracking tracking;

//    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
//    @PrimaryKeyJoinColumn
//    private Telephony telephony;

//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
//    @JoinColumn(name = "login", referencedColumnName = "login")
//    private List<Scenario> scenarios;


    /**
     * Reloading. Synk with DB
     */

//    public void reloadScenariosFromDb() {
//        try {
//            List<Scenario> scenariosFromDB = HibernateDao.getAllScenariosByUser(this);
//            setScenarios(scenariosFromDB);
//            LOGGER.debug("{}: Синхронизация с БД - {} сценариев перезагружены", login, scenariosFromDB.size());
//        } catch (Exception e) {
//            LOGGER.error(login + ": Синхронизация с БД - ошибка синхронизации сценариев", e);
//        }
//    }
//
//
//    public void reloadAmoAccountFromDb() {
//        try {
//            AmoAccount amoAccount = HibernateDao.getAmoAccountByUser(this);
//            setAmoAccount(amoAccount);
//            LOGGER.debug("{}: Синхронизация с БД - AmoAccount перезагружен", login);
//        } catch (Exception e) {
//            LOGGER.error(login + ": Синхронизация с БД - ошибка синхронизации AmoAccount", e);
//        }
//    }
//
//    public void reloadRoistatAccountFromDb() {
//        try {
//            RoistatAccount roistatAccount = HibernateDao.getRoistatAccountByUser(this);
//            setRoistatAccount(roistatAccount);
//            LOGGER.debug("{}: Синхронизация с БД - Roistat account перезагружен", login);
//        } catch (Exception e) {
//            LOGGER.error(login + ": Синхронизация с БД - ошибка синхронизации Roistat", e);
//        }
//    }
//
//    public void reloadTrackingFromDb() {
//        try {
//            Tracking tracking = HibernateDao.getTrackingByUser(this);
//            setTracking(tracking);
//            if (tracking != null) {
//                tracking.updateNumbers();
//            }
//            LOGGER.debug("{}: Синхронизация с БД - трекинг перезагружен", login);
//        } catch (Exception e) {
//            LOGGER.error(login + ": Синхронизация с БД - ошибка синхронизации трекинга", e);
//        }
//    }
//
//    public void reloadTelephonyFromDb() {
//        try {
//            Telephony telephony = HibernateDao.getTelephonyByUser(this);
//            setTelephony(telephony);
//            if (telephony != null) {
//                telephony.updateNumbers();
//            }
//            LOGGER.debug("{}: Синхронизация с БД - телефония перезагружена", login);
//        } catch (Exception e) {
//            LOGGER.error(login + ": Синхронизация с БД - ошибка синхронизации телефонии", e);
//        }
//    }


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


    /**
     * Numbers
     */

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
//    @Nullable
//    public Telephony getTelephony() {
//        return telephony;
//    }
//
//    public void setTelephony(Telephony telephony) {
//        this.telephony = telephony;
//    }
//
//    @Nullable
//    public Tracking getTracking() {
//        return tracking;
//    }
//
//    public void setTracking(Tracking tracking) {
//        this.tracking = tracking;
//    }
//
//    public String getLogin() {
//        return login;
//    }
//
//    public void setLogin(String login) {
//        this.login = login;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    @Nullable
//    public String getTrackingId() {
//        return trackingId;
//    }
//
//    public void setTrackingId(String trackingId) {
//        this.trackingId = trackingId;
//    }
//
//    public AmoAccount getAmoAccount() {
//        return amoAccount;
//    }
//
//    public void setAmoAccount(AmoAccount amoAccount) {
//        this.amoAccount = amoAccount;
//    }
//
//    public RoistatAccount getRoistatAccount() {
//        return roistatAccount;
//    }
//
//    public void setRoistatAccount(RoistatAccount roistatAccount) {
//        this.roistatAccount = roistatAccount;
//    }
//
//
//    @Override
//    public String toString() {
//        return "User{" +
//                "\n  login='" + login + '\'' +
//                "\n  password='" + password + '\'' +
//                "\n  email='" + email + '\'' +
//                "\n  trackingId='" + trackingId + '\'' +
//                "\n  tracking=" + tracking +
//                "\n  telephony=" + telephony +
//                "\n  amoAccount=" + amoAccount +
//                "\n  roistatAccount=" + roistatAccount +
//                "\n  scenarios=" + scenarios +
//                "\n}";
//    }
}
