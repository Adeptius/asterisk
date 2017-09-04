package ua.adeptius.amocrm.model.json;


import com.sun.istack.internal.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class JsonAmoContact {

    private int id = -1;
    private String name = "";
    private int last_modified = -1;
    private ArrayList<String> linked_leads_id = new ArrayList<>();
    private String responsible_user_id;

//    private ArrayList<CustomField> customFields = new ArrayList<>();
    //    private HashMap<Integer, String> tags = new HashMap<>();


    public JsonAmoContact(String json) {
        JSONObject jContact = new JSONObject(json);

        id = jContact.getInt("id");
        name = jContact.getString("name");
        last_modified = jContact.getInt("last_modified");
        responsible_user_id = ""+jContact.getInt("responsible_user_id");

//        JSONArray jTags = jContact.getJSONArray("tags");
//        for (int i = 0; i < jTags.length(); i++) {
//            JSONObject jTag = jTags.getJSONObject(i);
//            tags.put(jTag.getInt("id"), jTag.getString("name"));
//        }

//        JSONArray jCustomFields = jContact.getJSONArray("custom_fields");
//        for (int i = 0; i < jCustomFields.length(); i++) {
//            JSONObject jCustomField = jCustomFields.getJSONObject(i);
//            String id = jCustomField.getString("id");
//            String name = jCustomField.getString("name");
//            String code = jCustomField.getString("code");
//            HashMap<String, String> values = new HashMap<>();
//            JSONArray jValues = jCustomField.getJSONArray("values");
//            for (int j = 0; j < jValues.length(); j++) {
//                JSONObject jValue = jValues.getJSONObject(j);
//                values.put(jValue.getString("enum"), jValue.getString("value"));
//            }
//            customFields.add(new CustomField(id, name, code, values));
//        }


        JSONArray jLinkedLeads = jContact.getJSONArray("linked_leads_id");
        for (int i = 0; i < jLinkedLeads.length(); i++) {
            String lead = jLinkedLeads.getString(i);
            linked_leads_id.add(lead);
        }
    }

    public JsonAmoContact() {
    }

//    public void addCustomFields(CustomField custom) {
//        customFields.add(custom);
//    }


//    public static class CustomField {
//        public CustomField(String id, String name, String code, HashMap<String, String> values) {
//            this.id = id;
//            this.name = name;
//            this.code = code;
//            this.values = values;
//        }
//
//        public CustomField(String id, String name, String code, String phoneEnumId, String contactNumber) {
//            this.id = id;
//            this.name = name;
//            this.code = code;
//            HashMap<String, String> values = new HashMap<>();
//            values.put(phoneEnumId, contactNumber);
//            this.values = values;
//        }
//
//        String id;
//        String name;
//        String code;
//        HashMap<String, String> values;
//
//        @Override
//        public String toString() {
//            return "CustomField{" +
//                    "id='" + id + '\'' +
//                    ", name='" + name + '\'' +
//                    ", code='" + code + '\'' +
//                    ", values=" + values +
//                    '}';
//        }
//    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponsible_user_id() {
        return responsible_user_id;
    }

    public void setResponsible_user_id(String responsible_user_id) {
        this.responsible_user_id = responsible_user_id;
    }

    //    public int getLast_modified() {
//        return last_modified;
//    }

//    public void setLast_modified(int last_modified) {
//        this.last_modified = last_modified;
//    }

//    public HashMap<Integer, String> getTags() {
//        return tags;
//    }
//
//    public void setTags(HashMap<Integer, String> tags) {
//        this.tags = tags;
//    }
//
//    public void addTag(int key, String value) {
//        tags.put(key, value);
//    }
//
//    public void addTag(String value) {
//        tags.put(0, value);
//    }


    public ArrayList<String> getLinked_leads_id() {
        return linked_leads_id;
    }

//    @Nullable
//    public String getLinked_leads_idStringed() {
//
//        String leadsInOneString = "";
//        for (String s : linked_leads_id) {
//            leadsInOneString += "," + s;
//        }
//        if (leadsInOneString.length()>1){
//            leadsInOneString = leadsInOneString.substring(1);
//        }else {
//            return null;
//        }
//        return leadsInOneString;
//    }

    @Deprecated
    public void setLinked_leads_id(ArrayList<String> linked_leads_id) {
        throw new RuntimeException("Ни в коем случае нельзя задавать новый список сделок. Только добавлять, иначе потеряются предыдущие!!");
    }

    public void addLinked_leads_id(String id) {
        linked_leads_id.add(id);
    }

//    public ArrayList<CustomField> getCustomFields() {
//        return customFields;
//    }
//
//    public void setCustomFields(ArrayList<CustomField> customFields) {
//        this.customFields = customFields;
//    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (id != -1) {
            sb.append("\"id\":").append(id).append(",");
        }
        sb.append("\"name\":\"").append(name).append("\",");
        sb.append("\"responsible_user_id\":").append(responsible_user_id).append(",");
        if (last_modified == -1) last_modified = (int) (new Date().getTime() / 1000);
        sb.append("\"last_modified\":").append(last_modified).append(",");


//        // Таги
//        sb.append("\"tags\":\"");
//        final int[] cycle = {0};
//        tags.forEach((id, name) -> {
//            sb.append(name);
//            cycle[0]++;
//            if (cycle[0] != tags.size()) { // если элемент не последний
//                sb.append(",");
//            }
//        });
//        sb.append("\",");
//
//
//        // Кастом филды
//        sb.append("\"custom_fields\":[");
//        cycle[0] = 0;
//        customFields.forEach(field -> {
//            sb.append("{\"id\":\"").append(field.id).append("\",\"name\":\"").append(field.name).append("\",\"code\": \"").append(field.code).append("\",\"values\":[");
//
//            final int[] cycle2 = {0}; // закидываем энамы
//            field.values.forEach((enumId, value) -> {
//                sb.append("{\"value\":\"" + value + "\",\"enum\":\"" + enumId + "\"}");
//                cycle2[0]++;
//                if (cycle2[0] != field.values.size()) { // если элемент не последний
//                    sb.append(",");
//                }
//            });
//            sb.append("]}"); // конец энамов
//
//            cycle[0]++;
//            if (cycle[0] != customFields.size()) { // если элемент не последний
//                sb.append(",");
//            }
//        });
//        sb.append("],"); // конец кастом филдов


        sb.append("\"linked_leads_id\":[");
        for (int i = 0; i < linked_leads_id.size(); i++) {
            sb.append("\"").append(linked_leads_id.get(i)).append("\"");
            if (i != (linked_leads_id.size() - 1)) {
                sb.append(",");
            }
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }
}
