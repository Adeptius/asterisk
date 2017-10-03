package ua.adeptius.asterisk.json;


import ua.adeptius.amocrm.model.json.JsonPipeline;
import ua.adeptius.asterisk.model.AmoAccount;

import java.util.HashMap;
import java.util.List;

public class JsonAmoForController {

    private String domain;
    private String amoLogin;
    private String apiKey;
    private String[] responsibleUserSchedule;
    private boolean cling;
    private int pipelineId;
    private int stageId;
    private HashMap <String, String> operatorLocation;
    private HashMap<String, String> usersIdAndName;
    private List<JsonPipeline> pipelines;


    public JsonAmoForController() {
    }

    public JsonAmoForController(AmoAccount amoAccount) {
        domain = amoAccount.getDomain();
        amoLogin = amoAccount.getAmoLogin();
        apiKey = amoAccount.getApiKey();
        responsibleUserSchedule = amoAccount.getResponsibleUserSchedule();
        cling = amoAccount.isCling();
        pipelineId = amoAccount.getPipelineId();
        stageId = amoAccount.getStageId();
    }


    public List<JsonPipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<JsonPipeline> pipelines) {
        this.pipelines = pipelines;
    }

    public HashMap<String, String> getUsersIdAndName() {
        return usersIdAndName;
    }

    public void setUsersIdAndName(HashMap<String, String> usersIdAndName) {
        this.usersIdAndName = usersIdAndName;
    }

    public HashMap<String, String> getOperatorLocation() {
        return operatorLocation;
    }

    public void setOperatorLocation(HashMap<String, String> operatorLocation) {
        this.operatorLocation = operatorLocation;
    }

    public String[] getResponsibleUserSchedule() {
        return responsibleUserSchedule;
    }

    public void setResponsibleUserSchedule(String[] responsibleUserSchedule) {
        this.responsibleUserSchedule = responsibleUserSchedule;
    }

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

    public int getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(int pipelineId) {
        this.pipelineId = pipelineId;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
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
