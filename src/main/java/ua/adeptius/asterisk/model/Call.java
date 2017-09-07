package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static ua.adeptius.asterisk.utils.MyStringUtils.addZero;

@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class Call {

    @JsonProperty
    private String calledFrom;

    @JsonProperty
    private List<String> calledTo;

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

    private int amoDealId;
    private int amoContactId;
    private int lastOperationTime;
    private OuterPhone outerPhone;
    private CallPhase callPhase;
    private boolean sendedAnswerWsMessage; // чисто для AmoWSMessageSender что бы он знал отправлял или нет

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

    public boolean isSendedAnswerWsMessage() {
        return sendedAnswerWsMessage;
    }

    public void setSendedAnswerWsMessage(boolean sendedAnswerWsMessage) {
        this.sendedAnswerWsMessage = sendedAnswerWsMessage;
    }

//    //    private int timeDifference;
//    public int getCalculatedModifiedTime() { // нужно для амо который требует last_modified (добавление комента или тегов). Метод вычисляет для него значение
//        int currentTime = ((int) ((new Date().getTime() / 1000))) + timeDifference; // нужен в основном, если операции изменения очень частые
//        if (currentTime <= lastOperationTime) { // если текущее время совпадает с предыдущим
//            lastOperationTime++;
//            return lastOperationTime;
//        } else {
//            return currentTime;
//        }
//    }


    // временно для фронтенда пока не поймём как передавать calledTo во фронтенд
    @JsonProperty
    @Deprecated
    public String getCalledToOnePhone(){
        return calledTo.get(0);
    }

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

    public List<String> getCalledTo() {
        return calledTo;
    }

    public void setCalledTo(List<String> calledTo) {
        this.calledTo = calledTo;
    }


    public String getAsteriskId() {
        return asteriskId;
    }

    public void setAsteriskId(String asteriskId) {
        this.asteriskId = asteriskId;
    }

    public CallState getCallState() {
        if (callState == null) {
            return CallState.FAIL; // null если с сип позвонить на 934027182. Номер не может быть вызван.
        }
        return callState;
    }

    public void setCallState(CallState callState) {
        this.callState = callState;
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

    public int getAmoContactId() {
        return amoContactId;
    }

    public void setAmoContactId(int amoContactId) {
        this.amoContactId = amoContactId;
    }

    @Override
    public String toString() {
        return "Call{" +
                ", calledFrom='" + calledFrom + '\'' +
                ", calledTo='" + calledTo + '\'' +
                ", callState=" + callState +
                ", direction=" + direction +
                ", asteriskId='" + asteriskId + '\'' +
                ", user=" + user.getLogin() +
                ", calledDate='" + calledDate + '\'' +
                ", calledMillis=" + calledMillis +
                ", secondsTalk=" + secondsTalk +
                ", secondsFullTime=" + secondsFullTime +
                ", outerPhone=" + outerPhone +
                "}";
    }
}
