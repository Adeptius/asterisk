package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static ua.adeptius.asterisk.utils.MyStringUtils.addZero;

@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class Call {

    @JsonProperty
    private String calledFrom;

    @JsonProperty
    private String calledTo;

    @JsonProperty
    private CallState callState;

    @JsonProperty
    private Direction direction;

    @JsonProperty
    private String asteriskId;

    @JsonProperty
    private String utm;

    @JsonProperty
    private String googleId;

    private User user;

    @JsonProperty
    private String calledDate;

    @JsonProperty
    private long calledMillis;

    @JsonProperty
//    private int secondsToAnswer = -1; // Значение задаётся только 1 раз, если оно изначально -1. Астериск присылает 2 раза сообщение об ответе. Это просто защита.
    private int secondsFullTime;

    @JsonProperty
    private int secondsTalk;

//    @JsonProperty
//    private int secondsFullTime;

//    boolean callIsEnded;
    private int amoDealId;
    private int amoContactId;
    private int lastOperationTime;
    private OuterPhone outerPhone;
    private CallPhase callPhase;


    public enum CallPhase{
        NEW_CALL,
        REDIRECTED,
        ANSWERED,
        ENDED
    }

    public CallPhase getCallPhase() {
        return callPhase;
    }

    public void setCallPhase(CallPhase callPhase) {
        this.callPhase = callPhase;
    }

    //    public int getCalculatedModifiedTime() { // AUTOINCREMENT
//        int currentTime = ((int) ((new Date().getTime() / 1000))) + timeDifference;
//        if (currentTime <= lastOperationTime) { // если текущее время совпадает с предыдущим
//            lastOperationTime++;
//            return lastOperationTime;
//        } else {
//            return currentTime;
//        }
//    }


//    private int timeDifference;


    public int getSecondsFullTime() {
        return secondsFullTime;
    }

    public void setSecondsFullTime(int secondsFullTime) {
        this.secondsFullTime = secondsFullTime;
    }

    public void setLastOperationTime(int lastOperationTime) {
        this.lastOperationTime = lastOperationTime;
//        timeDifference = lastOperationTime - ((int) new Date().getTime() / 1000);
    }

    public int getAmoDealId() {
        return amoDealId;
    }

    public void setAmoDealId(int amoDealId) {
        this.amoDealId = amoDealId;
    }

    public int getSecondsTalk() {
        return secondsTalk;
    }

    public void setSecondsTalk(int secondsTalk) {
        this.secondsTalk = secondsTalk;
    }

    public long getCalledMillis() {
        return calledMillis;
    }

    public String getCalledDate() {
        return calledDate;
//        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(getCalledMillis()));
    }

    public void setCalledDate(Date calledDate) {
        calledMillis = calledDate.getTime();
        this.calledDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calledMillis);
    }

    public void setCalledDate(String calledDate) throws Exception {
        this.calledDate = calledDate;
        calledMillis = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(calledDate).getTime();
    }

    /**
     * Возвращает true callProcessor'у что бы сообщить установилось ли значение впервые впервые
     */
//    public boolean setAnsweredDate(Date answeredDate) { // Задаётся в CallProcessor
//        if (secondsToAnswer == -1) { // защита, что бы данные вводились только 1 раз.
//            secondsToAnswer = (int) ((answeredDate.getTime() - getCalledMillis()) / 1000);
//            return true;
//        } else {
//            return false;
//        }
//    }


//    @JsonProperty
//    public int getSecondsToAnswer() {
//        return secondsToAnswer == -1 ? secondsFullTime : secondsToAnswer;
//    }

//    @JsonProperty
//    public int getSecondsTalk() { // Тут высчитывается время разговора
//        if (callState == CallState.ANSWER) {
//            return getSecondsFullTime() - getSecondsToAnswer();
//        } else {
//            return 0;
//        }
//    }

//    public void setEndedDate(Date endedDate) { // Задаётся в CallProcessor
//        secondsFullTime = (int) ((endedDate.getTime() - getCalledMillis()) / 1000);
//        callIsEnded = true;
//    }

//    public boolean isCallIsEnded() { // Задаётся в CallProcessor
//        return callIsEnded;
//    }

//    public void setSecondsFullTime(int secondsFullTime) { // Задаётся при чтении с БД
//        this.secondsFullTime = secondsFullTime;
//    }

//    public int getSecondsFullTime() {
//        return secondsFullTime;
//    }

    public enum CallState {
        ANSWER, // звонок был принят и обработан сотрудником
        BUSY, // входящий звонок был, но линия была занята;
        FAIL,
        NOANSWER, // входящий вызов состоялся, но в течение времени ожидания ответа не был принят сотрудником
        CHANUNAVAIL // вызываемый номер был недоступен;
    }

    public enum Direction {
        IN, OUT
    }

//    public enum Service {
//        TRACKING, TELEPHONY
//    }

    public String getUtm() {
        return utm;
    }

    public void setUtm(String utm) {
        this.utm = utm;
    }


    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getCalledFrom() {
        return calledFrom;
    }

    public void setCalledFrom(String calledFrom) {
        this.calledFrom = addZero(calledFrom);
    }

    public String getCalledTo() {
        return calledTo;
    }

    public void setCalledTo(String calledto) {
        this.calledTo = addZero(calledto);
    }

    public String getAsteriskId() {
        return asteriskId;
    }

    public void setAsteriskId(String asteriskId) {
        this.asteriskId = asteriskId;
    }

//    public String getFirstCall() {
//        return firstCall;
//    }

//    public void setFirstCall(String firstCall) {
//        this.firstCall = firstCall;
//    }

    public CallState getCallState() {
        if (callState == null) {
            return CallState.FAIL; // null если с сип позвонить на 934027182. Номер не может быть вызван.
        }
        return callState;
    }

//    public boolean isStateWasAlreadySetted(){
//        return stateIsSetted;
//    }

//    private boolean stateIsSetted;

    public void setCallState(CallState callState) {
        this.callState = callState;
//        stateIsSetted = true;
    }

    public OuterPhone getOuterPhone() {
        return outerPhone;
    }

    public void setOuterPhone(OuterPhone outerPhone) {
        this.outerPhone = outerPhone;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

//    public int getAmoContactId() {
//        return amoContactId;
//    }

    public void setAmoContactId(int amoContactId) {
        this.amoContactId = amoContactId;
    }

    @Override
    public String toString() {
        return "Call{" +
                "\ncalledFrom='" + calledFrom + '\'' +
                "\ncalledTo='" + calledTo + '\'' +
                "\ncallState=" + callState +
                "\ndirection=" + direction +
                "\nasteriskId='" + asteriskId + '\'' +
                "\nuser=" + user.getLogin() +
                "\ncalledDate='" + calledDate + '\'' +
                "\ncalledMillis=" + calledMillis +
                "\nsecondsTalk=" + secondsTalk +
                "\nsecondsFullTime=" + secondsFullTime +
                "\nouterPhone=" + outerPhone +
                "\n}";
    }
}
