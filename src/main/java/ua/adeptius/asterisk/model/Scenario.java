package ua.adeptius.asterisk.model;

import javax.annotation.Nonnull;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ua.adeptius.asterisk.telephony.DestinationType.GSM;
import static ua.adeptius.asterisk.telephony.DestinationType.SIP;
import static ua.adeptius.asterisk.telephony.ForwardType.QUEUE;
import static ua.adeptius.asterisk.telephony.ForwardType.TO_ALL;


@Entity
@Table(name = "scenarios", schema = "calltrackdb")
public class Scenario {

    private static Logger LOGGER = LoggerFactory.getLogger(Scenario.class.getSimpleName());

    public Scenario() {
    }

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @JsonIgnore
    @Column(name = "login")
    private String login;

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @Column(name = "fromNumbers")
    private String fromNumbers;

    @Column(name = "toNumbers")
    private String toNumbers;

    @Column(name = "forwardType")
    @Enumerated(EnumType.STRING)
    private ForwardType forwardType;

    @Column(name = "destinationType")
    @Enumerated(EnumType.STRING)
    private DestinationType destinationType;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ScenarioStatus status;

    @Column(name = "awaitingTime")
    private int awaitingTime;

    @Column(name = "melody")
    private String melody;

    @Column(name = "startTime")
    private Integer startHour;

    @Column(name = "endTime")
    private Integer endHour;

    @Column(name = "days")
    private String days;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "login", insertable = false, updatable = false)
    private User user;


    @SuppressWarnings("Duplicates")
    @JsonIgnore
    public String getConfig() {
        if (destinationType == SIP) {
            if (forwardType == TO_ALL) {
                awaitingTime = 600;
            }
        }

        List<String> from = getFromList();
        List<String> to = getToList();

        StringBuilder builder = new StringBuilder();
        builder.append("; Start Rule\n");
        for (int i = 0; i < from.size(); i++) {
            String numberFrom = removeZero(from.get(i)); // удаляем нолик
            builder.append("exten => ").append(numberFrom).append(",1,Noop(${CALLERID(num)})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Gosub(sub-record-check,s,1(in,${EXTEN},force))\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(__FROM_DID=${EXTEN})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(CDR(did)=${FROM_DID})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(num=${CALLERID(num)})\n");

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

        if (builder.toString().endsWith("\n")) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("; End Rule\n");
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

    public void removeFromFromList(@Nonnull String number) {
        List<String> fromList = getFromList();
        fromList.remove(number);
        setFromList(fromList);
    }

    public void setFromList(@Nonnull List<String> numbers) {
        clearFromList();
        for (String number : numbers) {
            addToFromList(number);
        }
    }

    public void clearFromList() {
        fromNumbers = "";
    }

    public void addToFromList(@Nonnull String number) {
        if (!fromNumbers.equals("")) {
            fromNumbers += " ";
        }
        if (fromNumbers.contains(number)) {
            return;
        }
        fromNumbers += number;
    }

    public List<String> getFromList() {
        if (fromNumbers.equals("")) {
            return new ArrayList<>();
        }
        String[] splitted = fromNumbers.split(" ");
        return new ArrayList<>(Arrays.asList(splitted));
    }


    public void removeFromToList(@Nonnull String number) {
        List<String> toList = getToList();
        toList.remove(number);
        setToList(toList);
    }

    public void setToList(@Nonnull List<String> numbers) {
        clearToList();
        for (String number : numbers) {
            addToToList(number);
        }
    }

    public void clearToList() {
        toNumbers = "";
    }

    public void addToToList(@Nonnull String number) {
        if (!toNumbers.equals("")) {
            toNumbers += " ";
        }
        if (toNumbers.contains(number)) {
            return;
        }
        toNumbers += number;
    }

    public List<String> getToList() {
        if (toNumbers.equals("")) {
            return new ArrayList<>();
        }
        String[] splitted = toNumbers.split(" ");
        return new ArrayList<>(Arrays.asList(splitted));
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

    public boolean isThisScenarioCompatibleWith(Scenario another) {
        LOGGER.debug("Проверка совместимости сценария \n{}\nсо сценарием\n{}", this, another);

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

        //FIXME добавить поддержку ночных сценариев или дефолтных или сценариев либо сценарий "в данный момент никто не работает".

        // Проверяем временные диапазоны первого и второго сценария

        // Первый содержит второго или полное совпадение
        if (thisStartTime <= anotherStartTime && thisEndTime >= anotherEndTime) {
            LOGGER.debug("Сценарий \n{}\nсодержит диапазон сценария\n{}", this, another);
            return false;
        }

        // Второй содержит первого
        if (thisStartTime >= anotherStartTime && thisEndTime <= anotherEndTime) {
            LOGGER.debug("Сценарий \n{}\nсодержит диапазон сценария\n{}", another, this);
            System.out.println("Второй содержит первого или полное совпадение");
            return false;
        }

        // Первый начинается во время второго
        if (thisStartTime >= anotherStartTime && thisStartTime < anotherEndTime) {// первый может кончится в тоже время когда заканчивается второй
            LOGGER.debug("Диапазон сценария \n{}\nначинается во время сценария\n{}", this, another);
            System.out.println("Первый начинается во время второго");
            return false;
        }

        // Второй начинается во время первого
        if (anotherStartTime >= thisStartTime && anotherStartTime < thisEndTime) { // второй может кончится в тоже время когда начинается первый
            LOGGER.debug("Диапазон сценария \n{}\nначинается во время сценария\n{}", another, this);
            System.out.println("Второй начинается во время первого");
            return false;
        }

        return true;
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

    public String getFromNumbers() {
        return fromNumbers;
    }

    public void setFromNumbers(String from) {
        this.fromNumbers = from;
    }

    public ForwardType getForwardType() {
        return forwardType;
    }

    public void setForwardType(ForwardType forwardType) {
        this.forwardType = forwardType;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public int getAwaitingTime() {
        return awaitingTime;
    }

    public void setAwaitingTime(int awaitingTime) {
        this.awaitingTime = awaitingTime;
    }

    public String getMelody() {
        return melody;
    }

    public void setMelody(String melody) {
        this.melody = melody;
    }

    public User getUser() {
        return user;
    }

    public ScenarioStatus getStatus() {
        return status;
    }

    public void setStatus(ScenarioStatus status) {
        this.status = status;
    }

    public void setUser(User user) {
        this.user = user;
        login = user.getLogin();
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", fromNumbers='" + fromNumbers + '\'' +
                ", toNumbers='" + toNumbers + '\'' +
                ", forwardType=" + forwardType +
                ", destinationType=" + destinationType +
                ", awaitingTime=" + awaitingTime +
                ", melody='" + melody + '\'' +
                ", startTime=" + startHour +
                ", endTime=" + endHour +
                ", days='" + days + '\'' +
                ", user=" + user.getLogin() +
                '}';
    }
}
