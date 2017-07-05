package ua.adeptius.amocrm.model.json.contact;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Contact {

    private int id = -1;
    private String name = "";
    private int last_modified = -1;
    private HashMap<Integer, String> tags = new HashMap<>();
    private ArrayList<String> linked_leads_id = new ArrayList<>();
    private ArrayList<CustomField> customFields = new ArrayList<>();



    public Contact(String json) {
        JSONObject jContact = new JSONObject(json);

        id = jContact.getInt("id");
        name = jContact.getString("name");
        last_modified = jContact.getInt("last_modified");

        JSONArray jTags = jContact.getJSONArray("tags");
        for (int i = 0; i < jTags.length(); i++) {
            JSONObject jTag = jTags.getJSONObject(i);
            tags.put(jTag.getInt("id"), jTag.getString("name"));
        }

        JSONArray jCustomFields = jContact.getJSONArray("custom_fields");
        for (int i = 0; i < jCustomFields.length(); i++) {
            JSONObject jCustomField = jCustomFields.getJSONObject(i);
            String id = jCustomField.getString("id");
            String name = jCustomField.getString("name");
            String code = jCustomField.getString("code");
            HashMap<String, String> values = new HashMap<>();
            JSONArray jValues = jCustomField.getJSONArray("values");
            for (int j = 0; j < jValues.length(); j++) {
                JSONObject jValue = jValues.getJSONObject(j);
                values.put(jValue.getString("enum"), jValue.getString("value"));
            }
            customFields.add(new CustomField(id,name,code,values));
        }


        JSONArray jLinkedLeads = jContact.getJSONArray("linked_leads_id");
        for (int i = 0; i < jLinkedLeads.length(); i++) {
            String lead = jLinkedLeads.getString(i);
            linked_leads_id.add(lead);
        }
    }

    public Contact() {
    }


    private class CustomField {
        public CustomField(String id, String name, String code, HashMap<String, String> values) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.values = values;
        }

        String id;
        String name;
        String code;
        HashMap<String, String> values;

        @Override
        public String toString() {
            return "CustomField{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", code='" + code + '\'' +
                    ", values=" + values +
                    '}';
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"name\":\"").append(name).append("\",");
        sb.append("\"last_modified\":").append(last_modified).append(",");


        // Таги
        sb.append("\"tags\":[");
        final int[] cycle = {0};
        tags.forEach((id, name) -> {
            sb.append("{\"id\":").append(id).append(",\"name\":\"").append(name).append("\"}");
            cycle[0]++;
            if (cycle[0] != tags.size()){ // если элемент не последний
                sb.append(",");
            }
        });
        sb.append("],");


        // Кастом филды
        sb.append("\"custom_fields\":[");
        cycle[0] = 0;
        customFields.forEach(field -> {
            sb.append("{\"id\":\"").append(field.id).append("\",\"name\":\"").append(field.name).append("\",\"code\": \"").append(field.code).append("\",\"values\":[");

            final int[] cycle2 = {0}; // закидываем энамы
            field.values.forEach((enumId, value) -> {
                sb.append("{\"value\":\""+value+"\",\"enum\":\""+enumId+"\"}");
                cycle2[0]++;
                if (cycle2[0] != field.values.size()){ // если элемент не последний
                    sb.append(",");
                }
            });
            sb.append("]}"); // конец энамов

            cycle[0]++;
            if (cycle[0] != customFields.size()){ // если элемент не последний
                sb.append(",");
            }
        });
        sb.append("],"); // конец кастом филдов


        sb.append("\"linked_leads_id\":[");
        for (int i = 0; i < linked_leads_id.size(); i++) {
            sb.append("\"").append(linked_leads_id.get(i)).append("\"");
            if (i != (linked_leads_id.size()-1)){
                sb.append(",");
            }
        }
        sb.append("]");


        sb.append("}");
        return sb.toString();
    }
}
