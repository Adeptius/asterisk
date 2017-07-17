package ua.adeptius.asterisk.model;


import com.sun.istack.internal.NotNull;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "scenarios", schema = "calltrackdb")
public class Scenario {



    public Scenario() {
    }

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "login")
    private String login;

    @Column(name = "name")
    private String name;

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

    @Column(name = "awaitingTime")
    private int awaitingTime;

    @Column(name = "melody")
    private String melody;

    @Column(name = "startTime") // todo какое значение по умолчанию?
    private int startTime;

    @Column(name = "endTime")
    private int endTime;


    @Column(name = "days")
    private String days;

    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "login", insertable = false, updatable = false)
    private User user;

    public void setDays(boolean[] newDays){
        days = "";
        if (newDays[0]) days+= "пн ";
        if (newDays[1]) days+= "вт ";
        if (newDays[2]) days+= "ср ";
        if (newDays[3]) days+= "чт ";
        if (newDays[4]) days+= "пт ";
        if (newDays[5]) days+= "сб ";
        if (newDays[6]) days+= "вс ";
    }

    public boolean[] getDays(){
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

    //    public boolean isTodayIsOk(){
//        int currentDay = new Date().getDay(); // 1 - понедельник, 7 - воскресенье
//        return days[currentDay-1];
//    }

//    public boolean isNowIsOk(){
//        return false;
//    }


    public void removeFromFromList(@NotNull String number){
        List<String> fromList = getFromList();
        fromList.remove(number);
        setFromList(fromList);
    }

    public void setFromList(@NotNull List<String> numbers){
        clearFromList();
        for (String number : numbers) {
            addToFromList(number);
        }
    }

    public void clearFromList(){
        fromNumbers = "";
    }

    public void addToFromList(@NotNull String number){
        if (!fromNumbers.equals("")){
            fromNumbers += " ";
        }
        fromNumbers += number;
    }

    public List<String> getFromList(){
        if (fromNumbers.equals("")){
            return new ArrayList<>();
        }
        String[] splitted = fromNumbers.split(" ");
        return new ArrayList<>(Arrays.asList(splitted));
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

    public String getToNumbers() {
        return toNumbers;
    }

    public void setToNumbers(String to) {
        this.toNumbers = to;
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
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", days='" + days + '\'' +
                ", user=" + user.getLogin() +
                '}';
    }
}
