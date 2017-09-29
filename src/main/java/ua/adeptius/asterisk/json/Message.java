package ua.adeptius.asterisk.json;


import org.json.JSONException;
import org.json.JSONObject;

public class Message {

    public Message(String json) throws JSONException {
        try {
            JSONObject jsonObject = new JSONObject(json);
            status = Status.valueOf(jsonObject.getString("status"));
            message = jsonObject.getString("message");
        }catch (JSONException e){
            throw new JSONException("Неверный формат message. JSON: " + json);
        }
    }

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
