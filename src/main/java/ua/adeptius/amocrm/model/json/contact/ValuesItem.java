package ua.adeptius.amocrm.model.json.contact;


import org.codehaus.jackson.annotate.JsonProperty;

public class ValuesItem {
    @JsonProperty("value")
    private String value;
    @JsonProperty("enum")
    private String enumValue;
}