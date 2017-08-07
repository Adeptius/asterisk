package ua.adeptius.asterisk.json;



public class JsonSite {

    private String name;
    private String standartNumber;
    private Integer timeToBlock;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStandardNumber() {
        return standartNumber;
    }

    public void setStandartNumber(String standartNumber) {
        this.standartNumber = standartNumber;
    }

    public Integer getTimeToBlock() {
        return timeToBlock;
    }

    public void setTimeToBlock(Integer timeToBlock) {
        this.timeToBlock = timeToBlock;
    }

    @Override
    public String toString() {
        return "JsonSite{" +
                "name='" + name + '\'' +
                ", standartNumber='" + standartNumber + '\'' +
                ", timeToBlock=" + timeToBlock +
                '}';
    }
}
