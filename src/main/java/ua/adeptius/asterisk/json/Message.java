package ua.adeptius.asterisk.json;


public class Message {

    public Message(Status status, String message) {
        this.status = status;
        this.message = message;
    }

//    public Message(Status status, Object data) {
//        this.data = data;
//        this.status = status;
//    }

    private Status status;

    private String message;

//    private Object data;

    public enum Status {
        Success, Error
    }


//    public Object getData() {
//        return data;
//    }

//    public void setData(Object data) {
//        this.data = data;
//    }

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
