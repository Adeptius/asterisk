package ua.adeptius.amocrm.model.json;


import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonAmoAccount {

    private HashMap<String, String> leadStatuses = new HashMap<>();
    private String phoneEnumId = null;
    String phoneId = null;

    public JsonAmoAccount(String json) {
        JSONObject jAccount = new JSONObject(json);


        // Получаем этапы сделок
        JSONArray jLeadStatuses = jAccount.getJSONArray("leads_statuses");
        for (int i = 0; i < jLeadStatuses.length(); i++) {
            JSONObject jStatus = jLeadStatuses.getJSONObject(i);
            String id = jStatus.getString("id");
            String name = jStatus.getString("name");
            leadStatuses.put(id, name);
        }

        //Получаем id поля куда сохранять номер телефона
        JSONObject jCustomFields = jAccount.getJSONObject("custom_fields");
        JSONArray jCompanies = jCustomFields.getJSONArray("companies");

        for (int i = 0; i < jCompanies.length(); i++) {
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
                        return;
                    }
                }
            }
        }
    }

    @NotNull
    public HashMap<String, String> getLeadStatuses() {
        return leadStatuses;
    }

    @NotNull
    public String getPhoneEnumId() {
        return phoneEnumId;
    }

    @NotNull
    public String getPhoneId() {
        return phoneId;
    }
}
