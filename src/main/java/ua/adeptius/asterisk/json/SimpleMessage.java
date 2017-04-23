package ua.adeptius.asterisk.json;


public class SimpleMessage {

    public SimpleMessage(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private String key;
    private String value;


    @Override
    public String toString() {
        return "{\""+key+"\":\""+value+"\"}";
    }
}
