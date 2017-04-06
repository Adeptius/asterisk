package ua.adeptius.asterisk.dao;


import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.TelephonyCustomer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DaoHelper {

    public static String getStringFromList(ArrayList<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(strings.get(i));

        }
        return sb.toString();
    }

    public static ArrayList<String> getListFromString(String s) {
        if (s == null) {
            return new ArrayList<>();
        }
        if (s.startsWith(",")) {
            s = s.substring(1);
        }
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        String[] splitted = s.split(",");
        for (int i = 0; i < splitted.length; i++) {

        }
        return new ArrayList<>(Arrays.asList(splitted));
    }

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
        return "DELETE from " + SitesDao.SITE_TABLE + " WHERE name = '" + site + "'";
    }

    public static String createSqlQueryForSaveSite(Site site) {
        String name = site.getName();
        String email = site.getMail();
        String standartNumber = site.getStandartNumber();
        String googleId = site.getGoogleAnalyticsTrackingId();
        String phones = "";
        String blackList = "";
        String password = site.getPassword();
        String timeToBlock = site.getTimeToBlock() + "";
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

        String sql = "INSERT INTO " + SitesDao.SITE_TABLE + " VALUES("
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


    public static List<String> findTablesThatNeedToDelete(List<Site> sites, List<String> tables) {
        List<String> tablesToDelete = new ArrayList<>();
        List<String> sitesAlreadyHave = sites.stream().map(Site::getName).collect(Collectors.toList());
        for (String table : tables) {
            String siteNameFromTable = table.substring(10);

            if (!sitesAlreadyHave.contains(siteNameFromTable)) {
                tablesToDelete.add(table);
            }
        }
        return tablesToDelete;
    }


    public static List<String> findTablesThatNeedToCreate(List<Site> sites, List<String> tables) {
        List<String> sitesAlreadyHave = sites.stream().map(Site::getName).collect(Collectors.toList());
        List<String> sitesNeedToCreate = new ArrayList<>();
        for (String s : sitesAlreadyHave) {
            if (!tables.contains("statistic_" + s)) {
                sitesNeedToCreate.add(s);
            }
        }
        return sitesNeedToCreate;
    }

    public static String getQueryForSaveTelephonyCustomer(TelephonyCustomer newCustomer) {
        String name = newCustomer.getName();
        String email = newCustomer.getMail();
        String googleId = newCustomer.getGoogleAnalyticsTrackingId();
        String innerPhones = getStringFromList(newCustomer.getInnerPhones());
        String outerPhones = getStringFromList(newCustomer.getOuterPhones());
        String password = newCustomer.getPassword();

        return "INSERT INTO " + TelephonyDao.TELEPHONY_TABLE + " VALUES("
                + "'" + name + "',"
                + "'" + password + "',"
                + "'" + email + "',"
                + "'" + googleId + "',"
                + "'" + innerPhones + "',"
                + "'" + outerPhones + "')";
    }

    public static String getQueryForEditTelephonyCustomer(TelephonyCustomer newCustomer) {
        String name = newCustomer.getName();
        String email = newCustomer.getMail();
        String googleId = newCustomer.getGoogleAnalyticsTrackingId();
        String innerPhones = getStringFromList(newCustomer.getInnerPhones());
        String outerPhones = getStringFromList(newCustomer.getOuterPhones());
        String password = newCustomer.getPassword();

        return "UPDATE `" + TelephonyDao.TELEPHONY_TABLE + "` SET "
                + "`password`='" + password + "', "
                + "`email`='" + email + "', "
                + "`tracking_id`='" + googleId + "', "
                + "`inner_phones`='" + innerPhones + "', "
                + "`outer_phones`='" + outerPhones + "' "
                + "WHERE "
                + "`name`='" + name + "'";
    }

    public static String createSqlQueryForDeleteTelephonyCustomer(String name) {
        return "DELETE from " + TelephonyDao.TELEPHONY_TABLE + " WHERE name = '" + name + "'";
    }
}
