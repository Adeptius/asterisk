package ua.adeptius.amocrm;


import ua.adeptius.amocrm.model.json.AmoAccount;
import ua.adeptius.amocrm.monitor.CookieCleaner;

import java.util.List;

public class TestClass {


    public static void main(String[] args) throws Exception {
        new CookieCleaner();

//        String domain = "adeptius";
//        String userLogin = "adeptius@wid.ua";
//        String userApiKey = "a99ead2f473e150091360d25aecc2878";

        String domain = "wid";
        String userLogin = "adeptius@wid.u";
        String userApiKey = "a99ead2f473e150091360d25aecc2878";

//        String cookie = AmoDAO.getCookie(domain, userLogin, userApiKey);
//        System.out.println(cookie);


        String cookie = AmoDAO.auth(domain, userLogin, userApiKey);
        System.out.println(cookie);

//        AmoAccount amoAccount = AmoDAO.getAllUserInfo(domain, userLogin, userApiKey);
//        List<AmoAccount.Leads_status> leads_statuses = amoAccount.getLeads_statuses();
//        for (AmoAccount.Leads_status leads_status : leads_statuses) {
//            System.out.println(leads_status.getId() + " = " + leads_status.getName());
//        }
    }


}
