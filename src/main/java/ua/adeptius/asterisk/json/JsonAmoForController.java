package ua.adeptius.asterisk.json;


public class JsonAmoForController {

    private String domain;
    private String amoLogin;
    private String apiKey;
    private boolean cling;

    public boolean isCling() {
        return cling;
    }

    public void setCling(boolean cling) {
        this.cling = cling;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAmoLogin() {
        return amoLogin;
    }

    public void setAmoLogin(String amoLogin) {
        this.amoLogin = amoLogin;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String toString() {
        return "JsonAmoForController{" +
                "domain='" + domain + '\'' +
                ", amoLogin='" + amoLogin + '\'' +
                ", cling='" + cling + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}
