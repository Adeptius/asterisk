package ua.adeptius.asterisk.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.Call;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MySqlStatisticDao {

    private static Logger LOGGER =  LoggerFactory.getLogger(MySqlStatisticDao.class.getSimpleName());

    private static ComboPooledDataSource statisticDataSource;

    public static void init() throws Exception {
        String login = Settings.getSetting("dbLogin");
        String password = Settings.getSetting("dbPassword");
        statisticDataSource = new ComboPooledDataSource();
        statisticDataSource.setDriverClass("com.mysql.jdbc.Driver");
        statisticDataSource.setJdbcUrl("jdbc:mysql://" + Settings.getSetting("dbAdress") + "statisticdb");
        statisticDataSource.setUser(login);
        statisticDataSource.setPassword(password);
        statisticDataSource.setMinPoolSize(1);
        statisticDataSource.setMaxPoolSize(5);
        statisticDataSource.setAcquireIncrement(0);
    }

    protected static Connection getStatisticConnection() throws Exception {
        return statisticDataSource.getConnection();
    }

    public static List<String> getListOfTables() throws Exception {
//        LOGGER.trace("Загрузка списка таблиц");
        String sql = "SHOW TABLES";
        try (Connection connection = getStatisticConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            List<String> listOfTables = new ArrayList<>();
            while (set.next()) {
                listOfTables.add(set.getString("Tables_in_statisticdb"));
            }
            return listOfTables;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Ошибка загрузки списка таблиц из БД", e);
        }
        throw new Exception("Ошибка при загрузке таблиц статистики с БД");
    }

    public static List<Call> getStatisticOfRange(String user, String startDate, String endDate, String direction) throws Exception {
        LOGGER.trace("{}: запрос статистики с {} по {} направление {}", user,startDate, endDate, direction);
        String sql = "SELECT * FROM " + user +
                " WHERE direction = '" + direction +
                "' AND calledDate BETWEEN STR_TO_DATE('" + startDate +
                "', '%Y-%m-%d %H:%i:%s') AND STR_TO_DATE('" + endDate +
                "', '%Y-%m-%d %H:%i:%s')";
        try (Connection connection = getStatisticConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            List<Call> statisticList = new ArrayList<>();
            while (set.next()) {
                Call call = new Call();
                call.setCalledDate(set.getString("calledDate"));
                call.setDirection(Call.Direction.valueOf(set.getString("direction")));
                call.setCalledFrom(set.getString("calledFrom"));
                call.setCalledTo(set.getString("calledTo"));
                call.setCallState(Call.CallState.valueOf(set.getString("callState")));
                call.setSecondsToAnswer(set.getInt("secondsToAnswer"));
                call.setSecondsFullTime(set.getInt("secondsFullTime"));
                call.setAsteriskId(set.getString("call_id"));
                call.setGoogleId(set.getString("google_id"));
                call.setUtm(set.getString("utm"));
                statisticList.add(call);
            }
            return statisticList;
        } catch (Exception e) {
            LOGGER.error(user+": ошибка получения истории с бд с "+startDate+" по "+endDate+" направление "+direction, e);
        }
        throw new Exception("Ошибка при загрузке статистики с БД");
    }

    public static void deleteTables(List<String> tablesToDelete) {
        for (String s : tablesToDelete) {
            LOGGER.trace("Удаляем таблицу {}", s);
            String sql = "DROP TABLE `" + s + "`";
            try (Connection connection = getStatisticConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                LOGGER.debug("Таблица статистики {} была удалена.", s);
            } catch (Exception e) {
                LOGGER.error("Ошибка удаления таблицы "+s, e);
            }
        }
    }

    public static void createStatisticTables(List<String> tablesToCreate) {
        for (String s : tablesToCreate) {
            LOGGER.trace("Создание таблицы {}", s);
            String sql = "CREATE TABLE `" + s + "` (  " +
                    "`calledDate` VARCHAR(20) NOT NULL,  " +
                    "`direction` VARCHAR(3) NOT NULL,  " +
                    "`calledFrom` VARCHAR(45) NULL,  " +
                    "`calledTo` VARCHAR(45) NULL,  " +
                    "`callState` VARCHAR(15) NOT NULL,  " +
                    "`secondsToAnswer` INT NULL,  " +
                    "`secondsFullTime` INT NULL,  " +
                    "`call_id` VARCHAR(45) NULL,  " +
                    "`google_id` VARCHAR(45) NULL,  " +
                    "`utm` VARCHAR(600) NULL,  " +
                    "PRIMARY KEY (`calledDate`));";
            try (Connection connection = getStatisticConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                LOGGER.debug("Таблица статистики {} была создана.", s);
            } catch (Exception e) {
                LOGGER.error("Ошибка создания таблицы"+s, e);
            }
        }
    }

    public static void saveCall(Call call) {
        String calledTo = call.getCalledTo();
        String login = call.getUser().getLogin();
        String calledDate = call.getCalledDate();
        Call.Direction direction = call.getDirection();
        String calledFrom = call.getCalledFrom();
        Call.CallState callState = call.getCallState();
        int secondsToAnswer = call.getSecondsToAnswer();
        int secondsFullTime = call.getSecondsFullTime();
        String asteriskId = call.getAsteriskId();
        String googleId = call.getGoogleId();
        String utm = call.getUtm();

        LOGGER.trace("{}: cохранение звонка в БД. {} -> {}", login, calledFrom, calledTo);
        StringBuilder sb = new StringBuilder(28);
        sb.append("INSERT INTO `").append(login).append("` VALUES ('")
                .append(calledDate).append("', '")
                .append(direction).append("', '")
                .append(calledFrom).append("', '")
                .append(calledTo).append("', '")
                .append(callState).append("', '")
                .append(secondsToAnswer)
                .append("', '")
                .append(secondsFullTime)
                .append("', '")
                .append(asteriskId).append("',");

        if (googleId != null){
                    sb.append(" '").append(googleId).append("',");
        }else {
            sb.append(" null,");
        }

        if (utm != null){
            sb.append(" '").append(utm).append("');");
        }else {
            sb.append(" null);");
        }
        try (Connection connection = getStatisticConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sb.toString());
        } catch (Exception e) {
            LOGGER.error(login +": ошибка сохранения звонка "+calledFrom+" -> "+ calledTo, e);
        }
    }


    public static void createOrCleanStatisticsTables() throws Exception {
        LOGGER.debug("Создание или удаление таблиц статистики");
        List<String> tables = getListOfTables(); // Taблицы в БД
        List<String> customerNames = UserContainer.getUsers().stream().map(User::getLogin).collect(Collectors.toList());
        List<String> tablesToDelete = tables.stream().filter(table -> !customerNames.contains(table)).collect(Collectors.toList());
        List<String> tablesToCreate = customerNames.stream().filter(name -> !tables.contains(name)).collect(Collectors.toList());
        LOGGER.debug("Синхронизация таблиц статистики. Создано: {}, удалено: {}", tablesToCreate.size(), tablesToDelete.size());
        deleteTables(tablesToDelete);
        createStatisticTables(tablesToCreate);
    }
}
