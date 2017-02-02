package ua.adeptius.asterisk.utils;



public class StringUtils {



    public static String getStringedTime(long time){
        time = time /1000;

        int seconds = (int) (time%60);
        int minutes = (int) ((time/60)%60);
        int hours = (int) ((time/60)/60);

        String sec = String.valueOf(seconds).length() == 1 ? "0"+seconds : ""+seconds;

        String stringedTime = sec + "c";
        if (minutes != 0){
            String min = String.valueOf(minutes).length() == 1 ? "0"+minutes : ""+minutes;
            stringedTime = min+ "м " + stringedTime;
        }
        if (hours != 0){
            stringedTime = hours + "ч " + stringedTime;
        }

        return stringedTime;
    }
}
