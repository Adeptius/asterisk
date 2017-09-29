package ua.adeptius.asterisk.dao;


import com.google.gson.stream.JsonWriter;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.Call;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class MySqlStatisticDao {

    private static Logger LOGGER = LoggerFactory.getLogger(MySqlStatisticDao.class.getSimpleName());

    private static ComboPooledDataSource statisticDataSource;

    public static void init() throws Exception {
        String login = Main.settings.getDbUsername();
        String password = Main.settings.getDbPassword();
        statisticDataSource = new ComboPooledDataSource();
        statisticDataSource.setDriverClass("com.mysql.jdbc.Driver");
        statisticDataSource.setJdbcUrl(Main.settings.getDbUrl() + "statisticdb");
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


    public static int getCountStatisticOfRange(String sql) throws Exception {
        try (Connection connection = getStatisticConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            int count = 0;
            while (set.next()) {
                count = set.getInt("COUNT(*)");
            }
            return count;
        }
    }


    public static List<Call> getStatisticOfRange(String sql) throws Exception {
        try (Connection connection = getStatisticConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            List<Call> statisticList = new ArrayList<>();
            while (set.next()) {
                Call call = new Call();
                call.setId(set.getInt("id"));
                call.setCalledDate(set.getString("called_date"));
                call.setDirection(Call.Direction.valueOf(set.getString("direction")));
                call.setCalledFrom(set.getString("called_from"));
                call.setCalledTo(Arrays.asList(set.getString("called_to")));
                call.setCallState(Call.CallState.valueOf(set.getString("call_state")));
                call.setSecondsFullTime(set.getInt("seconds_full_time"));
                call.setSecondsTalk(set.getInt("seconds_talk"));
                call.setAsteriskId(set.getString("call_id"));
                call.setGoogleId(set.getString("google_id"));
                call.setUtmSource(set.getString("utm_source"));
                call.setUtmMedium(set.getString("utm_medium"));
                call.setUtmCampaign(set.getString("utm_campaign"));
                call.setUtmTerm(set.getString("utm_term"));
                call.setUtmContent(set.getString("utm_content"));
                call.setOuterNumber(set.getString("outer_number"));
                call.setComment(set.getString("comment"));
                call.setNewLead(set.getBoolean("new_lead"));
                statisticList.add(call);
            }
            return statisticList;
        }
    }


//    public static List<Call> getStatisticOfRangeOutputStream(String user, String startDate, String endDate, String direction,
//                                                             int limit, int offset, OutputStream stream) throws Exception {
//        LOGGER.trace("{}: запрос статистики с {} по {} направление {}", user,startDate, endDate, direction);
//        String directionQuery = "direction = '" + direction + "' AND ";
//        if (direction.equals("BOTH")){
//            directionQuery = "";
//        }
//
//        String sql = "SELECT * FROM " + user +
//                " WHERE " +
//                directionQuery +
//                "calledDate BETWEEN STR_TO_DATE('" + startDate +
//                "', '%Y-%m-%d %H:%i:%s') AND STR_TO_DATE('" + endDate +
//                "', '%Y-%m-%d %H:%i:%s')  LIMIT "+limit+" OFFSET "+offset;
//        try (Connection connection = getStatisticConnection();
//             Statement statement = connection.createStatement()) {
//            ResultSet rs = statement.executeQuery(sql);
//            List<Call> statisticList = new ArrayList<>();
////            while (set.next()) {
////                Call call = new Call();
////                call.setCalledDate(set.getString("calledDate"));
////                call.setDirection(Call.Direction.valueOf(set.getString("direction")));
////                call.setCalledFrom(set.getString("calledFrom"));
////                call.setCalledTo(Arrays.asList(set.getString("calledTo")));
////                call.setCallState(Call.CallState.valueOf(set.getString("callState")));
////                call.setSecondsFullTime(set.getInt("secondsFullTime"));
////                call.setSecondsTalk(set.getInt("secondsTalk"));
////                call.setAsteriskId(set.getString("call_id"));
////                call.setGoogleId(set.getString("google_id"));
////                call.setUtm(set.getString("utm"));
////                statisticList.add(call);
////            }
//
//            JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, "UTF-8"));
//            writer.beginArray();
//            ResultSetMetaData rsmd = rs.getMetaData();
//            while(rs.next()) {
//                writer.beginObject();
//                // loop rs.getResultSetMetadata columns
//                for(int idx=1; idx<=rsmd.getColumnCount(); idx++) {
//                    writer.name(rsmd.getColumnLabel(idx)); // write key:value pairs
//                    writer.value(rs.getString(idx));
//                }
//                writer.endObject();
//            }
//            writer.endArray();
//            writer.close();
//            stream.flush();
//            return statisticList;
//        } catch (Exception e) {
//            LOGGER.error(user+": ошибка получения истории с бд с "+startDate+" по "+endDate+" направление "+direction, e);
//        }
//        throw new Exception("Ошибка при загрузке статистики с БД");
//    }

    public static void deleteTables(List<String> tablesToDelete) {
        for (String s : tablesToDelete) {
            LOGGER.trace("Удаляем таблицу {}", s);
            String sql = "DROP TABLE `" + s + "`";
            try (Connection connection = getStatisticConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                LOGGER.debug("Таблица статистики {} была удалена.", s);
            } catch (Exception e) {
                LOGGER.error("Ошибка удаления таблицы " + s, e);
            }
        }
    }

    public static void createStatisticTables(List<String> tablesToCreate) {
        for (String s : tablesToCreate) {
            LOGGER.trace("Создание таблицы {}", s);
            String sql = "CREATE TABLE `" + s + "` (  " +
                    "`id` INT NOT NULL AUTO_INCREMENT,  " +
                    "`called_date` VARCHAR(20) NOT NULL,  " +
                    "`direction` VARCHAR(3) NOT NULL,  " +
                    "`called_from` VARCHAR(13) NULL,  " +
                    "`called_to` VARCHAR(45) NULL,  " +
                    "`call_state` VARCHAR(11) NOT NULL,  " +
                    "`seconds_full_time` TINYINT(4) NULL,  " +
                    "`seconds_talk` TINYINT(4) NULL,  " +
                    "`call_id` VARCHAR(18) NULL,  " +
                    "`google_id` VARCHAR(24) NULL,  " +
                    "`utm_source` VARCHAR(45) NULL,  " +
                    "`utm_medium` VARCHAR(45) NULL,  " +
                    "`utm_campaign` VARCHAR(45) NULL,  " +
                    "`utm_term` VARCHAR(45) NULL,  " +
                    "`utm_content` VARCHAR(45) NULL,  " +
                    "`outer_number` VARCHAR(13) NULL,  " +
                    "`comment` VARCHAR(100) NULL,  " +
                    "`new_lead` TINYINT(1) NOT NULL,  " +
                    "PRIMARY KEY (`id`));";
            try (Connection connection = getStatisticConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                LOGGER.debug("Таблица статистики {} была создана.", s);
            } catch (Exception e) {
                LOGGER.error("Ошибка создания таблицы" + s, e);
            }
        }
    }

    public static void saveCall(Call call) {
        String calledTo = call.getCalledTo().get(0);
        String login = call.getUser().getLogin();
        String calledDate = call.getCalledDate();
        Call.Direction direction = call.getDirection();
        String calledFrom = call.getCalledFrom();
        Call.CallState callState = call.getCallState();
        int secondsTalk = call.getSecondsTalk();
        int secondsFullTime = call.getSecondsFullTime();
        String asteriskId = call.getAsteriskId();
        String googleId = call.getGoogleId();
        String utmSource = call.getUtmSource();
        String utmMedium = call.getUtmMedium();
        String utmCampaign = call.getUtmCampaign();
        String utmTerm = call.getUtmTerm();
        String utmContent = call.getUtmContent();
        String outerNumber = call.getOuterNumber();
        String comment = call.getComment();
        boolean newLead = call.isNewLead();

        LOGGER.trace("{}: cохранение звонка в БД. {} -> {}", login, calledFrom, calledTo);
        StringBuilder sb = new StringBuilder(28);
        sb.append("INSERT INTO `").append(login)
                .append("` (`called_date`, `direction`, `called_from`, `called_to`, `call_state`, `seconds_full_time`, `seconds_talk`, `call_id`, `google_id`, `utm_source`, `utm_medium`, `utm_campaign`, `utm_term`, `utm_content`, `outer_number`, `comment`, `new_lead`) VALUES ('")
                .append(calledDate).append("', '")
                .append(direction).append("', '")
                .append(calledFrom).append("', '")
                .append(calledTo).append("', '")
                .append(callState).append("', '")
                .append(secondsFullTime).append("', '")
                .append(secondsTalk).append("', '")
                .append(asteriskId).append("',");

        if (googleId != null) {
            sb.append(" '").append(googleId).append("',");
        } else {
            sb.append(" null,");
        }

        if (utmSource != null) {
            sb.append(" '").append(utmSource).append("',");
        } else {
            sb.append(" null,");
        }

        if (utmMedium != null) {
            sb.append(" '").append(utmMedium).append("',");
        } else {
            sb.append(" null,");
        }

        if (utmCampaign != null) {
            sb.append(" '").append(utmCampaign).append("',");
        } else {
            sb.append(" null,");
        }

        if (utmTerm != null) {
            sb.append(" '").append(utmTerm).append("',");
        } else {
            sb.append(" null,");
        }

        if (utmContent != null) {
            sb.append(" '").append(utmContent).append("',");
        } else {
            sb.append(" null,");
        }

        if (outerNumber != null) {
            sb.append(" '").append(outerNumber).append("',");
        } else {
            sb.append(" null,");
        }

        if (comment != null) {
            sb.append(" '").append(comment).append("',");
        } else {
            sb.append(" null,");
        }

        sb.append(" '").append(newLead ? 1 : 0).append("');");

        try (Connection connection = getStatisticConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sb.toString());
        } catch (Exception e) {
            LOGGER.error(login + ": ошибка сохранения звонка " + calledFrom + " -> " + calledTo, e);
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
