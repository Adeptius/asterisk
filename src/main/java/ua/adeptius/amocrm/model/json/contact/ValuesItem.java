package ua.adeptius.amocrm.model.json.contact;

import org.codehaus.jackson.annotate.JsonProperty;


public class ValuesItem {
    @JsonProperty("value")
    private String value;
    @JsonProperty("enum")
    private String enumType;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEnumType() {
        return enumType;
    }

    public void setEnumType(String enumType) {
        this.enumType = enumType;
    }

    @Override
    public String toString() {
        return "ValuesItem{" +
                "value='" + value + '\'' +
                ", enumType='" + enumType + '\'' +
                '}';
    }
}