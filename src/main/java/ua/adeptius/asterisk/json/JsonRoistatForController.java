package ua.adeptius.asterisk.json;


public class JsonRoistatForController {

    private String projectNumber;
    private String apiKey;

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String toString() {
        return "JsonRoistatForController{" +
                "projectNumber='" + projectNumber + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}
