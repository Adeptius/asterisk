package ua.adeptius.asterisk.model;


public class IdPairTime {


    public IdPairTime(int id, int time) {
        this.id = id;
        this.time = time;
    }

    private int id;
    private int time;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
