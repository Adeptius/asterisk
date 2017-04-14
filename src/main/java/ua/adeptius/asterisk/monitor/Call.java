package ua.adeptius.asterisk.monitor;


import ua.adeptius.asterisk.model.Customer;

import java.util.Date;

public class Call {

    private String id;
    private String from;
    private String to;
    private Customer customer;
    private Date called;
    private Date answered;
    private Date ended;
    private CallState callState;
    private Direction direction;

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public CallState getCallState() {
        return callState;
    }

    public void setCallState(CallState callState) {
        this.callState = callState;
    }

    public Date getCalled() {
        return called;
    }

    public void setCalled(Date called) {
        this.called = called;
    }

    public Date getAnswered() {
        return answered;
    }

    public void setAnswered(Date answered) {
        this.answered = answered;
    }

    public Date getEnded() {
        return ended;
    }

    public void setEnded(Date ended) {
        this.ended = ended;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = addZero(from);
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = addZero(to);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static String addZero(String source) {
        try {
            if (source.length() == 9 && !source.startsWith("0")) {
                source = "0" + source;
            }
        } catch (Exception e) {
//            System.out.println("Ошибка добавления нолика. Пришло " + source);
        }
        return source;
    }

    public enum CallState{
        ANSWERED, BUSY
    }

    public enum Direction{
        IN, OUT
    }



    @Override
    public String toString() {
        return "Call{" +
                "id='" + id + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", customer=" + customer.getName() +
                ", called=" + called +
                ", answered=" + answered +
                ", ended=" + ended +
                '}';
    }
}
