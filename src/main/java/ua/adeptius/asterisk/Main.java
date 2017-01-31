package ua.adeptius.asterisk;


import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.MySqlDao;
import ua.adeptius.asterisk.javafx.Gui;
import ua.adeptius.asterisk.model.AsteriskMonitor;
import ua.adeptius.asterisk.utils.PhonesWatcher;
import ua.adeptius.asterisk.utils.Settings;


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
    }
}
