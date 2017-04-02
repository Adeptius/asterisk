package ua.adeptius.asterisk.telephony;


import ua.adeptius.asterisk.dao.ConfigDAO;

import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.telephony.DestinationType.*;
import static ua.adeptius.asterisk.telephony.ForwardType.*;

public class TestingClass {


    public static void main(String[] args) {
        testWrite();
        testRead();
    }


    private static void testWrite(){
        Rule rule = new Rule("e404");

        rule.addNumberFrom("111");
        rule.addNumberFrom("222");

        rule.addNumberTo("333");
        rule.addNumberTo("444");

        rule.setForwardType(QUEUE);
        rule.setDestinationType(GSM);
        rule.setTime(15);
        rule.setMelody("m(simple)");

        ArrayList<Rule> arrayList = new ArrayList<>();
        arrayList.add(rule);
        try {
            ConfigDAO.writeToFile("e404" , arrayList);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void testRead(){
        try {
            List<Rule> ruleList = ConfigDAO.readFromFile("C:\\Users\\Владимир\\Desktop\\rules\\e404.conf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
