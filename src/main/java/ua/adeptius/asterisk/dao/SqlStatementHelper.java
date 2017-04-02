package ua.adeptius.asterisk.dao;


import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;

import java.util.List;

public class SqlStatementHelper {


    public static String createSqlQueryForCtreatingStatisticTable(String name) {
        String sql = "CREATE TABLE `" + name + "` (  " +
                "`date` VARCHAR(20) NOT NULL,  " +
                "`direction` VARCHAR(3) NOT NULL,  " +
                "`to` VARCHAR(45) NULL,  " +
                "`from` VARCHAR(45) NULL,  " +
                "`time_to_answer` INT NULL,  " +
                "`talking_time` INT NULL,  " +
                "`google_id` VARCHAR(45) NULL,  " +
                "`call_id` VARCHAR(45) NULL,  " +
                "`utm` VARCHAR(600) NULL,  " +
                "PRIMARY KEY (`date`));";
        return sql;
    }

    public static String createSqlQueryForDeleteSite(String site) {
        return "DELETE from "+MySqlDao.SITE_TABLE+" WHERE name = '" + site + "'";
    }

    public static String createSqlQueryForSaveSite(Site site) {
        String name = site.getName();
        String email = site.getMail();
        String standartNumber = site.getStandartNumber();
        String googleId = site.getGoogleAnalyticsTrackingId();
        String phones = "";
        String blackList = "";
        String password = site.getPassword();
        String timeToBlock = site.getTimeToBlock()+"";
        List<Phone> phoneList = site.getPhones();
        for (Phone phone : phoneList) {
            phones += "," + phone.getNumber();
        }
        if (phones.startsWith(",")) {
            phones = phones.substring(1);
        }

        List<String> blackIPList = site.getBlackIps();
        for (String s : blackIPList) {
            blackList += "," + s;
        }
        if (blackList.startsWith(",")) {
            blackList = blackList.substring(1);
        }

        String sql = "INSERT INTO "+MySqlDao.SITE_TABLE+" VALUES("
                + "'" + name + "',"
                + "'" + googleId + "',"
                + "'" + email + "',"
                + "'" + phones + "',"
                + "'" + standartNumber + "',"
                + "'" + blackList + "',"
                + "'" + timeToBlock + "',"
                + "'" + password + "')";
        return sql;
    }

}
