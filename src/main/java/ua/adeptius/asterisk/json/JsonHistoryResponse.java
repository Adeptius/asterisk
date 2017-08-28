package ua.adeptius.asterisk.json;


import ua.adeptius.asterisk.model.Call;

import java.util.List;

public class JsonHistoryResponse {

    private int limit;
    private int offset;
    private int count;
    private List<Call> calls;

    public JsonHistoryResponse(int limit, int offset, List<Call> calls, int count) {
        this.limit = limit;
        this.offset = offset;
        this.calls = calls;
        this.count = count;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public List<Call> getCalls() {
        return calls;
    }

    public void setCalls(List<Call> calls) {
        this.calls = calls;
    }

    @Override
    public String toString() {
        return "JsonHistoryResponse{" +
                "limit=" + limit +
                ", offset=" + offset +
                ", calls=" + calls +
                '}';
    }
}
