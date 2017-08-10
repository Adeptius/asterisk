package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.json.Message;

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
}