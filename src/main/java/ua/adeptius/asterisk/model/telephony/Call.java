package ua.adeptius.asterisk.model.telephony;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import ua.adeptius.asterisk.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static ua.adeptius.asterisk.utils.MyStringUtils.addZero;

@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class Call {

    @JsonProperty
    private int id;
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
    private String googleId;
    @JsonProperty
    private String calledDate;
    @JsonProperty
    private long calledMillis;
    @JsonProperty
    private int secondsFullTime;
    @JsonProperty
    private int secondsTalk;
    @JsonProperty
    private String utmSource;
    @JsonProperty
    private String utmMedium;
    @JsonProperty
    private String utmCampaign;
    @JsonProperty
    private String utmTerm;
    @JsonProperty
    private String utmContent;
    @JsonProperty
    private String outerNumber;
    @JsonProperty
    private String comment;
    @JsonProperty
    private boolean newLead;

    private User user;
    private int amoDealId;
    private int amoContactId;
    private String amoContactResponsibleId;

    private int lastOperationTime;
    private OuterPhone outerPhone;
    private CallPhase callPhase;

    private Rule rule;

    public enum CallPhase{
        NEW_CALL,
        REDIRECTED,
        ANSWERED,
        ENDED
    }

    public Rule getRule() {
        return rule;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public CallPhase getCallPhase() {
        return callPhase;
    }

    public void setCallPhase(CallPhase callPhase) {
        this.callPhase = callPhase;
    }

    public int getCalculatedModifiedTime() { // нужно для амо который требует last_modified (добавление комента или тегов). Метод вычисляет для него значение
        int currentTime = (int) (System.currentTimeMillis() /1000); // нужен в основном, если операции изменения очень частые
        if (currentTime <= lastOperationTime) { // если текущее время совпадает с предыдущим
            lastOperationTime++;
            return lastOperationTime;
        } else {
            return currentTime;
        }
    }

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

    public String getAmoContactResponsibleId() {
        return amoContactResponsibleId;
    }

    public void setAmoContactResponsibleId(String amoContactResponsibleId) {
        this.amoContactResponsibleId = amoContactResponsibleId;
    }

    public void setUtm(String urlQuery) {
        if (StringUtils.isBlank(urlQuery)) {
            return;
        }

        String[] parameters;
        if (!urlQuery.contains("&")) { // если параметр один
            parameters = new String[]{urlQuery};
        }else { // если параметров много
            parameters = urlQuery.split("&");
        }

        for (int i = 0; i < parameters.length; i++) { // фильтруем параметры
            String parameter = parameters[i];
            if (!parameter.contains("=")){// если не содержит равно
                continue;
            }

            String[] splittedParam = parameter.split("=");
            if (splittedParam.length <2){ // если ключ или значение отсутствует
                continue;
            }

            String key = splittedParam[0];
            String value = splittedParam[1];

            if (key.startsWith("utm_source")){
                setUtmSource(value);
            }else if (key.startsWith("utm_medium")){
                setUtmMedium(value);
            }else if (key.startsWith("utm_campaign")){
                setUtmCampaign(value);
            }else if (key.startsWith("utm_content")){
                setUtmContent(value);
            }else if (key.startsWith("utm_term")){
                setUtmTerm(value);
            }
        }
    }

    public String getUtmSource() {
        return utmSource;
    }

    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }

    public String getUtmMedium() {
        return utmMedium;
    }

    public void setUtmMedium(String utmMedium) {
        this.utmMedium = utmMedium;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }

    public String getUtmTerm() {
        return utmTerm;
    }

    public void setUtmTerm(String utmTerm) {
        this.utmTerm = utmTerm;
    }

    public String getUtmContent() {
        return utmContent;
    }

    public void setUtmContent(String utmContent) {
        this.utmContent = utmContent;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOuterNumber() {
        return outerNumber;
    }

    public void setOuterNumber(String outerNumber) {
        this.outerNumber = outerNumber;
    }

    public boolean isNewLead() {
        return newLead;
    }

    public void setNewLead(boolean newLead) {
        this.newLead = newLead;
    }

    @Override
    public String toString() {
        return "Call{" +
//                "calledFrom='" + calledFrom + '\'' +
//                ", calledTo=" + calledTo +
//                ", callState=" + callState +
//                ", direction=" + direction +
//                ", asteriskId='" + asteriskId + '\'' +
//                ", utm='" + utm + '\'' +
//                ", googleId='" + googleId + '\'' +
//                ", user=" + user.getLogin() +
//                ", calledDate='" + calledDate + '\'' +
//                ", calledMillis=" + calledMillis +
//                ", secondsFullTime=" + secondsFullTime +
//                ", secondsTalk=" + secondsTalk +
                ", utmSource='" + utmSource + '\'' +
                ", utmMedium='" + utmMedium + '\'' +
                ", utmCampaign='" + utmCampaign + '\'' +
                ", utmTerm='" + utmTerm + '\'' +
                ", utmContent='" + utmContent + '\'' +
//                ", outer_number='" + outer_number + '\'' +
//                ", comment='" + comment + '\'' +
//                ", new_lead=" + new_lead +
//                ", amoDealId=" + amoDealId +
//                ", amoContactId=" + amoContactId +
//                ", amoContactResponsibleId='" + amoContactResponsibleId + '\'' +
//                ", lastOperationTime=" + lastOperationTime +
//                ", outerPhone=" + outerPhone +
//                ", callPhase=" + callPhase +
//                ", sendedAnswerWsMessage=" + sendedAnswerWsMessage +
//                ", rule=" + rule +
                '}';
    }
}
