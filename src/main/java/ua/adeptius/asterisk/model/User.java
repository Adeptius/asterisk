package ua.adeptius.asterisk.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.exceptions.ScenarioConflictException;
import ua.adeptius.asterisk.telephony.OldRule;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.nio.file.NoSuchFileException;
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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Tracking tracking;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Telephony telephony;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private AmoAccount amoAccount;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private RoistatAccount roistatAccount;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "login", referencedColumnName = "login")
    List<Scenario> scenarios;

    @Transient
//    private List<OldRule> oldRules = new ArrayList<>();


    public List<Scenario> getScenarios() {
        return scenarios;
    }

//    public void setScenarios(List<Scenario> scenarios) {
//        this.scenarios = scenarios;
//    }

    public void addScenario(Scenario newScenario) throws ScenarioConflictException {
        if (getScenarios().stream().map(Scenario::getName).anyMatch(s -> s.equals(newScenario.getName()))) {
            throw new ScenarioConflictException("Сценарий с таким именем уже существует");
        }
        // TODO проследить что бы с фронтенда не добавили уже активированный сценарий
        getScenarios().add(newScenario);
    }

    public void activateScenario(int id) throws ScenarioConflictException {
        Scenario scenario;
        try {
             scenario = getScenarioById(id);
        } catch (NoSuchElementException e) {
            throw new ScenarioConflictException("Сценарий c id "+id+" не найден");
        }

        List<String> numbers = scenario.getFromList(); // Это список номеров нового сценария.
        // нужно по каждому номеру, содержащимся в нём найти его сценарии и каждый проверить на конфликты с новым сценарием.
        for (String number : numbers) {
            Set<Scenario> scenariosByNumber = getActivatedScenariosByOuterPhoneNumber(number);
            for (Scenario userScenario : scenariosByNumber) {
                if (!userScenario.isThisScenarioCompatibleWith(scenario)) { // если один из существующих сценариев не совместим с новым.
                    throw new ScenarioConflictException("Внешнему номеру " + number + " назначен сценарий \""
                            + userScenario.getName() + "\" который имеет пересекающиеся диапазоны времени с \"" + scenario.getName() + "\"");
                }
            }
        }
        scenario.setStatus(ScenarioStatus.ACTIVATED); // TODO Протестить
    }

    private Scenario getScenarioById(int id){ // TODO Протестить
        return getScenarios().stream().filter(scenario -> scenario.getId() == id).findFirst().get();
    }

    private Set<Scenario> getActivatedScenariosByOuterPhoneNumber(String number) {
        return getScenariosByOuterPhoneNumber(number).stream().filter(scenario -> scenario.getStatus() == ScenarioStatus.ACTIVATED).collect(Collectors.toSet());
    }

  private Set<Scenario> getDeactivatedScenariosByOuterPhoneNumber(String number) {
        return getScenariosByOuterPhoneNumber(number).stream().filter(scenario -> scenario.getStatus() == ScenarioStatus.DEACTIVATED).collect(Collectors.toSet());
    }


    private Set<Scenario> getScenariosByOuterPhoneNumber(String number) {
        Set<Scenario> foundedScenarios = new HashSet<>(); // сюда ложим сценарии, которые связаны с данным номером
        for (Scenario scenario : scenarios) {
            List<String> phones = scenario.getFromList();// взяли у сценария список его номеров.
            for (String phone : phones) {
                if (phone.equals(number)) { // если есть совпадение
                    foundedScenarios.add(scenario); // ложим сценарий в найденные
                    break; // прерываем цикл и переходим к следующему сценарию
                }
            }
        }
        return foundedScenarios;
    }

//    public void loadRules() {
//        try {
//            LOGGER.trace("{}: загрузка правил", login);
//            oldRules = RulesConfigDAO.readFromFile(login);
//        } catch (NoSuchFileException ignored) {
//        } catch (Exception e) {
//            LOGGER.error(login + ": ошибка загрузки правил", e);
//        }
//    }

//    public void saveRules() throws Exception {
//        if (oldRules.size() == 0) {
//            RulesConfigDAO.removeFile(login);
//        } else {
//            RulesConfigDAO.writeToFile(login, oldRules);
//        }
//    }

    @JsonIgnore
    public List<String> getAvailableNumbers() {
        List<String> numbers = new ArrayList<>();
        if (telephony != null) {
            numbers.addAll(telephony.getAvailableNumbers());
        }
        if (tracking != null) {
            numbers.addAll(tracking.getAvailableNumbers());
        }
        return numbers;
    }

//    public List<OldRule> getOldRules() {
//        return oldRules;
//    }

//    public void setOldRules(List<OldRule> oldRules) {
//        this.oldRules = oldRules;
//    }

    public Telephony getTelephony() {
        return telephony;
    }

    public void setTelephony(Telephony telephony) {
        this.telephony = telephony;
    }

    public Tracking getTracking() {
        return tracking;
    }

    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
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
        if (trackingId != null) {
            return trackingId;
        } else {
            return "";
        }
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }


    public AmoAccount getAmoAccount() {
        return amoAccount;
    }

    public void setAmoAccount(AmoAccount amoAccount) {
        this.amoAccount = amoAccount;
    }

    public RoistatAccount getRoistatAccount() {
        return roistatAccount;
    }

    public void setRoistatAccount(RoistatAccount roistatAccount) {
        this.roistatAccount = roistatAccount;
    }


    @Override
    public String toString() {
        return "User{" +
                "\n  login='" + login + '\'' +
                "\n  password='" + password + '\'' +
                "\n  email='" + email + '\'' +
                "\n  trackingId='" + trackingId + '\'' +
                "\n  tracking=" + tracking +
                "\n  telephony=" + telephony +
                "\n  amoAccount=" + amoAccount +
                "\n  roistatAccount=" + roistatAccount +
                "\n  scenarios=" + scenarios +
//                "\n  oldRules=" + oldRules +
                "\n}";
    }
}
