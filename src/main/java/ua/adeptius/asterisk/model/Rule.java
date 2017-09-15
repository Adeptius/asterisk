package ua.adeptius.asterisk.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.exceptions.JsonParseException;
import ua.adeptius.asterisk.json.JsonChainElement;
import ua.adeptius.asterisk.json.JsonRule;
import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static ua.adeptius.asterisk.telephony.DestinationType.GSM;
import static ua.adeptius.asterisk.telephony.DestinationType.SIP;
import static ua.adeptius.asterisk.telephony.ForwardType.QUEUE;
import static ua.adeptius.asterisk.telephony.ForwardType.TO_ALL;


@Entity
@Table(name = "rules", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class Rule {

    private static Logger LOGGER = LoggerFactory.getLogger(Rule.class.getSimpleName());

    public Rule() {
    }


    // Конвертор из json обьекта в POJO в ScenarioWebController
    public Rule(JsonRule jsonRule) throws JsonParseException {
        String jName = jsonRule.getName();
        if (jName == null) {
            throw new JsonParseException("Rule name not set");
        }
        this.name = jName;

        String jType = jsonRule.getType();
        try {
            type = RuleType.valueOf(jType);
        } catch (Exception e) {
            throw new JsonParseException(name + ": wrong rule type: " + jType);
        }

        Integer jStartHour = jsonRule.getStartHour();
        if (jStartHour == null) {
            throw new JsonParseException(name + ": start hour not set: " + jStartHour);
        }
        this.startHour = jStartHour;

        Integer jEndHour = jsonRule.getEndHour();
        if (jEndHour == null) {
            throw new JsonParseException(name + ": end hour not set: " + jEndHour);
        }
        this.endHour = jEndHour;

        boolean[] jDays = jsonRule.getDays();
        if (jDays == null || jDays.length != 7) {
            throw new JsonParseException(name + ": wrong days");
        }
        setDays(jDays);

        String jMelody = jsonRule.getMelody();
        if (jMelody == null) {
            throw new JsonParseException(name + ": melody not set: " + jMelody);
        }
        melody = jsonRule.getMelody();

        greeting = jsonRule.getGreeting();
        message = jsonRule.getMessage();
        amoResponsibleId = jsonRule.getAmoResponsibleId();
        chain = new HashMap<>();


        for (Map.Entry<Integer, JsonChainElement> entry : jsonRule.getChain().entrySet()) {
            JsonChainElement jElement = entry.getValue();
            Integer number = entry.getKey();
            ChainElement element = new ChainElement();
            List<String> jToNumbers = jElement.getToList();
            try {
                element.setToList(jToNumbers);
            } catch (Exception e) {
                throw new JsonParseException(name + ": wrong ToList: " + jToNumbers);
            }

            String jForwardType = jElement.getForwardType();
            try {
                element.setForwardType(ForwardType.valueOf(jForwardType));
            } catch (Exception e) {
                throw new JsonParseException(name + ": wrong forward type: " + jForwardType);
            }

            String jDestinationType = jElement.getDestinationType();
            try {
                element.setDestinationType(DestinationType.valueOf(jDestinationType));
            } catch (Exception e) {
                throw new JsonParseException(name + ": wrong destination type: " + jDestinationType);
            }


            Integer jAwaitingTime = jElement.getAwaitingTime();
            if (jAwaitingTime == null) {
                throw new JsonParseException(name + ": awaiting time not set: " + jAwaitingTime);
            }
            element.setAwaitingTime(jAwaitingTime);

            chain.put(number, element);
        }
    }

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "login")
    private String login;

    @JsonProperty
    @Column(name = "name")
    private String name;

    @Column(name = "scenario")
    private String scenario;

    @JsonProperty
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private RuleType type;

    @JsonProperty
    @Column(name = "startTime")
    private Integer startHour;

    @JsonProperty
    @Column(name = "endTime")
    private Integer endHour;

    @JsonProperty
    @Column(name = "days")
    private String days;

    @JsonProperty
    @Column(name = "melody")
    private String melody;

    @JsonProperty
    @Column(name = "amo_responsible_id")
    private String amoResponsibleId;

    @JsonProperty
    @Column(name = "greeting")
    private Integer greeting;

    @JsonProperty
    @Column(name = "message")
    private Integer message;

    @ManyToOne
    @JoinColumn(name = "login", insertable = false, updatable = false)
    private User user;

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    /**
     * Chain
     */
    @Transient
    private HashMap<Integer, ChainElement> chain; // Это кэш.

    @JsonProperty
    public HashMap<Integer, ChainElement> getChain() {
        if (chain == null) {
            chain = new HashMap<>();
//        HashMap<Integer, ChainElement> chain = new HashMap<>();
            Set<ChainElement> ruleElements = user.getAllChainElements().stream()
                    .filter(element -> element.getRule().equals(name))
                    .collect(Collectors.toSet());
            for (ChainElement ruleElement : ruleElements) {
                chain.put(ruleElement.getPosition(), ruleElement);
            }
        }
        return chain;
    }

    public void addChainElement(ChainElement element) {
        element.setUser(user);
        element.setRule(name);
        user.saveInUsersChains(element);
    }

    public static final String AGI_ADDRESS = "localhost/in_processor.agi";

    public String getConfig(List<String> from) {
        ChainElement firstElement = getChain().get(0);

        DestinationType destinationType = firstElement.getDestinationType();
        ForwardType forwardType = firstElement.getForwardType();
        int awaitingTime = firstElement.getAwaitingTime();

        if (destinationType == SIP) {
            if (forwardType == TO_ALL) {
                awaitingTime = 600;
            }
        }

        List<String> to = firstElement.getToList();

        StringBuilder builder = new StringBuilder();
        builder.append("; '").append(scenario).append("' - '").append(name).append("' ").append(from).append("\n");

        for (int i = 0; i < from.size(); i++) {
            String numberFrom = removeZero(from.get(i)); // удаляем нолик
            builder.append("exten => ").append(numberFrom).append(",1,Noop(${CALLERID(num)})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Gosub(sub-record-check,s,1(in,${EXTEN},force))\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(__FROM_DID=${EXTEN})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(CDR(did)=${FROM_DID})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(num=${CALLERID(num)})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Agi(agi://").append(AGI_ADDRESS).append(")\n");

            if (destinationType == SIP) {
                for (String sipTo : to) {
                    builder.append("exten => ").append(numberFrom).append(",n,Dial(SIP/").append(sipTo).append(",").append(awaitingTime).append(")\n");
                }
            } else if (destinationType == GSM) {
                if (forwardType == QUEUE) { // По очереди
                    for (String numberTo : to) {
                        builder.append("exten => ").append(numberFrom).append(",n,Dial(SIP/Intertelekom_main/")
                                .append(numberTo).append(",").append(awaitingTime).append(",m(").append(melody).append("))\n");
                    }
                } else if (forwardType == TO_ALL) { // Сразу всем
                    builder.append("exten => ").append(numberFrom).append(",n,Dial(");
                    for (int j = 0; j < to.size(); j++) {
                        builder.append("SIP/Intertelekom_main/").append(to.get(j));
                        if (j != to.size() - 1) {
                            builder.append("&");
                        }
                    }
                    builder.append(",").append(600).append(",m(").append(melody).append("))\n");
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public void setDays(boolean[] newDays) {
        days = "";
        if (newDays[0]) days += "пн ";
        if (newDays[1]) days += "вт ";
        if (newDays[2]) days += "ср ";
        if (newDays[3]) days += "чт ";
        if (newDays[4]) days += "пт ";
        if (newDays[5]) days += "сб ";
        if (newDays[6]) days += "вс ";
    }

    @JsonProperty
    public boolean[] getDays() {
        return new boolean[]{
                days.contains("пн"),
                days.contains("вт"),
                days.contains("ср"),
                days.contains("чт"),
                days.contains("пт"),
                days.contains("сб"),
                days.contains("вс")
        };

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    private String removeZero(String source) {
        try {
            if (source.length() == 10 && source.startsWith("0")) {
                source = source.substring(1);
            }
        } catch (Exception e) {
            System.out.println("Ошибка добавления нолика. Пришло " + source);
        }
        return source;
    }

    public boolean isThisRuleCompatibleWith(Rule another) {
        LOGGER.debug("Проверка совместимости правила \n{}\nс правилом\n{}", this, another);

        boolean[] thisFirstDays = this.getDays();
        boolean[] anotherDays = another.getDays();

        boolean hasDaysMatch = false;
        for (int i = 0; i < 7; i++) {
            if (thisFirstDays[i] && anotherDays[i]) {
                hasDaysMatch = true;
            }
        }
        if (!hasDaysMatch) {
            LOGGER.debug("Совпадений по дням нет. Время не проверяем.");
            return true;
        }

        LOGGER.debug("Есть совпадения по дням. Проверяем часы активации.");

        int thisStartTime = this.getStartHour();
        int thisEndTime = this.getEndHour();

        int anotherStartTime = another.getStartHour();
        int anotherEndTime = another.getEndHour();

        // Проверяем временные диапазоны первого и второго сценария

        // Первый содержит второго или полное совпадение
        if (thisStartTime <= anotherStartTime && thisEndTime >= anotherEndTime) {
            LOGGER.debug("Правило \n{}\nсодержит диапазон правила\n{}", this, another);
            return false;
        }

        // Второй содержит первого
        if (thisStartTime >= anotherStartTime && thisEndTime <= anotherEndTime) {
            LOGGER.debug("Правило \n{}\nсодержит диапазон правила\n{}", another, this);
            System.out.println("Второй содержит первого или полное совпадение");
            return false;
        }

        // Первый начинается во время второго
        if (thisStartTime >= anotherStartTime && thisStartTime < anotherEndTime) {// первый может кончится в тоже время когда заканчивается второй
            LOGGER.debug("Диапазон правила \n{}\nначинается во время правила\n{}", this, another);
            System.out.println("Первый начинается во время второго");
            return false;
        }

        // Второй начинается во время первого
        if (anotherStartTime >= thisStartTime && anotherStartTime < thisEndTime) { // второй может кончится в тоже время когда начинается первый
            LOGGER.debug("Диапазон правила \n{}\nначинается во время правила\n{}", another, this);
            System.out.println("Второй начинается во время первого");
            return false;
        }

        return true;
    }

    public Integer getGreetingId() {
        return greeting;
    }

    public UserAudio getGreetingMelody() {
        if (greeting == null){
            return null;
        }
        return user.getUserAudio().stream().filter(melody -> melody.getId() == greeting).findFirst().orElse(null);
    }

    public void setGreetingId(Integer greeting) {
        this.greeting = greeting;
    }

    public Integer getMessageId() {
        return message;
    }

    public void setMessageId(Integer message) {
        this.message = message;
    }

    public UserAudio getMessageMelody(){
        if (message == null){
            return null;
        }
        return user.getUserAudio().stream().filter(melody -> melody.getId() == message).findFirst().orElse(null);
    }


    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startTime) {
        this.startHour = startTime;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endTime) {
        this.endHour = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    public String getMelody() {
        return melody;
    }

    public void setMelody(String melody) {
        this.melody = melody;
    }

    public void setUser(User user) {
        this.user = user;
        login = user.getLogin();
    }

    public String getAmoResponsibleId() {
        return amoResponsibleId;
    }

    public void setAmoResponsibleId(String amoResponsibleId) {
        this.amoResponsibleId = amoResponsibleId;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", scenario='" + scenario + '\'' +
                ", startHour=" + startHour +
                ", endHour=" + endHour +
                ", days='" + days + '\'' +
                ", amoResponsibleId='" + amoResponsibleId + '\'' +
                ", user=" + user.getLogin() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;

        if (id != rule.id) return false;
        if (login != null ? !login.equals(rule.login) : rule.login != null) return false;
        if (name != null ? !name.equals(rule.name) : rule.name != null) return false;
        return scenario != null ? scenario.equals(rule.scenario) : rule.scenario == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (login != null ? login.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (scenario != null ? scenario.hashCode() : 0);
        return result;
    }
}
