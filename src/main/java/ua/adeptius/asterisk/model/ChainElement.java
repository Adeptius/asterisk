package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;


@Entity
@Table(name = "chains", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class ChainElement {

    private static Logger LOGGER = LoggerFactory.getLogger(ChainElement.class.getSimpleName());

    public ChainElement() {
    }

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "login")
    private String login;

    @Column(name = "rule")
    private String rule;

    @Column(name = "position")
    private Integer position;

    @Column(name = "toNumbers")
    private String toNumbers;

    @JsonProperty
    @Column(name = "forwardType")
    @Enumerated(EnumType.STRING)
    private ForwardType forwardType;

    @JsonProperty
    @Column(name = "destinationType")
    @Enumerated(EnumType.STRING)
    private DestinationType destinationType;

    @JsonProperty
    @Column(name = "awaitingTime")
    private int awaitingTime;

    @JsonProperty
    @Column(name = "melody")
    private String melody;

    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "login", insertable = false, updatable = false)
    private User user;

    public static final String AGI_ADDRESS = "78.159.55.63/hello.agi";


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

//    public void removeFromFromList(@Nonnull String number) {
//        List<String> fromList = getFromList();
//        fromList.remove(number);
//        setFromList(fromList);
//    }

//    public void setFromList(@Nonnull List<String> numbers) {
//        clearFromList();
//        for (String number : numbers) {
//            addToFromList(number);
//        }
//    }

//    public void clearFromList() {
//        fromNumbers = "";
//    }
//
//    public void addToFromList(@Nonnull String number) {
//        if (!fromNumbers.equals("")) {
//            fromNumbers += " ";
//        }
//        if (fromNumbers.contains(number)) {
//            return;
//        }
//        fromNumbers += number;
//    }
//
//    public List<String> getFromList() {
//        if (fromNumbers.equals("")) {
//            return new ArrayList<>();
//        }
//        String[] splitted = fromNumbers.split(" ");
//        return new ArrayList<>(Arrays.asList(splitted));
//    }


    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
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

    @JsonProperty
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setUser(User user) {
        this.user = user;
        login = user.getLogin();
    }
}