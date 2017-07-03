package ua.adeptius.asterisk.json;




public class JsonTelephony {

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
        return "JsonTelephony{" +
                "innerCount=" + innerCount +
                ", outerCount=" + outerCount +
                '}';
    }
}
