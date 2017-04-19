package ua.adeptius.asterisk.utils.logging;


import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.utils.logging.LogCategory;

import java.util.LinkedList;

public class MyLogger {

    public static LinkedList<String> logs = new LinkedList<>();

    public static void log(LogCategory category, String message) {
        if (Settings.getSettingBoolean(category.toString())){
            System.out.println(message);
            logs.addFirst(message);
            if (logs.size()>70){
                logs.removeLast();
            }
        }
    }

    public static void logAndThrow(LogCategory category, String message) throws Exception{
        log(category, message);
        throw new Exception(message);
    }
}
