package ua.adeptius.amocrm.model.json;


import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JsonAmoAccount {

//    private HashMap<String, String> leadStatuses = new HashMap<>();
    private String phoneEnumId = null;
    private String phoneId = null;
    private HashMap<String, String> usersIdAndName = new HashMap<>();
    private HashMap<String, String> usersNameAndId = new HashMap<>();
    private List<JsonPipeline> pipelines = new ArrayList<>();
    private String current_user_id;

    public JsonAmoAccount(String json) {
        JSONObject jAccount = new JSONObject(json);

        current_user_id = "" + jAccount.getInt("current_user");


        // Получаем этапы сделок
//        JSONArray jLeadStatuses = jAccount.getJSONArray("leads_statuses");
//        for (int i = 0; i < jLeadStatuses.length(); i++) {
//            JSONObject jStatus = jLeadStatuses.getJSONObject(i);
//            String id = jStatus.getString("id");
//            String name = jStatus.getString("name");
//            leadStatuses.put(id, name);
//        }

        //Получаем id поля куда сохранять номер телефона
        JSONObject jCustomFields = jAccount.getJSONObject("custom_fields");
        JSONArray jCompanies = jCustomFields.getJSONArray("contacts");

        boolean enumsFounded = false;
        for (int i = 0; i < jCompanies.length(); i++) {
            if (enumsFounded){
                break;
            }
            JSONObject jCompany = jCompanies.getJSONObject(i);
            phoneId = jCompany.getString("id");
            String name = jCompany.getString("name");
            String code = jCompany.getString("code");
            if ("PHONE".equals(code)){
                JSONObject jEnums = jCompany.getJSONObject("enums");
                Map<String, Object> enumsMap = jEnums.toMap();
                for (Map.Entry<String, Object> entry : enumsMap.entrySet()) {
                    if (entry.getValue().equals("WORK")){
                        phoneEnumId = entry.getKey();
                        enumsFounded = true;
                        break;
                    }
                }
            }
        }

        JSONArray jUsers = jAccount.getJSONArray("users");
        for (int i = 0; i < jUsers.length(); i++) {
            JSONObject jUser = jUsers.getJSONObject(i);
            String id = jUser.getString("id");
            String name = jUser.getString("name");
            usersIdAndName.put(id, name);
            usersNameAndId.put(name, id);
        }


        JSONObject jPipelines = jAccount.getJSONObject("pipelines");

        Iterator<String> keys = jPipelines.keys();

        while (keys.hasNext()){
            String next = keys.next();
            JSONObject jPipeline = jPipelines.getJSONObject(next);
            pipelines.add(new JsonPipeline(jPipeline));
        }
    }

    public List<JsonPipeline> getPipelines() {
        return pipelines;
    }

    public void setPhoneEnumId(@Nonnull String phoneEnumId) {
        this.phoneEnumId = phoneEnumId;
    }

//    @NotNull
//    public HashMap<String, String> getLeadStatuses() {
//        return leadStatuses;
//    }

    @NotNull
    public String getPhoneEnumId() {
        return phoneEnumId;
    }

    @NotNull
    public String getPhoneId() {
        return phoneId;
    }

    public String getCurrent_user_id() {
        return current_user_id;
    }

    public HashMap<String, String> getUsersIdAndName() {
        return usersIdAndName;
    }

    public HashMap<String, String> getUsersNameAndId() {
        return usersNameAndId;
    }

    @Override
    public String toString() {
        return "JsonAmoAccount{" +
//                "leadStatuses=" + leadStatuses +
                ", phoneEnumId='" + phoneEnumId + '\'' +
                ", phoneId='" + phoneId + '\'' +
                ", usersIdAndName=" + usersIdAndName +
                ", usersNameAndId=" + usersNameAndId +
                ", pipelines=" + pipelines +
                ", current_user_id='" + current_user_id + '\'' +
                '}';
    }
}
