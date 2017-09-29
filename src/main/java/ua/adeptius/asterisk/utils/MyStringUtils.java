package ua.adeptius.asterisk.utils;


import org.asteriskjava.manager.event.ManagerEvent;
import ua.adeptius.asterisk.exceptions.UkrainianNumberParseException;
import ua.adeptius.asterisk.json.Message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ua.adeptius.asterisk.json.Message.Status.Error;

public class MyStringUtils {

    public static String getStringedTime(long time){
        time = time /1000;

        int seconds = (int) (time%60);
        int minutes = (int) ((time/60)%60);
        int hours = (int) ((time/60)/60);

        String sec = String.valueOf(seconds).length() == 1 ? "0"+seconds : ""+seconds;

        String stringedTime = sec + "s";
        if (minutes != 0){
            String min = String.valueOf(minutes).length() == 1 ? "0"+minutes : ""+minutes;
            stringedTime = min+ "m " + stringedTime;
        }
        if (hours != 0){
            stringedTime = hours + "h " + stringedTime;
        }

        return stringedTime;
    }

    public static String doTwoSymb(int i) {
        String s = String.valueOf(i);
        if (s.length() == 1) s = "0" + s;
        return s;
    }

    public static boolean validateThatContainsOnlyEngLettersAndNumbers(String stringToValidate){
        String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < stringToValidate.length(); i++) {
            String s = stringToValidate.substring(i, i + 1);
            if (!str.contains(s)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLoginValid(String login){
        String str = "0123456789abcdefghijklmnopqrstuvwxyz-_";
        for (int i = 0; i < login.length(); i++) {
            String s = login.substring(i, i + 1);
            if (!str.contains(s)) {
               return false;
            }
        }
        return true;
    }

    public static String cleanAndValidateUkrainianPhoneNumber(String number) throws UkrainianNumberParseException {
        if (number == null) {
            throw new UkrainianNumberParseException();
        }
        number = number.replaceAll("\\D+", "");

        if (number.startsWith("380")) {
            number = number.substring(2);
        } else if (number.startsWith("80")) {
            number = number.substring(1);
        }
        System.out.println("result filtering: " + number);
        if (number.length() == 10 && number.startsWith("0")) {
            return number;
        }
        throw new UkrainianNumberParseException();
    }

    public static String addZero(String source) {
        try {
            if (source.length() == 9 && !source.startsWith("0")) {
                source = "0" + source;
            }
        } catch (Exception e) {
//            System.out.println("Ошибка добавления нолика. Пришло " + source);
        }
        return source;
    }


    public static String makePrettyLog(ManagerEvent event) {
        String s = event.toString();
        s = s.substring(31);
        if (s.contains("timestamp=null,")) {
            s = s.replaceAll("timestamp=null,", "");
        }
        if (s.contains("sequencenumber=null,")) {
            s = s.replaceAll("sequencenumber=null,", "");
        }
        if (s.contains("server=null,")) {
            s = s.replaceAll("server=null,", "");
        }
        if (s.contains("actionid=null,")) {
            s = s.replaceAll("actionid=null,", "");
        }
        if (s.contains("connectedlinenum=null,")) {
            s = s.replaceAll("connectedlinenum=null,", "");
        }
        if (s.contains("accountcode=null,")) {
            s = s.replaceAll("accountcode=null,", "");
        }
        if (s.contains("connectedlinename=null,")) {
            s = s.replaceAll("connectedlinename=null,", "");
        }
        if (s.contains("calleridname=null,")) {
            s = s.replaceAll("calleridname=null,", "");
        }
        s = removeRegexFromString(s, "dateReceived='.*2017',");
        s = removeRegexFromString(s, "systemHashcode=\\d{8,10}");
        s = removeRegexFromString(s, "channel='SIP\\/\\d*-[\\d|\\w]*',");
        s = removeRegexFromString(s, "privilege='\\w*,\\w*',");
        return s;
    }


    private static String removeRegexFromString(String log, String regex) {
        Matcher regexMatcher = Pattern.compile(regex).matcher(log);
        if (regexMatcher.find()) {
            log = log.replaceAll(regexMatcher.group(), "");
        }
        return log;
    }


    public static String generateRandomKey(){
        String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder(62);
        for (int i = 0; i < 20; i++) {
            int random = (int) (Math.random()*62);
            builder.append(str.charAt(random));
        }
        return builder.toString();
    }
}
