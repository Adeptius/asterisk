package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.javafx.Gui;
import ua.adeptius.asterisk.javafx.GuiController;
import ua.adeptius.asterisk.model.LogCategory;

public class MyLogger {


    public static void log(LogCategory category, String message) {
        if (Settings.getSettingBoolean(category.toString())){
            if (Settings.getSettingBoolean("ONLY_ACTIVE_SITE") && !message.contains(GuiController.selectedSiteString)){
                return;
            }
            System.out.println(message);
//            try{
//                Main.gui.guiController.appendLog(message);
//            }catch (NullPointerException e){
////            e.printStackTrace();
//            }
        }
    }

    public static void printException(Exception e) {
        if (true){ // Settings.showDetailedErrorsLogs
            e.printStackTrace();
            try{
                Main.gui.guiController.appendLog(e.toString());
            }catch (NullPointerException e2){
                e2.printStackTrace();
            }
        }
    }
}
