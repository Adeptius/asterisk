package ua.adeptius.asterisk.model;


import ua.adeptius.asterisk.utils.StringUtils;

public class PhoneStatistic {

    private long called;
    private long answered;
    private long ended;

    private String to;
    private String from;

    private Site site;



    public String getTimeToAnswer(){
        long time = answered - called;
        return StringUtils.getStringedTime(time);
    }

    public String getSpeakTime(){
        long time = ended - answered;
        return StringUtils.getStringedTime(time);
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
