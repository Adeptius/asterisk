package ua.adeptius.asterisk;


import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.NewMySqlDao;
import ua.adeptius.asterisk.javafx.Gui;
import ua.adeptius.asterisk.model.AsteriskMonitor;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.Cleaner;
import ua.adeptius.asterisk.utils.Settings;


public class Main {

    public static AsteriskMonitor monitor;
    public static NewMySqlDao newMySqlDao;
    public static Gui gui;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }

    private void init() throws Exception {
        Settings.load(this.getClass());
        newMySqlDao = new NewMySqlDao();
        newMySqlDao.init();
        MainController.sites = newMySqlDao.getSites();

        monitor = new AsteriskMonitor();
        monitor.run();

        new Thread(() -> {
            gui = new Gui();
            gui.startGui();
        }).start();

        new Cleaner();
    }
}
