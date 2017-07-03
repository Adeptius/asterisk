package ua.adeptius.amocrm;


import org.apache.commons.text.StringEscapeUtils;
import ua.adeptius.amocrm.model.json.AmoAccount;
import ua.adeptius.amocrm.monitor.CookieCleaner;

import java.util.List;

public class TestClass {


    public static void main(String[] args) throws Exception {

//        String s = "[{\"id\":17730117,\"name\":\"\\u043d\\u0430\\u0442\\u0430\\u043b\\u044c\\u044f\",\"last_modified\":1496668616,\"account_id\":14624035,\"date_create\":1496668616,\"created_user_id\":1480933,\"modified_user_id\":1480933,\"responsible_user_id\":1480933,\"group_id\":0,\"closest_task\":0,\"linked_company_id\":null,\"company_name\":\"\",\"tags\":[{\"id\":199723,\"name\":\"\\u0438\\u043c\\u043f\\u043e\\u0440\\u0442_05062017_1616\",\"element_type\":1}],\"type\":\"contact\",\"custom_fields\":[{\"id\":\"495303\",\"name\":\"\\u0422\\u0435\\u043b\\u0435\\u0444\\u043e\\u043d\",\"code\":\"PHONE\",\"values\":[{\"value\":\"(097) 148-25-84\",\"enum\":\"1104351\"}]},{\"id\":\"525363\",\"name\":\"\\u0410\\u0434\\u0440\\u0435\\u0441\",\"values\":[{\"value\":\"\\u0434\\u043e\\u043c  \\u043a\\u043e\\u0440\\u043f.  \\u043d\\u043e\\u043c.\",\"subtype\":\"1\"},{\"value\":\"\\u041a\\u0438\\u0435\\u0432\",\"subtype\":\"3\"}]}],\"linked_leads_id\":[\"6913989\"]}],\"server_time\":1499086603}}";
//
//        s = StringEscapeUtils.unescapeJava(s);
//        System.out.println(s);




        new CookieCleaner();

        String domain = "adeptius";
        String userLogin = "adeptius@wid.ua";
        String userApiKey = "a99ead2f473e150091360d25aecc2878";

//        String domain = "wid";
//        String userLogin = "adeptius@wid.ua";
//        String userApiKey = "a99ead2f473e150091360d25aecc2878";

//        String cookie = AmoDAO.auth(domain, userLogin, userApiKey);
//        System.out.println(cookie);

//        AmoAccount amoAccount = AmoDAO.getAllUserInfo(domain, userLogin, userApiKey);
//        List<AmoAccount.Leads_status> leads_statuses = amoAccount.getLeads_statuses();
//        for (AmoAccount.Leads_status leads_status : leads_statuses) {
//            System.out.println(leads_status.getId() + " = " + leads_status.getName());
//        }

//        String contacts = AmoDAO.getContacts(domain, userLogin, userApiKey);
//        System.out.println(contacts);

        int id = AmoDAO.getContactIdByNumber(domain, userLogin, userApiKey, "5381238");

        System.out.println(id);




    }


}
