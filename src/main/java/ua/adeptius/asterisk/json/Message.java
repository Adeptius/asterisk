package ua.adeptius.asterisk.json;


public class Message {

    public Message(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    private Status status;

    private String message;

    public enum Status {
        Success, Error
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
