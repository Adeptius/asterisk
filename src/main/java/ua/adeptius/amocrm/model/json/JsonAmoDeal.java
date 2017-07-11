package ua.adeptius.amocrm.model.json;


import org.json.JSONObject;

import java.util.Date;

public class JsonAmoDeal {

//    private HashMap<String, String> leadStatuses = new HashMap<>();
//    private String phoneEnumId = null;
//    String phoneId = null;

    private String id;
    private String statusId;
    private int dateCreate;

    public JsonAmoDeal(String json) {
        JSONObject jDeal = new JSONObject(json);

        id = jDeal.getString("id");
        statusId = jDeal.getString("status_id");
        dateCreate = jDeal.getInt("date_create");
    }

    public boolean isOpen(){
        return !"142".equals(statusId) && !"143".equals(statusId);
    }

    public String getId() {
        return id;
    }

    public int getIdInt() {
        return Integer.parseInt(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public int getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(int dateCreate) {
        this.dateCreate = dateCreate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"id\":\"").append(id).append("\",");
        sb.append("\"last_modified\":\"").append(new Date().getTime() / 1000).append("\"");

        sb.append("}");
        return sb.toString();
    }
}
