package ua.adeptius.asterisk.json;


public class JsonHistoryQuery {

    private String dateFrom;
    private String dateTo;
    private String direction;

    public JsonHistoryQuery(String dateFrom, String dateTo, String direction) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.direction = direction;
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
}
