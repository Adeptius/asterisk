package ua.adeptius.asterisk.json;


public class JsonHistoryQuery {

    private String dateFrom;
    private String dateTo;
    private String direction;
    private int limit;
    private int offset;


    public JsonHistoryQuery(String dateFrom, String dateTo, String direction, int limit, int offset) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.direction = direction;
        this.limit = limit;
        this.offset = offset;
    }

    public JsonHistoryQuery() {
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "JsonHistoryQuery{" +
                "dateFrom='" + dateFrom + '\'' +
                ", dateTo='" + dateTo + '\'' +
                ", direction='" + direction + '\'' +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
