package ua.adeptius.asterisk.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.NewCall;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.log;

public class MySqlStatisticDao extends MySqlDao {

    private static Logger LOGGER =  LoggerFactory.getLogger(MySqlStatisticDao.class.getSimpleName());


    public static List<String> getListOfTables() throws Exception {
        LOGGER.trace("Загрузка списка таблиц");
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
        log(DB_OPERATIONS, "Ошибка при поиске таблиц статистики с БД");
        throw new Exception("Ошибка при загрузке таблиц статистики с БД");
    }

    public static List<NewCall> getStatisticOfRange(String user, String startDate, String endDate, String direction) throws Exception {
        LOGGER.trace("{}: запрос статистики с {} по {} направление {}", user,startDate, endDate, direction);
        String sql = "SELECT * FROM " + user +
                " WHERE direction = '" + direction +
                "' AND date BETWEEN STR_TO_DATE('" + startDate +
                "', '%Y-%m-%d %H:%i:%s') AND STR_TO_DATE('" + endDate +
                "', '%Y-%m-%d %H:%i:%s')";
        try (Connection connection = getStatisticConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            List<NewCall> statisticList = new ArrayList<>();
            while (set.next()) {
                NewCall call = new NewCall();
                call.setCalledDate(set.getString("calledDate"));
                call.setDirection(NewCall.Direction.valueOf(set.getString("direction")));
                call.setCalledFrom(set.getString("calledFrom"));
                call.setCalledTo(set.getString("calledTo"));
                call.setCallState(NewCall.CallState.valueOf(set.getString("callState")));
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
        log(DB_OPERATIONS, "Ошибка при загрузке статистики с БД");
        throw new Exception("Ошибка при загрузке статистики с БД");
    }

    public static void deleteTables(List<String> tablesToDelete) {
        for (String s : tablesToDelete) {
            LOGGER.trace("Удаляем таблицу {}", s);
            String sql = "DROP TABLE `" + s + "`";
            try (Connection connection = getStatisticConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                log(DB_OPERATIONS, "Таблица статистики " + s + " была удалена.");
            } catch (Exception e) {
                LOGGER.error("Ошибка удаления таблицы "+s, e);
                log(DB_OPERATIONS, "Ошибка удаления ненужной таблицы статистики с бд " + s);
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
                log(DB_OPERATIONS, "Таблица статистики " + s + " была создана.");
            } catch (Exception e) {
                LOGGER.error("Ошибка создания таблицы"+s, e);
                log(DB_OPERATIONS, "Ошибка при создании таблицы статистики в БД " + s);
            }
        }
    }

    public static void saveCall(NewCall call) {
        LOGGER.trace("{}: cохранение звонка в БД. {} -> {}", call.getUser().getLogin(), call.getCalledFrom(), call.getCalledTo());
        String sql = "INSERT INTO `" + call.getUser().getLogin() + "` VALUES ('"
                + call.getCalledDate() + "', '"
                + call.getDirection() + "', '"
                + call.getCalledFrom() + "', '"
                + call.getCalledTo() + "', '"
                + call.getCallState() + "', '"
                + call.getSecondsToAnswer() + "', '"
                + call.getSecondsFullTime() + "', '"
                + call.getAsteriskId() + "', '"
                + call.getGoogleId() + "', '"
                + call.getUtm() + "');";
        try (Connection connection = getStatisticConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            LOGGER.error(call.getUser().getLogin()+": ошибка сохранения звонка "+call.getCalledFrom()+" -> "+call.getCalledTo(), e);
            log(DB_OPERATIONS, call.getUser().getLogin() + ": Ошибка при сохранении отчета в БД ");
        }
    }


    public static void createOrCleanStatisticsTables() throws Exception {
        LOGGER.debug("Создание или удаление таблиц статистики");
        List<String> tables = getListOfTables(); // Taблицы в БД
        List<String> customerNames = UserContainer.getUsers().stream().map(User::getLogin).collect(Collectors.toList());
        List<String> tablesToDelete = tables.stream().filter(table -> !customerNames.contains(table)).collect(Collectors.toList());
        List<String> tablesToCreate = customerNames.stream().filter(name -> !tables.contains(name)).collect(Collectors.toList());
        MyLogger.log(DB_OPERATIONS, "Синхронизация таблиц статистики. Создано: " + tablesToCreate.size() + ", удалено: " + tablesToDelete.size());
        deleteTables(tablesToDelete);
        createStatisticTables(tablesToCreate);
    }
}
