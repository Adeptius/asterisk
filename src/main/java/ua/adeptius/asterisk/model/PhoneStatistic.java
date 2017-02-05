package ua.adeptius.asterisk.model;


import ua.adeptius.asterisk.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static ua.adeptius.asterisk.utils.StringUtils.doTwoSymb;

public class PhoneStatistic {

    private long called;
    private long answered;
    private long ended;

    private String to;
    private String from;
    String googleId;
    String reques;



    private Site site;


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
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
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


}
