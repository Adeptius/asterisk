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

    private String from;
    private String to;
    private String date;
    private int timeToAnswerInSeconds;
    private int talkingTime;
    String googleId;
    String reques;
    private String callUniqueId;


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
        return reques;
    }

    public void setRequest(String reques) {
        this.reques = reques;
    }

    public String getTimeToAnswer(){
        long time = answered - called;
        return StringUtils.getStringedTime(time);
    }

    public String getSpeakTime(){
        long time = ended - answered;
        return StringUtils.getStringedTime(time);
    }


    public int getTimeToAnswerInSeconds(){
        long time = answered - called;
        return (int) (time/1000);
    }

    public int getSpeakTimeInSeconds(){
        long time = ended - answered;
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
}
