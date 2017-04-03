package ua.adeptius.asterisk.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.TelephonyCustomer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_ERROR_CONNECTING;
import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.log;
import static ua.adeptius.asterisk.utils.logging.MyLogger.logAndThrow;
import static ua.adeptius.asterisk.utils.logging.MyLogger.printException;

@SuppressWarnings("Duplicates")
public class TelephonyDao {

    private ComboPooledDataSource cpds;
    public static final String TELEPHONY_TABLE = "telephony_users";
    public static final String DB_URL =  "jdbc:mysql://" + Settings.getSetting("___dbAdress") + "telephonydb";

    public void init() throws Exception {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver");
        cpds.setJdbcUrl(DB_URL);
        cpds.setUser(Settings.getSetting("___dbLogin"));
        cpds.setPassword(Settings.getSetting("___dbPassword"));
        cpds.setMinPoolSize(1);
        cpds.setMaxPoolSize(2);
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


    public List<TelephonyCustomer> getTelephonyCustomers() throws Exception {
        List<TelephonyCustomer> telephonyCustomers = new ArrayList<>();
        String sql = "SELECT * from "+TELEPHONY_TABLE;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                telephonyCustomers.add(new TelephonyCustomer(
                        set.getString("name"),
                        set.getString("email"),
                        set.getString("tracking_id"),
                        set.getString("password"),
                        DaoHelper.getListFromString(set.getString("inner_phones")),
                        DaoHelper.getListFromString(set.getString("outer_phones"))
                ));
            }
            return telephonyCustomers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        logAndThrow(DB_OPERATIONS, "Ошибка при загрузке пользователей телефонии с БД");
        throw new Exception("Ошибка при загрузке пользователей телефонии с БД");
    }






}
