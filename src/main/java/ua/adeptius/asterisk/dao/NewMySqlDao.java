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

import static ua.adeptius.asterisk.utils.MyLogger.log;
import static ua.adeptius.asterisk.utils.MyLogger.printException;


@SuppressWarnings("ALL")
public class NewMySqlDao {

    private ComboPooledDataSource cpds;
    public static String TABLE = Settings.dbTableName;
//    public static String PHONE = Settings.dbColumnPhoneName;
//    public static String GOOGLEID = Settings.dbColumnGoogleIdName;
//    public static String TIMELEFT = Settings.dbColumnTimeToDieName;

    public void init() throws Exception {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver");
        cpds.setJdbcUrl("jdbc:mysql://"+Settings.dbAdress);
        cpds.setUser(Settings.dbLogin);
        cpds.setPassword(Settings.dbPassword);
        cpds.setMinPoolSize(1);
        cpds.setMaxPoolSize(5);
        cpds.setAcquireIncrement(0);
    }

    private Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            log("Ошибка подключения к базе...", this.getClass());
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
                String[] p = set.getString("phones").split(",");
                List<Phone> phones = new ArrayList<>();
                for (int i = 0; i < p.length; i++) {
                    phones.add(new Phone(p[i]));
                }
                sites.add(new Site(
                        set.getString("name"),
                        set.getString("adress"),
                        phones,
                        set.getString("standart_number"),
                        set.getString("tracking_id"),
                        set.getString("email")
                ));
            }
            return sites;
        } catch (SQLException e) {
            log("", this.getClass());
            printException(e);
            List<String> list = new ArrayList<>();
            list.add(Settings.standartNumber);
        }
        throw new Exception("Ошибка при загрузке данных с БД");
    }



}
