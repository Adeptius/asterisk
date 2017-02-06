package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.model.LogCategory;

import java.util.LinkedList;

public class MyLogger {

    public static LinkedList<String> logs = new LinkedList<>();

    public static void log(LogCategory category, String message) {
        if (Settings.getSettingBoolean(category.toString())){
            if (Settings.getSettingBoolean("ONLY_ACTIVE_SITE") && !message.contains(Settings.getSetting("ACTIVE_SITE"))){
                return;
            }
            System.out.println(message);
            logs.addFirst(message);
            if (logs.size()>70){
                logs.removeLast();
            }
        }
    }

    public static void printException(Exception e) {
        if (true){ // Settings.showDetailedErrorsLogs
            e.printStackTrace();
//            try{
//                Main.gui.guiController.appendLog(e.toString());
//            }catch (NullPointerException e2){
//                e2.printStackTrace();
//            }
        }
    }
}
