package ua.adeptius.amocrm.model.json.contact;


import org.codehaus.jackson.annotate.JsonProperty;

public class TagsItem {
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("element_type")
    private Integer elementType;

    @Override
    public String toString() {
        return "TagsItem{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", elementType=" + elementType +
                '}';
    }
}