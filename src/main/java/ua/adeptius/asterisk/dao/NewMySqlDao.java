package ua.adeptius.asterisk.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.Settings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.model.LogCategory.DB_ERROR_CONNECTING;
import static ua.adeptius.asterisk.utils.MyLogger.log;
import static ua.adeptius.asterisk.utils.MyLogger.printException;


public class NewMySqlDao {

    private ComboPooledDataSource cpds;
    public static String TABLE = Settings.getSetting("dbTableName");
//    public static String PHONE = Settings.dbColumnPhoneName;
//    public static String GOOGLEID = Settings.dbColumnGoogleIdName;
//    public static String TIMELEFT = Settings.dbColumnTimeToDieName;

    public void init() throws Exception {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver");
        cpds.setJdbcUrl("jdbc:mysql://"+Settings.getSetting("dbAdress"));
        cpds.setUser(Settings.getSetting("dbLogin"));
        cpds.setPassword(Settings.getSetting("dbPassword"));
        cpds.setMinPoolSize(1);
        cpds.setMaxPoolSize(5);
        cpds.setAcquireIncrement(0);
    }

    private Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            log(DB_ERROR_CONNECTING, "Ошибка подключения к базе...");
            printException(e);
            return null;
        }
    }


    public List<Site> getSites() throws Exception {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT * from "+TABLE;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {

                // парсим телефоны
                String[] p = set.getString("phones").split(",");
                List<Phone> phones = new ArrayList<>();
                int start = 0;
                if (p[0].equals("")) {
                    start=1;
                }
                for (int i = start; i < p.length; i++) {
                    phones.add(new Phone(p[i]));
                }

                // парсим черный список
                List<String> ips = new ArrayList<>();
                String s = set.getString("black_list_ip");
                if (s != null){
                    String[] ip = s.split(",");
                    start = 0;
                    if (ip[0].equals("")) {
                        start=1;
                    }
                    for (int i = start; i < ip.length; i++) {
                        ips.add(ip[i]);
                    }
                }

                sites.add(new Site(
                        set.getString("name"),
                        set.getString("adress"),
                        phones,
                        set.getString("standart_number"),
                        set.getString("tracking_id"),
                        set.getString("email"),
                        ips
                ));
            }
            return sites;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new Exception("Ошибка при загрузке данных с БД");
    }
}
