package ua.adeptius.asterisk;


import ua.adeptius.asterisk.controllers.DBController;
import ua.adeptius.asterisk.dao.MySqlDao;
import ua.adeptius.asterisk.model.AsteriskMonitor;
import ua.adeptius.asterisk.utils.Cleaner;
import ua.adeptius.asterisk.utils.Settings;


public class Main {

    public static AsteriskMonitor monitor;
    public static MySqlDao mySqlDao;
    public static DBController dbController;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }

    private void init() throws Exception {
        Settings.load(this.getClass());
        mySqlDao = new MySqlDao();
        mySqlDao.init();
        dbController = new DBController();
        dbController.setMySqlDao(mySqlDao);

        monitor = new AsteriskMonitor();
        monitor.setDbController(dbController);
        monitor.run();
        dbController.clearAllDb();

        new Cleaner(dbController);
    }
}
