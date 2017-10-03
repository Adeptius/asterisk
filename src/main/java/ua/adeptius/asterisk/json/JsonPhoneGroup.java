package ua.adeptius.asterisk.json;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import ua.adeptius.asterisk.model.User;

import java.util.Arrays;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class JsonPhoneGroup {

    @JsonProperty
    private String name;

    private List<String> phones;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    @Override
    public String toString() {
        return "JsonPhoneGroup{" +
                "name='" + name + '\'' +
                ", phones=" + phones +
                '}';
    }
}
