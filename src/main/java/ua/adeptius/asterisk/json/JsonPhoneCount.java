package ua.adeptius.asterisk.json;




public class JsonPhoneCount {

    private Integer innerCount;
    private Integer outerCount;

    public Integer getInnerCount() {
        return innerCount;
    }

    public void setInnerCount(Integer innerCount) {
        this.innerCount = innerCount;
    }

    public Integer getOuterCount() {
        return outerCount;
    }

    public void setOuterCount(Integer outerCount) {
        this.outerCount = outerCount;
    }

    @Override
    public String toString() {
        return "JsonPhoneCount{" +
                "innerCount=" + innerCount +
                ", outerCount=" + outerCount +
                '}';
    }
}
