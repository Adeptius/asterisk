package ua.adeptius.asterisk.forwarding;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.adeptius.asterisk.forwarding.DestinationType.*;
import static ua.adeptius.asterisk.forwarding.ForwardType.*;

public class Main {


    public static void main(String[] args) {
        testWrite();
        testRead();
    }


    private static void testWrite(){
        Rule rule = new Rule();

        rule.addNumberFrom("111");
        rule.addNumberFrom("222");

        rule.addNumberTo("333");
        rule.addNumberTo("444");

        rule.setForwardType(BY_TURNS);
        rule.setDestinationType(GSM);
        rule.setTime(15);
        rule.setMelody("m(simple)");

        ArrayList<Rule> arrayList = new ArrayList<>();
        arrayList.add(rule);
        ConfigDAO.writeToFile("e404" , arrayList);

        String s = "Тариф стоит 15 грн";
        Matcher regexMatcher = Pattern.compile("\\(\\d{1,5}\\)?[ ]{0,4}\\d{1,4}-\\d{1,4}-\\d{1,4}").matcher(s);
    }

    private static void testRead(){
        try {
            List<Rule> ruleList = ConfigDAO.readFromFile("C:\\Users\\Владимир\\Desktop\\rules\\e404.conf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
