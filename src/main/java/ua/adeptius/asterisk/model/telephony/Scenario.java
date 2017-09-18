package ua.adeptius.asterisk.model.telephony;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import ua.adeptius.asterisk.model.User;

import javax.persistence.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Entity
@Table(name = "scenarios", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class Scenario {

    @JsonProperty
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

    @ManyToOne
    @JoinColumn(name = "login", insertable = false, updatable = false)
    private User user;

    @JsonProperty
    public List<Rule> getRules(){
        return user.getAllRules().stream()
                .filter(rule -> name.equals(rule.getScenario()))
                .collect(Collectors.toList());
    }

    public void addRule(Rule rule){
        rule.setUser(user);
        rule.setScenario(name);
        user.saveInUsersRules(rule);
    }

    @SuppressWarnings("Duplicates")
    public Rule getRuleForNow(){
        Calendar calendar = new GregorianCalendar();
        long currentMillis = calendar.getTimeInMillis();
        long newTimeMillis = currentMillis; // Просчитываем следующий час
        calendar.setTimeInMillis(newTimeMillis);

        int dayByCalendar = calendar.get(Calendar.DAY_OF_WEEK);

        int day = 0; // День. Понедельник = 0
        if (dayByCalendar == Calendar.TUESDAY) {
            day = 1;
        } else if (dayByCalendar == Calendar.WEDNESDAY) {
            day = 2;
        } else if (dayByCalendar == Calendar.THURSDAY) {
            day = 3;
        } else if (dayByCalendar == Calendar.FRIDAY) {
            day = 4;
        } else if (dayByCalendar == Calendar.SATURDAY) {
            day = 5;
        } else if (dayByCalendar == Calendar.SUNDAY) {
            day = 6;
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Час дня. час ночи = 1
        return getRuleByTime(day, hour);
    }

    public Rule getRuleByTime(int day, int hour){
        List<Rule> rules = getRules();
        Rule ruleToReturn = null;
        Rule defaultRule = null;
        for (Rule rule : rules) {
            if (rule.getType() == RuleType.DEFAULT){
                defaultRule = rule;
                continue;
            }
            if (rule.getDays()[day] && rule.getStartHour() <= hour && rule.getEndHour() > hour){
                ruleToReturn = rule;
                break;
            }
        }

        if (ruleToReturn != null){
            return ruleToReturn;
        }else {
            return defaultRule;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null){
            login = user.getLogin();
        }
        this.user = user;
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
