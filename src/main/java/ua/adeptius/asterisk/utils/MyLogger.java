package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.Main;

public class MyLogger {


    public static void log(String message, Class clazz ) {
        System.out.println(message);
        try{
            Main.gui.guiController.appendLog(message);
        }catch (NullPointerException e){
//            e.printStackTrace();
        }
    }

    public static void printException(Exception e) {
        if (Settings.showDetailedErrorsLogs){
            e.printStackTrace();
            try{
                Main.gui.guiController.appendLog(e.toString());
            }catch (NullPointerException e2){
                e2.printStackTrace();
            }
        }
    }
}
