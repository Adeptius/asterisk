package ua.adeptius.asterisk.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.logAndThrow;

public class MySqlCalltrackDao extends MySqlDao {

    public static List<String> getMelodies() throws Exception {
        List<String> melodies = new ArrayList<>();
        String sql = "SELECT * FROM `melodies`";
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                melodies.add(set.getString("name"));
            }
            return melodies;
        } catch (Exception e) {
            e.printStackTrace();
        }
        logAndThrow(DB_OPERATIONS, "Ошибка при загрузке пользователей телефонии с БД");
        throw new Exception("Ошибка при загрузке пользователей телефонии с БД");
    }
}
