package ua.adeptius.amocrm.model.json;


import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;

public class JsonPipeline {

    private int id;
    private String name;
    private HashMap<Integer, String> statusesIdAndName = new HashMap<>();

    public JsonPipeline(JSONObject jPipeline) {
        id = jPipeline.getInt("id");
        name = jPipeline.getString("name");

        JSONObject jStatuses = jPipeline.getJSONObject("statuses");
        Iterator<String> keys = jStatuses.keys();
        while (keys.hasNext()){
            String next = keys.next();
            JSONObject jStatus = jStatuses.getJSONObject(next);
            int statusId = jStatus.getInt("id");
            String statusName = jStatus.getString("name");
            statusesIdAndName.put(statusId, statusName);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public HashMap<Integer, String> getStatusesIdAndName() {
        return statusesIdAndName;
    }

    @Override
    public String toString() {
        return "JsonPipeline{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", statusesIdAndName=" + statusesIdAndName +
                '}';
    }
}
