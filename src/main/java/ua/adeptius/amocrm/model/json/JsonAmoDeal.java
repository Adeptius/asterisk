package ua.adeptius.amocrm.model.json;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class JsonAmoDeal {

//    private HashMap<String, String> leadStatuses = new HashMap<>();
//    private String phoneEnumId = null;
//    String phoneId = null;

    private String id;
    private String statusId;
    private int dateCreate;
    int serverResponseTime;
    private Set<String> tags = new HashSet<>();

    public JsonAmoDeal(String json, int serverTimeWhenResponse) {
//        Server Time нужен только для синхронизации времени с серваком.
//        И вставлен он сюда для того что бы не усложнять алгоритмы сложными обьектами
//        Например при поиске активных сделок в AmoDao.
        this(json);
        this.serverResponseTime = serverTimeWhenResponse;
    }

    public JsonAmoDeal(String json) {
        JSONObject jDeal = new JSONObject(json);

        id = jDeal.getString("id");
        statusId = jDeal.getString("status_id");
        dateCreate = jDeal.getInt("date_create");

        JSONArray jTags = jDeal.getJSONArray("tags");
        for (int i = 0; i < jTags.length(); i++) {
            JSONObject jtag = jTags.getJSONObject(i);
            tags.add(jtag.getString("name"));
        }
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

    public int getServerResponseTime() {
        return serverResponseTime;
    }

    public void setServerResponseTime(int serverResponseTime) {
        this.serverResponseTime = serverResponseTime;
    }


    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
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
