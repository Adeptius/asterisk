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

        JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
        HashMap<String, String> users = jsonAmoAccount.getUsers();

        new Thread(() -> {
            while (users.size() > 0){
                for (Map.Entry<String, String> entry : users.entrySet()) {
                    String id = entry.getKey();
                }
            }

        }).start();

        new Thread(() -> {
            while (users.size() > 0){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Map.Entry<String, String> entry : users.entrySet()) {
                    String id = "" + ((int)Math.random()*5);
                    String id2 = "" + ((int)Math.random()*5);
                    users.put(id, id2);
                    break;
                }
                System.out.println(users.size());

            }
        }).start();
    }
}
