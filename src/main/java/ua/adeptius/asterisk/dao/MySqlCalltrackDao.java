package ua.adeptius.asterisk.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySqlCalltrackDao extends MySqlDao {

    private static Logger LOGGER =  LoggerFactory.getLogger(MySqlCalltrackDao.class.getSimpleName());


    public static List<String> getMelodies() throws Exception {
//        LOGGER.trace("Запрос списка мелодий");
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
            LOGGER.error("Не удалось загрузить мелодии с БД", e);
        }
        throw new Exception("Ошибка при загрузке пользователей телефонии с БД");
    }
}
