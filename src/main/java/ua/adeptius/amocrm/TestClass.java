package ua.adeptius.amocrm;


import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.amocrm.model.json.JsonAmoDeal;

public class TestClass {

    public static void main(String[] args) throws Exception  {
        String userLogin = "adeptius@wid.ua";
        String userApiKey = "a99ead2f473e150091360d25aecc2878";
//        String userLogin = "omix@i.ua";
//        String userApiKey = "a99ead2f473e150091360d25aecc2878";

        String domain = "adeptiustest2";
//        String domain = "wid";
//        String domain = "adeptiustest";


        JsonAmoContact contact = AmoDAO.getContactIdByPhoneNumber(domain, userLogin, userApiKey, "0934027182");
        System.out.println(contact);
        JsonAmoDeal latestDeal = AmoDAO.getContactsLatestActiveDial(domain, userLogin, userApiKey, contact);
        System.out.println(latestDeal);

    }
}
