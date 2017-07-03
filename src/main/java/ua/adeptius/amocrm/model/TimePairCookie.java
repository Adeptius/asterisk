package ua.adeptius.amocrm.model;



public class TimePairCookie {

    private long timeCreated;
    private String coockie;

    public TimePairCookie(long timeCreated, String coockie) {
        this.timeCreated = timeCreated;
        this.coockie = coockie;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getCoockie() {
        return coockie;
    }

    public void setCoockie(String coockie) {
        this.coockie = coockie;
    }
}
