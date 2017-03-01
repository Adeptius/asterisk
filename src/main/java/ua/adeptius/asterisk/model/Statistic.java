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
    @JsonIgnore
    private int speakTimeInSeconds;
    @JsonIgnore
    private int speakTime;
    @JsonIgnore
    private int dateForDb;
    @JsonIgnore
    private int timeToAnswerInSeconds;

    private String direction;
    private String from;
    private String to;
    private String date;
    private int talkingTime;
    String googleId;
    String reques;
    private String callUniqueId;


    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setTimeToAnswer(int timeToAnswer) {
        this.timeToAnswerInSeconds = timeToAnswer;
        this.timeToAnswer = timeToAnswer;
    }

    public int getTimeToAnswerForWeb() {
        return timeToAnswer;
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
        if (reques==null){
            return "";
        }
        return reques;
    }

    public void setRequest(String reques) {
        this.reques = filterUtmMarks(reques);
    }
 public void setRequestWithOutfiltering(String reques) {
        this.reques = reques;
    }

    public String getTimeToAnswer(){
        if (answered == 0){
            return "0";
        }
        long time = answered - called;
        return StringUtils.getStringedTime(time);
    }

    public String getSpeakTime(){
        if (answered == 0){
            return "Сбой звонка";
        }
        long time = ended - answered;
        return StringUtils.getStringedTime(time);
    }



    public int getTimeToAnswerInSeconds(){
        if (answered == 0){
            return 0;
        }
        long time = answered - called;
        return (int) (time/1000);
    }

    public int getSpeakTimeInSeconds(){
        long time = 0;
        if (answered !=0){
            time = ended - answered;
        }else {
            time = ended - called;
        }
        return (int) (time / 1000);
    }

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

    public long getCalled() {
        return called;
    }

    public void setCalled(long called) {
        this.called = called;
    }

    public long getAnswered() {
        return answered;
    }

    public void setAnswered(long answered) {
        this.answered = answered;
    }

    public long getEnded() {
        return ended;
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


    private static String filterUtmMarks(String s){
        if (s==null || "".equals(s)){ // если параметров нет вообще
            return s;
        }
        if (!s.contains("=")){ // если почему-то не пусто но и параметра нет
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
                            String currentValue = splitted[i].substring(splitted[i].indexOf("=")+1);
                            if (!currentValue.equals("")){ // если значение не пустое.
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
        }else {// если параметр 1
            for (int i = 0; i < keys.length; i++) {
                String key = s.substring(0, s.indexOf("="));
                if (keys[i].equals(key)){
                    return s;
                }
            }
            return "";
        }
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "called=" + called +
                ", answered=" + answered +
                ", ended=" + ended +
                ", site=" + site +
                ", speakTimeInSeconds=" + speakTimeInSeconds +
                ", speakTime=" + speakTime +
                ", dateForDb=" + dateForDb +
                ", direction='" + direction + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", date='" + date + '\'' +
                ", timeToAnswerInSeconds=" + timeToAnswerInSeconds +
                ", talkingTime=" + talkingTime +
                ", googleId='" + googleId + '\'' +
                ", reques='" + reques + '\'' +
                ", callUniqueId='" + callUniqueId + '\'' +
                '}';
    }
}
