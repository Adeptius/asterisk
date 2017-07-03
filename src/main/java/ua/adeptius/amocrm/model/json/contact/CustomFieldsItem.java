package ua.adeptius.amocrm.model.json.contact;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class CustomFieldsItem {
    @JsonProperty("code")
    private String code;
    @JsonProperty("values")
    private List<ValuesItem> values;
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private String id;
}