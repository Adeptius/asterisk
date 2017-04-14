package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static ua.adeptius.asterisk.utils.StringUtils.doTwoSymb;

public class Statistic {

    @JsonIgnore
    private long called;
    @JsonIgnore
    private long answered;
    @JsonIgnore
    private long ended;
    @JsonIgnore
    private int timeToAnswer;
    @JsonIgnore
    private Site site;


    private String direction;
    private String from;
    private String to;
    private String date;
    private int talkingTime;
    String googleId;
    String reques;
    private String callUniqueId;

    public String getTimeToAnswerForWebInSeconds() {
        if (timeToAnswer == 0) {
            return "Недозвон";
        }
        return "" + (timeToAnswer - 2); // для отображения в веб. Корректирую погрешность на +2 секунды
    }

    // Используется в JSON
    public String getTimeToAnswerForWebPretty() {
        if (timeToAnswer == 0) {
            return "Недозвон";
        }
        return StringUtils.getStringedTime((timeToAnswer - 2) * 1000); // для отображения в веб. Корректирую погрешность на +2 секунды
    }

    // Используется в JSON
    public String getTalkingTimePretty() {
        return StringUtils.getStringedTime(talkingTime*1000);
    }


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setTimeToAnswer(int timeToAnswer) {
        this.timeToAnswer = timeToAnswer;
    }


    public int getTalkingTime() {
        return talkingTime;
    }

    public void setTalkingTime(int talkingTime) {
        this.talkingTime = talkingTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getRequest() {
        if (reques == null) {
            return "";
        }
        return reques;
    }

    public void setRequest(String reques) {
        this.reques = filterUtmMarks(reques);
    }

    public void setRequestWithoutFiltering(String reques) {
        this.reques = reques;
    }

    public String getTimeToAnswer() {
        if (answered == 0) {
            return "0";
        }
        long time = answered - called;
        return StringUtils.getStringedTime(time);
    }


    @JsonIgnore
    public int getTimeToAnswerInSeconds() {
        if (answered == 0) {
            return 0;
        }
        long time = answered - called;
        return (int) (time / 1000);
    }

    @JsonIgnore
    public int getSpeakTimeInSeconds() {
        long time = 0;
        if (answered != 0) {
            time = ended - answered;
        } else {
            time = ended - called;
        }
        return (int) (time / 1000);
    }

    @JsonIgnore
    public String getDateForDb() {
        long time = called;
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format1.format(calendar.getTime());
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }


    public void setCalled(long called) {
        this.called = called;
    }


    public void setAnswered(long answered) {
        this.answered = answered;
    }


    public void setEnded(long ended) {
        this.ended = ended;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getCallUniqueId() {
        return callUniqueId;
    }

    public void setCallUniqueId(String callUniqueId) {
        this.callUniqueId = callUniqueId;
    }


    private static String filterUtmMarks(String s) {
        if (s == null || "".equals(s)) { // если параметров нет вообще
            return s;
        }
        if (!s.contains("=")) { // если почему-то не пусто но и параметра нет
            return "";
        }
        String[] keys = new String[]{"utm_source", "utm_medium", "utm_campaign", "utm_content", "utm_term", "pm_source", "pm_block", "pm_position"};
        String result = "";
        if (s.contains("&")) { // если параметров несколько
            String[] splitted = s.split("&");
            for (int i = 0; i < splitted.length; i++) {
                if (splitted[i].contains("=")) { // защита, если попадёт хрен знает что вместо ключ=значение
                    String currentKey = splitted[i].substring(0, splitted[i].indexOf("="));
                    for (int j = 0; j < keys.length; j++) {
                        if (currentKey.equals(keys[j])) {
                            String currentValue = splitted[i].substring(splitted[i].indexOf("=") + 1);
                            if (!currentValue.equals("")) { // если значение не пустое.
                                if (!result.equals("")) { // вначале & добавлять не нужно
                                    result += "&";
                                }
                                result += splitted[i];
                            }
                        }
                    }
                }
            }
            return result;
        } else {// если параметр 1
            for (int i = 0; i < keys.length; i++) {
                String key = s.substring(0, s.indexOf("="));
                if (keys[i].equals(key)) {
                    return s;
                }
            }
            return "";
        }
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "\ncalled=" + called +
                "\nanswered=" + answered +
                "\nended=" + ended +
                "\ntimeToAnswer=" + timeToAnswer +
                "\nsite=" + site +
                "\ndirection='" + direction + '\'' +
                "\nfrom='" + from + '\'' +
                "\nto='" + to + '\'' +
                "\ndate='" + date + '\'' +
                "\ntalkingTime=" + talkingTime +
                "\ngoogleId='" + googleId + '\'' +
                "\nreques='" + reques + '\'' +
                "\ncallUniqueId='" + callUniqueId + '\'' +
                "\n}";
    }
}
