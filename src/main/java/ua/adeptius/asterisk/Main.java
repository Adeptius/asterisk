package ua.adeptius.asterisk;


import javafx.application.Platform;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.MySqlDao;
import ua.adeptius.asterisk.javafx.Gui;
import ua.adeptius.asterisk.model.AsteriskMonitor;
import ua.adeptius.asterisk.utils.PhonesWatcher;
import ua.adeptius.asterisk.utils.Settings;
import ua.adeptius.asterisk.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class Main {

    public static AsteriskMonitor monitor;
    public static MySqlDao mySqlDao;
    public static Gui gui;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }

    private void init() throws Exception {
        Settings.load(this.getClass());
        mySqlDao = new MySqlDao();
        mySqlDao.init();
        MainController.sites = mySqlDao.getSites();

        monitor = new AsteriskMonitor();
        monitor.run();

        new Thread(() -> {
            gui = new Gui();
            gui.startGui();
        }).start();

        new PhonesWatcher();

        List<String> tables = mySqlDao.getListOfTables();

        List<String> tablesToDelete = Utils.findTablesThatNeedToDelete(MainController.sites, tables);

        mySqlDao.deleteTables(tablesToDelete);






//        new Thread(() -> {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//            while(true){
//                try {
//                    if (reader.readLine().equals("gui")){
//                        try {
////                            gui.stop();
//                            Platform.exit();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        gui = new Gui();
//                        gui.startGui();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }
}
