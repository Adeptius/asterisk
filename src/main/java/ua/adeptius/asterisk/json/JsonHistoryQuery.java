package ua.adeptius.asterisk.json;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import ua.adeptius.asterisk.exceptions.JsonHistoryQueryValidationException;

import java.util.Arrays;
import java.util.regex.Pattern;

public class JsonHistoryQuery {

    public JsonHistoryQuery() {
    }

    private String dateFrom;
    private String dateTo;
    private int limit;
    private int offset;
    private Filter filter;

    private String sqlFilter;

    public String buildSqlQueryCount(String tableName) throws JsonHistoryQueryValidationException {
        if (sqlFilter == null) {
            buildSqlQueryFilters();
        }
        return "SELECT COUNT(*) FROM " + tableName + sqlFilter;
    }

    public String buildSqlQueryResult(String tableName) throws JsonHistoryQueryValidationException {
        if (sqlFilter == null) {
            buildSqlQueryFilters();
        }
        return "SELECT * FROM " + tableName + sqlFilter + "\nORDER BY called_date DESC\nLIMIT " + limit + " OFFSET " + offset;
    }

    private void buildSqlQueryFilters() throws JsonHistoryQueryValidationException {
        if (dateFrom == null || !Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(dateFrom).find()) {
            throw new JsonHistoryQueryValidationException("Wrong FROM date");
        }

        if (dateTo == null || !Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(dateTo).find()) {
            throw new JsonHistoryQueryValidationException("Wrong TO date");
        }

        if (1 > limit || limit > 300) {
            throw new JsonHistoryQueryValidationException("Limit range is 1-300");
        }

        if (offset < 0) {
            throw new JsonHistoryQueryValidationException("Offset is less than 0");
        }

        if (filter == null) {
            filter = new Filter();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\nWHERE called_date BETWEEN STR_TO_DATE('").append(dateFrom).append("', '%Y-%m-%d %H:%i:%s') AND STR_TO_DATE('").append(dateTo).append("', '%Y-%m-%d %H:%i:%s') ");

        String direction = filter.getDirection();
        if (direction != null) {
            sb.append("\nAND direction = '").append(direction).append("'");
        }

        String[] calledFrom = filter.getCalledFrom();
        if (!ArrayUtils.isEmpty(calledFrom)) {
            sb.append("\nAND (");
            for (int i = 0; i < calledFrom.length; i++) {
                if (i != 0) {
                    sb.append(" OR ");
                }
                sb.append("called_from = '").append(calledFrom[i]).append("'");
            }
            sb.append(")");
        }


        String[] outerNum = filter.getOuterNum();
        if (!ArrayUtils.isEmpty(outerNum)) {
            sb.append("\nAND (");
            for (int i = 0; i < outerNum.length; i++) {
                if (i != 0) {
                    sb.append(" OR ");
                }
                sb.append("outer_number = '").append(outerNum[i]).append("'");
            }
            sb.append(")");
        }


        String[] calledTo = filter.getCalledTo();
        if (!ArrayUtils.isEmpty(calledTo)) {
            sb.append("\nAND (");
            for (int i = 0; i < calledTo.length; i++) {
                if (i != 0) {
                    sb.append(" OR ");
                }
                sb.append("called_to = '").append(calledTo[i]).append("'");
            }
            sb.append(")");
        }

        Boolean newLead = filter.getNewLead();
        if (newLead != null) {
            sb.append("\nAND new_lead = ").append(newLead ? 1 : 0).append("");
        }

        Boolean answered = filter.getAnswered();
        if (answered != null) {
            sb.append("\nAND call_state ").append(answered ? "" : "!").append("= 'ANSWER'");
        }

        String utmSource = filter.getUtmSource();
        if (!StringUtils.isBlank(utmSource)) {
            sb.append("\nAND utm_source = '").append(utmSource).append("'");
        }

        String utmMedium = filter.getUtmMedium();
        if (!StringUtils.isBlank(utmMedium)) {
            sb.append("\nAND utm_medium = '").append(utmMedium).append("'");
        }

        String utmCampaing = filter.getUtmCampaing();
        if (!StringUtils.isBlank(utmCampaing)) {
            sb.append("\nAND utm_campaign = '").append(utmCampaing).append("'");
        }

        String utmTerm = filter.getUtmTerm();
        if (!StringUtils.isBlank(utmTerm)) {
            sb.append("\nAND utm_term = '").append(utmTerm).append("'");
        }

        String utmContent = filter.getUtmContent();
        if (!StringUtils.isBlank(utmContent)) {
            sb.append("\nAND utm_content = '").append(utmContent).append("'");
        }

        sqlFilter = sb.toString();
    }


    public class Filter {
        private String direction;
        private String[] calledFrom;
        private String[] outerNum;
        private String[] calledTo;
        private Boolean newLead;
        private Boolean answered;
        private String utmSource;
        private String utmMedium;
        private String utmCampaing;
        private String utmTerm;
        private String utmContent;

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public Boolean getAnswered() {
            return answered;
        }

        public void setAnswered(Boolean answered) {
            this.answered = answered;
        }

        public Boolean getNewLead() {
            return newLead;
        }

        public void setNewLead(Boolean newLead) {
            this.newLead = newLead;
        }

        public String[] getCalledFrom() {
            return calledFrom;
        }

        public void setCalledFrom(String[] calledFrom) {
            this.calledFrom = calledFrom;
        }

        public String[] getOuterNum() {
            return outerNum;
        }

        public void setOuterNum(String[] outerNum) {
            this.outerNum = outerNum;
        }

        public String[] getCalledTo() {
            return calledTo;
        }

        public void setCalledTo(String[] calledTo) {
            this.calledTo = calledTo;
        }

        public String getUtmSource() {
            return utmSource;
        }

        public void setUtmSource(String utmSource) {
            this.utmSource = utmSource;
        }

        public String getUtmMedium() {
            return utmMedium;
        }

        public void setUtmMedium(String utmMedium) {
            this.utmMedium = utmMedium;
        }

        public String getUtmCampaing() {
            return utmCampaing;
        }

        public void setUtmCampaing(String utmCampaing) {
            this.utmCampaing = utmCampaing;
        }

        public String getUtmTerm() {
            return utmTerm;
        }

        public void setUtmTerm(String utmTerm) {
            this.utmTerm = utmTerm;
        }

        public String getUtmContent() {
            return utmContent;
        }

        public void setUtmContent(String utmContent) {
            this.utmContent = utmContent;
        }

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("Filter{");
            if (direction != null) {
                sb.append("direction='").append(direction).append('\'');
            }
            if (calledFrom != null) {
                sb.append(", calledFrom=").append(Arrays.toString(calledFrom));
            }
            if (outerNum != null) {
                sb.append(", outerNum=").append(Arrays.toString(outerNum));
            }
            if (calledTo != null) {
                sb.append(", calledTo=").append(Arrays.toString(calledTo));
            }
            if (newLead != null) {
                sb.append(", newLead=").append(newLead);
            }
            if (answered != null) {
                sb.append(", answered=").append(answered);
            }
            if (utmSource != null) {
                sb.append(", utmSource='").append(utmSource).append('\'');
            }
            if (utmMedium != null) {
                sb.append(", utmMedium='").append(utmMedium).append('\'');
            }
            if (utmCampaing != null) {
                sb.append(", utmCampaing='").append(utmCampaing).append('\'');
            }
            if (utmTerm != null) {
                sb.append(", utmTerm='").append(utmTerm).append('\'');
            }
            if (utmContent != null) {
                sb.append(", utmContent='").append(utmContent).append('\'');
            }
            sb.append('}');
            return sb.toString();
        }
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

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "JsonHistoryQuery{" +
                "dateFrom='" + dateFrom + '\'' +
                ", dateTo='" + dateTo + '\'' +
                ", limit=" + limit +
                ", offset=" + offset +
                ", filter=" + filter +
                '}';
    }
}
