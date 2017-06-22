package ua.adeptius.asterisk.monitor;


import org.asteriskjava.manager.event.ManagerEvent;
import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewCall {

    @JsonIgnore
    private ArrayList<ManagerEvent> events = new ArrayList<>(300);

    private String calledFrom;
    private String calledTo;
    private String asteriskId;

    private Date calledDate;
    private Date answeredDate;
    private Date endedDate;

    //TODO нужны методы получения дат в текстовом формате для БД и для сайта

    // Нужно для опеределения кому принадлежит тот номер на которы позвонил посетитель
    // а не на кого он на самом деле попал. Для трекинга
    @JsonIgnore
    private String firstCall;

    private Call.CallState callState;
    private Call.Direction direction;

    @JsonIgnore
    private Call.Service service;

    @JsonIgnore
    private User user;




    public long getCalledMillis() {
        return calledDate.getTime();
    }

    public long getAnsweredMillis() {
        return answeredDate.getTime();
    }

    public long getEndedMillis() {
        return endedDate.getTime();
    }

    public String getDateForDb() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getCalledDate());
    }


    public void addEvent(ManagerEvent event){
        events.add(event);
    }




    public enum CallState{
        ANSWERED, BUSY, FAIL
    }

    public enum Direction{
        IN, OUT
    }

    public enum Service{
        TRACKING, TELEPHONY
    }


    public ArrayList<ManagerEvent> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<ManagerEvent> events) {
        this.events = events;
    }

    public String getCalledFrom() {
        return calledFrom;
    }

    public void setCalledFrom(String calledFrom) {
        this.calledFrom = calledFrom;
    }

    public String getCalledTo() {
        return calledTo;
    }

    public void setCalledTo(String calledto) {
        this.calledTo = calledto;
    }

    public String getAsteriskId() {
        return asteriskId;
    }

    public void setAsteriskId(String asteriskId) {
        this.asteriskId = asteriskId;
    }

    public Date getCalledDate() {
        return calledDate;
    }

    public void setCalledDate(Date calledDate) {
        this.calledDate = calledDate;
    }

    public Date getAnsweredDate() {
        return answeredDate;
    }

    public void setAnsweredDate(Date answeredDate) {
        this.answeredDate = answeredDate;
    }

    public Date getEndedDate() {
        return endedDate;
    }

    public void setEndedDate(Date endedDate) {
        this.endedDate = endedDate;
    }

    public String getFirstCall() {
        return firstCall;
    }

    public void setFirstCall(String firstCall) {
        this.firstCall = firstCall;
    }

    public Call.CallState getCallState() {
        return callState;
    }

    public void setCallState(Call.CallState callState) {
        this.callState = callState;
    }

    public Call.Direction getDirection() {
        return direction;
    }

    public void setDirection(Call.Direction direction) {
        this.direction = direction;
    }

    public Call.Service getService() {
        return service;
    }

    public void setService(Call.Service service) {
        this.service = service;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "NewCall{" +
//                "\n    events=" + events +
                "\n    calledFrom='" + calledFrom + '\'' +
                "\n    calledTo='" + calledTo + '\'' +
                "\n    asteriskId='" + asteriskId + '\'' +
                "\n    calledDate=" + calledDate +
                "\n    answeredDate=" + answeredDate +
                "\n    endedDate=" + endedDate +
                "\n    firstCall='" + firstCall + '\'' +
                "\n    callState=" + callState +
                "\n    direction=" + direction +
                "\n    service=" + service +
                "\n    user=" + user.getLogin() +
                "\n}";
    }
}
