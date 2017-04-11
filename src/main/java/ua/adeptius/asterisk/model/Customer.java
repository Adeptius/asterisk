package ua.adeptius.asterisk.model;


import ua.adeptius.asterisk.dao.ConfigDAO;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

public abstract class Customer {

    protected String name;
    protected String eMail;
    protected String googleAnalyticsTrackingId;
    protected String password;
    protected List<Rule> rules = new ArrayList<>();

    public Customer(String name, String eMail, String googleAnalyticsTrackingId, String password) {
        this.name = name;
        this.eMail = eMail;
        this.googleAnalyticsTrackingId = googleAnalyticsTrackingId;
        this.password = password;
        loadRules();
    }

    public String getName() {
        return name;
    }

    public String geteMail() {
        return eMail;
    }

    public String getGoogleAnalyticsTrackingId() {
        return googleAnalyticsTrackingId;
    }

    public String getPassword() {
        return password;
    }

    public void loadRules(){
        try {
            rules = ConfigDAO.readFromFile(name);
        } catch (NoSuchFileException ignored) {
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void removeRulesFile(){
        try{
            ConfigDAO.removeFile(name);
            MyLogger.log(LogCategory.ELSE, "Файл конфига " + name + ".conf удалён.");
        }catch (Exception ignored){}
    }

    public abstract List<String> getAvailableNumbers();

    public abstract void updateNumbers() throws Exception;

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void saveRules() throws Exception{
        ConfigDAO.writeToFile(name, rules);
    }



    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", eMail='" + eMail + '\'' +
                ", googleAnalyticsTrackingId='" + googleAnalyticsTrackingId + '\'' +
                ", password='" + password + '\'' +
                ", rules=" + rules +
                '}';
    }
}
