package ua.adeptius.asterisk.json;



public class JsonTracking {

    private Integer siteNumbersCount;
    private String standartNumber;
    private Integer timeToBlock;

    public Integer getSiteNumbersCount() {
        return siteNumbersCount;
    }

    public void setSiteNumbersCount(Integer siteNumbersCount) {
        this.siteNumbersCount = siteNumbersCount;
    }

    public String getStandartNumber() {
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
}
