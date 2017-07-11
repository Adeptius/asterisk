package ua.adeptius.amocrm.model.json.contact;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@Deprecated
public class CustomFieldsItem {
    @JsonProperty("code")
    private String code;
    @JsonProperty("values")
    private List<ValuesItem> values;
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private String id;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<ValuesItem> getValues() {
        return values;
    }

    public void setValues(List<ValuesItem> values) {
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CustomFieldsItem{" +
                "code='" + code + '\'' +
                ", values=" + values +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}