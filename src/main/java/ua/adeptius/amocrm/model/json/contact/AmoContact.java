package ua.adeptius.amocrm.model.json.contact;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class AmoContact {
    @JsonProperty("closest_task")
    private Integer closestTask;
    @JsonProperty("linked_company_id")
    private String linkedCompanyId;
    @JsonProperty("custom_fields")
    private List<CustomFieldsItem> customFields;
    @JsonProperty("created_user_id")
    private Integer createdUserId;
    @JsonProperty("modified_user_id")
    private Integer modifiedUserId;
    @JsonProperty("type")
    private String type;
    @JsonProperty("responsible_user_id")
    private Integer responsibleUserId;
    @JsonProperty("tags")
    private List tags;
    @JsonProperty("account_id")
    private Integer accountId;
    @JsonProperty("date_create")
    private Integer dateCreate;
    @JsonProperty("group_id")
    private Integer groupId;
    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("last_modified")
    private Integer lastModified;
    @JsonProperty("linked_leads_id")
    private List linkedLeadsId;
}