package ua.adeptius.amocrm;


import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.amocrm.model.json.JsonAmoDeal;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestClass {

    public static void main(String[] args) throws Exception  {
        User user = new User();
        user.setLogin("e404");
//        AmoAccount amoAccount = new AmoAccount("adeptius@wid.ua", "a99ead2f473e150091360d25aecc2878", "adeptiustest2");
        AmoAccount amoAccount = new AmoAccount("adeptius@wid.ua", "a99ead2f473e150091360d25aecc2878", "wid");
        amoAccount.setUser(user);

//        JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
//        String phoneId = jsonAmoAccount.getPhoneId();
//        String phoneEnumId = jsonAmoAccount.getPhoneEnumId();
//        System.out.println(phoneId + " - " + phoneEnumId);

        JsonAmoContact contact = AmoDAO.getContactIdByPhoneNumber(amoAccount, "0934027182");

        System.out.println(contact);


    }
}
