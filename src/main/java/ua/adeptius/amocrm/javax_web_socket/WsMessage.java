package ua.adeptius.amocrm.javax_web_socket;

public class WsMessage {


    private MessageEventType eventType;
    private String to;
    private String from;
    private String content;
    private String callTo;
    private String dealId;
    private String callId;
    private MessageCallPhase callPhase;

    private transient String userId;

    public WsMessage() {
    }


    public MessageCallPhase getCallPhase() {
        return callPhase;
    }

    public void setCallPhase(MessageCallPhase callPhase) {
        this.callPhase = callPhase;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public WsMessage(MessageEventType eventType) {
        this.eventType = eventType;
    }

    public MessageEventType getEventType() {
        return eventType;
    }

    public void setEventType(MessageEventType eventType) {
        this.eventType = eventType;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCallTo() {
        return callTo;
    }

    public void setCallTo(String callTo) {
        this.callTo = callTo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "WsMessage{" +
                "eventType=" + eventType +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", content='" + content + '\'' +
                ", callTo='" + callTo + '\'' +
                ", dealId='" + dealId + '\'' +
                ", callId='" + callId + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
