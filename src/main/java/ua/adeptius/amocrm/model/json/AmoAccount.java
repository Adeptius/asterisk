package ua.adeptius.amocrm.model.json;

import java.util.List;


public class AmoAccount {

    private String id;
    private String name;
    private String subdomain;
    private String currency;
    private boolean paid_from;
    private boolean paid_till;
    private String timezone;
    private String language;
    private String notifications_base_url;
    private String notifications_ws_url;
    private String amojo_base_url;
    private int current_user;
    private int version;
    private String date_pattern;
    private Short_date_pattern short_date_pattern;
    private String date_format;
    private String time_format;
    private String country;
    private String unsorted_on;
    private int mobile_feature_version;
    private String customers_enabled;
    private Limits limits;
    private List<User> users;
    private List<Group> groups;
    private List<Leads_status> leads_statuses;
    private Custom_fields custom_fields;
    private List<Note_type> note_types;
    private List<Task_type> task_types;
    private Pipelines pipelines;
    private String timezoneoffset;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean getPaid_from() {
        return paid_from;
    }

    public boolean getPaid_till() {
        return paid_till;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getLanguage() {
        return language;
    }

    public String getNotifications_base_url() {
        return notifications_base_url;
    }

    public String getNotifications_ws_url() {
        return notifications_ws_url;
    }

    public String getAmojo_base_url() {
        return amojo_base_url;
    }

    public int getCurrent_user() {
        return current_user;
    }

    public int getVersion() {
        return version;
    }

    public String getDate_pattern() {
        return date_pattern;
    }

    public Short_date_pattern getShort_date_pattern() {
        return short_date_pattern;
    }

    public String getDate_format() {
        return date_format;
    }

    public String getTime_format() {
        return time_format;
    }

    public String getCountry() {
        return country;
    }

    public String getUnsorted_on() {
        return unsorted_on;
    }

    public int getMobile_feature_version() {
        return mobile_feature_version;
    }

    public String getCustomers_enabled() {
        return customers_enabled;
    }

    public Limits getLimits() {
        return limits;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<Leads_status> getLeads_statuses() {
        return leads_statuses;
    }

    public Custom_fields getCustom_fields() {
        return custom_fields;
    }

    public List<Note_type> getNote_types() {
        return note_types;
    }

    public List<Task_type> getTask_types() {
        return task_types;
    }

    public Pipelines getPipelines() {
        return pipelines;
    }

    public String getTimezoneoffset() {
        return timezoneoffset;
    }

    public static class Short_date_pattern {
        private String date;
        private String time;
        private String date_time;

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getDate_time() {
            return date_time;
        }
    }

    public static class Limits {
        private boolean users_count;
        private boolean contacts_count;
        private boolean active_deals_count;

        public boolean getUsers_count() {
            return users_count;
        }

        public boolean getContacts_count() {
            return contacts_count;
        }

        public boolean getActive_deals_count() {
            return active_deals_count;
        }
    }

    public static class User {
        private String id;
        private String mail_admin;
        private String name;
        private String last_name;
        private String login;
        private String photo_url;
        private String phone_number;
        private String language;
        private String is_admin;
        private String unsorted_access;
        private String catalogs_access;
        private int group_id;
        private String rights_lead_add;
        private String rights_lead_view;
        private String rights_lead_edit;
        private String rights_lead_delete;
        private String rights_lead_export;
        private String rights_contact_add;
        private String rights_contact_view;
        private String rights_contact_edit;
        private String rights_contact_delete;
        private String rights_contact_export;
        private String rights_company_add;
        private String rights_company_view;
        private String rights_company_edit;
        private String rights_company_delete;
        private String rights_company_export;
        private boolean free_user;

        public String getId() {
            return id;
        }

        public String getMail_admin() {
            return mail_admin;
        }

        public String getName() {
            return name;
        }

        public String getLast_name() {
            return last_name;
        }

        public String getLogin() {
            return login;
        }

        public String getPhoto_url() {
            return photo_url;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public String getLanguage() {
            return language;
        }

        public String getIs_admin() {
            return is_admin;
        }

        public String getUnsorted_access() {
            return unsorted_access;
        }

        public String getCatalogs_access() {
            return catalogs_access;
        }

        public int getGroup_id() {
            return group_id;
        }

        public String getRights_lead_add() {
            return rights_lead_add;
        }

        public String getRights_lead_view() {
            return rights_lead_view;
        }

        public String getRights_lead_edit() {
            return rights_lead_edit;
        }

        public String getRights_lead_delete() {
            return rights_lead_delete;
        }

        public String getRights_lead_export() {
            return rights_lead_export;
        }

        public String getRights_contact_add() {
            return rights_contact_add;
        }

        public String getRights_contact_view() {
            return rights_contact_view;
        }

        public String getRights_contact_edit() {
            return rights_contact_edit;
        }

        public String getRights_contact_delete() {
            return rights_contact_delete;
        }

        public String getRights_contact_export() {
            return rights_contact_export;
        }

        public String getRights_company_add() {
            return rights_company_add;
        }

        public String getRights_company_view() {
            return rights_company_view;
        }

        public String getRights_company_edit() {
            return rights_company_edit;
        }

        public String getRights_company_delete() {
            return rights_company_delete;
        }

        public String getRights_company_export() {
            return rights_company_export;
        }

        public boolean getFree_user() {
            return free_user;
        }
    }

    public static class Group {
    }

    public static class Leads_status {
        private String id;
        private String name;
        private int pipeline_id;
        private String sort;
        private String color;
        private String editable;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getPipeline_id() {
            return pipeline_id;
        }

        public String getSort() {
            return sort;
        }

        public String getColor() {
            return color;
        }

        public String getEditable() {
            return editable;
        }
    }

    public static class Contacts {
        private String id;
        private String name;
        private String code;
        private String multiple;
        private String type_id;
        private String disabled;
        private int sort;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getMultiple() {
            return multiple;
        }

        public String getType_id() {
            return type_id;
        }

        public String getDisabled() {
            return disabled;
        }

        public int getSort() {
            return sort;
        }
    }

    public static class Leads {
    }

    public static class Enums {
        private String json_335383;
        private String json_335385;
        private String json_335387;
        private String json_335389;
        private String json_335391;
        private String json_335393;

        public String getJson_335383() {
            return json_335383;
        }

        public String getJson_335385() {
            return json_335385;
        }

        public String getJson_335387() {
            return json_335387;
        }

        public String getJson_335389() {
            return json_335389;
        }

        public String getJson_335391() {
            return json_335391;
        }

        public String getJson_335393() {
            return json_335393;
        }
    }

    public static class Companies {
        private String id;
        private String name;
        private String code;
        private String multiple;
        private String type_id;
        private String disabled;
        private int sort;
        private Enums enums;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getMultiple() {
            return multiple;
        }

        public String getType_id() {
            return type_id;
        }

        public String getDisabled() {
            return disabled;
        }

        public int getSort() {
            return sort;
        }

        public Enums getEnums() {
            return enums;
        }
    }

    public static class Customers {
    }

    public static class Custom_fields {
        private List<Contacts> contacts;
        private List<Leads> leads;
        private List<Companies> companies;
        private List<Customers> customers;

        public List<Contacts> getContacts() {
            return contacts;
        }

        public List<Leads> getLeads() {
            return leads;
        }

        public List<Companies> getCompanies() {
            return companies;
        }

        public List<Customers> getCustomers() {
            return customers;
        }
    }

    public static class Note_type {
        private int id;
        private String name;
        private String code;
        private String editable;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public String getEditable() {
            return editable;
        }
    }

    public static class Task_type {
        private int id;
        private String name;
        private String code;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }
    }

    public static class Json_15391087 {
        private int id;
        private String name;
        private int pipeline_id;
        private int sort;
        private String color;
        private String editable;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getPipeline_id() {
            return pipeline_id;
        }

        public int getSort() {
            return sort;
        }

        public String getColor() {
            return color;
        }

        public String getEditable() {
            return editable;
        }
    }

    public static class Json_15391090 {
        private int id;
        private String name;
        private int pipeline_id;
        private int sort;
        private String color;
        private String editable;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getPipeline_id() {
            return pipeline_id;
        }

        public int getSort() {
            return sort;
        }

        public String getColor() {
            return color;
        }

        public String getEditable() {
            return editable;
        }
    }

    public static class Json_15391093 {
        private int id;
        private String name;
        private int pipeline_id;
        private int sort;
        private String color;
        private String editable;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getPipeline_id() {
            return pipeline_id;
        }

        public int getSort() {
            return sort;
        }

        public String getColor() {
            return color;
        }

        public String getEditable() {
            return editable;
        }
    }

    public static class Json_15391096 {
        private int id;
        private String name;
        private int pipeline_id;
        private int sort;
        private String color;
        private String editable;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getPipeline_id() {
            return pipeline_id;
        }

        public int getSort() {
            return sort;
        }

        public String getColor() {
            return color;
        }

        public String getEditable() {
            return editable;
        }
    }

    public static class Json_142 {
        private int id;
        private String name;
        private String color;
        private int sort;
        private String editable;
        private int pipeline_id;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        public int getSort() {
            return sort;
        }

        public String getEditable() {
            return editable;
        }

        public int getPipeline_id() {
            return pipeline_id;
        }
    }

    public static class Json_143 {
        private int id;
        private String name;
        private String color;
        private int sort;
        private String editable;
        private int pipeline_id;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        public int getSort() {
            return sort;
        }

        public String getEditable() {
            return editable;
        }

        public int getPipeline_id() {
            return pipeline_id;
        }
    }

    public static class Statuses {
        private Json_15391087 json_15391087;
        private Json_15391090 json_15391090;
        private Json_15391093 json_15391093;
        private Json_15391096 json_15391096;
        private Json_142 json_142;
        private Json_143 json_143;

        public Json_15391087 getJson_15391087() {
            return json_15391087;
        }

        public Json_15391090 getJson_15391090() {
            return json_15391090;
        }

        public Json_15391093 getJson_15391093() {
            return json_15391093;
        }

        public Json_15391096 getJson_15391096() {
            return json_15391096;
        }

        public Json_142 getJson_142() {
            return json_142;
        }

        public Json_143 getJson_143() {
            return json_143;
        }
    }

    public static class Json_659884 {
        private int id;
        private int value;
        private String label;
        private String name;
        private int sort;
        private boolean is_main;
        private Statuses statuses;
        private int leads;

        public int getId() {
            return id;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }

        public int getSort() {
            return sort;
        }

        public boolean getIs_main() {
            return is_main;
        }

        public Statuses getStatuses() {
            return statuses;
        }

        public int getLeads() {
            return leads;
        }
    }

    public static class Pipelines {
        private Json_659884 json_659884;

        public Json_659884 getJson_659884() {
            return json_659884;
        }
    }
}
