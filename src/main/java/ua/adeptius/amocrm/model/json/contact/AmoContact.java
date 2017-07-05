package ua.adeptius.amocrm.model.json.contact;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;


public class AmoContact {
//    @JsonProperty("closest_task")
//    private Integer closestTask;
//    @JsonProperty("linked_company_id")
//    private String linkedCompanyId;
    @JsonProperty("custom_fields")
    private List<CustomFieldsItem> customFields;
//    @JsonProperty("created_user_id")
//    private Integer createdUserId;
//    @JsonProperty("modified_user_id")
//    private Integer modifiedUserId;
//    @JsonProperty("type")
//    private String type;
    @JsonProperty("responsible_user_id")
    private Integer responsibleUserId;
    @JsonProperty("tags")
    private List<TagsItem> tags;
//    @JsonProperty("account_id")
//    private Integer accountId;
//    @JsonProperty("date_create")
//    private Integer dateCreate;
//    @JsonProperty("group_id")
//    private Integer groupId;
//    @JsonProperty("company_name")
//    private String companyName;
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("last_modified")
    private Integer lastModified;
    @JsonProperty("linked_leads_id")
    private List<String> linkedLeadsId;

//
//    public void setClosestTask(Integer closestTask) {
//        this.closestTask = closestTask;
//    }
//
//    public void setLinkedCompanyId(String linkedCompanyId) {
//        this.linkedCompanyId = linkedCompanyId;
//    }
//
//    public void setCustomFields(List<CustomFieldsItem> customFields) {
//        this.customFields = customFields;
//    }
//
//    public void setCreatedUserId(Integer createdUserId) {
//        this.createdUserId = createdUserId;
//    }
//
//    public void setModifiedUserId(Integer modifiedUserId) {
//        this.modifiedUserId = modifiedUserId;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }

    public void setResponsibleUserId(Integer responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public void setTags(List<TagsItem> tags) {
        this.tags = tags;
    }

//    public void setAccountId(Integer accountId) {
//        this.accountId = accountId;
//    }
//
//    public void setDateCreate(Integer dateCreate) {
//        this.dateCreate = dateCreate;
//    }
//
//    public void setGroupId(Integer groupId) {
//        this.groupId = groupId;
//    }
//
//    public void setCompanyName(String companyName) {
//        this.companyName = companyName;
//    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLastModified(Integer lastModified) {
        this.lastModified = lastModified;
    }

    public void setLinkedLeadsId(List<String> linkedLeadsId) {
        this.linkedLeadsId = linkedLeadsId;
    }
//
//    public Integer getClosestTask() {
//        return closestTask;
//    }
//
//    public String getLinkedCompanyId() {
//        return linkedCompanyId;
//    }
//
//    public List<CustomFieldsItem> getCustomFields() {
//        return customFields;
//    }
//
//    public Integer getCreatedUserId() {
//        return createdUserId;
//    }
//
//    public Integer getModifiedUserId() {
//        return modifiedUserId;
//    }
//
//    public String getType() {
//        return type;
//    }

    public Integer getResponsibleUserId() {
        return responsibleUserId;
    }

    public List<TagsItem> getTags() {
        return tags;
    }
//
//    public Integer getAccountId() {
//        return accountId;
//    }
//
//    public Integer getDateCreate() {
//        return dateCreate;
//    }
//
//    public Integer getGroupId() {
//        return groupId;
//    }
//
//    public String getCompanyName() {
//        return companyName;
//    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }

    public Integer getLastModified() {
        return lastModified;
    }

    public List<String> getLinkedLeadsId() {
        return linkedLeadsId;
    }

    @Override
    public String toString() {
        return "AmoContact{" +
                "customFields=" + customFields +
                ", responsibleUserId=" + responsibleUserId +
                ", tags=" + tags +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", lastModified=" + lastModified +
                ", linkedLeadsId=" + linkedLeadsId +
                '}';
    }


    //    @Override
//    public String toString() {
//        return "AmoContact{" +
//                "closestTask=" + closestTask +
//                ", linkedCompanyId='" + linkedCompanyId + '\'' +
//                ", customFields=" + customFields +
//                ", createdUserId=" + createdUserId +
//                ", modifiedUserId=" + modifiedUserId +
//                ", type='" + type + '\'' +
//                ", responsibleUserId=" + responsibleUserId +
//                ", tags=" + tags +
//                ", accountId=" + accountId +
//                ", dateCreate=" + dateCreate +
//                ", groupId=" + groupId +
//                ", companyName='" + companyName + '\'' +
//                ", name='" + name + '\'' +
//                ", id=" + id +
//                ", lastModified=" + lastModified +
//                ", linkedLeadsId=" + linkedLeadsId +
//                '}';
//    }
}