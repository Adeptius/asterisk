package ua.adeptius.asterisk.model.telephony;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.User;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;


@Entity
@Table(name = "phone_group", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class PhoneGroup {

    private static Logger LOGGER = LoggerFactory.getLogger(PhoneGroup.class.getSimpleName());

    public PhoneGroup() {
    }

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "login")
    private String login;

    @JsonProperty
    @Column(name = "name")
    private String name;

    @Column(name = "phones_string")
    private String phonesString;

    @JsonProperty
    public List<String> getPhones() {
        if (StringUtils.isBlank(phonesString)) {
            return new ArrayList<>();
        }
        String[] splitted = phonesString.split(" ");
        return new ArrayList<>(Arrays.asList(splitted));
    }

    public void setPhones(List<String> list){
        phonesString = "";
        for (int i = 0; i < list.size(); i++) {
            if (i==0){
                phonesString += list.get(i);
            }else {
                phonesString += " " + list.get(i);
            }
        }
    }

    public void setUser(User user) {
        login = user.getLogin();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhoneGroup that = (PhoneGroup) o;

        if (login != null ? !login.equals(that.login) : that.login != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PhoneGroup{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", phonesString='" + phonesString + '\'' +
                '}';
    }
}
