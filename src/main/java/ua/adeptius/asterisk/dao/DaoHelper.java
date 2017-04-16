package ua.adeptius.asterisk.dao;


import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.Customer;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.TelephonyCustomer;
import ua.adeptius.asterisk.monitor.Call;

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
        return "CREATE TABLE `" + name + "` (  " +
                "`date` VARCHAR(20) NOT NULL,  " +
                "`direction` VARCHAR(3) NOT NULL,  " +
                "`from` VARCHAR(45) NULL,  " +
                "`to` VARCHAR(45) NULL,  " +
                "`callState` VARCHAR(8) NOT NULL,  " +
                "`time_to_answer` INT NULL,  " +
                "`talking_time` INT NULL,  " +
                "`call_id` VARCHAR(45) NULL,  " +
                "`google_id` VARCHAR(45) NULL,  " +
                "`utm` VARCHAR(600) NULL,  " +
                "PRIMARY KEY (`date`));";
    }

    public static String createSqlQueryForCtreatingStatisticTableTelephony(String name) {
        return "CREATE TABLE `" + name + "` (  " +
                "`date` VARCHAR(20) NOT NULL,  " +
                "`direction` VARCHAR(3) NOT NULL,  " +
                "`from` VARCHAR(45) NULL,  " +
                "`to` VARCHAR(45) NULL,  " +
                "`callState` VARCHAR(8) NOT NULL,  " +
                "`time_to_answer` INT NULL,  " +
                "`talking_time` INT NULL,  " +
                "`call_id` VARCHAR(45) NULL,  " +
                "PRIMARY KEY (`date`));";
    }


    public static String createSqlQueryForDeleteSite(String site) {
        return "DELETE from sites WHERE name = '" + site + "'";
    }

    public static String createSqlQueryForSaveSite(Site site) {
        String name = site.getName();
        String email = site.geteMail();
        String standartNumber = site.getStandartNumber();
        String googleId = site.getGoogleAnalyticsTrackingId();
        String blackList = "";
        String password = site.getPassword();
        String timeToBlock = site.getTimeToBlock() + "";
        int outerPhones = site.getOuterNumbersCount();

        List<String> blackIPList = site.getBlackIps();
        for (String s : blackIPList) {
            blackList += "," + s;
        }
        if (blackList.startsWith(",")) {
            blackList = blackList.substring(1);
        }


        return "INSERT INTO sites VALUES("
                + "'" + name + "',"
                + "'" + googleId + "',"
                + "'" + email + "',"
                + "'',"
                + "'" + standartNumber + "',"
                + "'" + blackList + "',"
                + "'" + timeToBlock + "',"
                + "'" + password + "',"
                + "'" + outerPhones + "')";
    }


    public static List<String> findTablesThatNeedToDeleteSite(List<Site> sites, List<String> tables) {
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


    public static List<String> findTablesThatNeedToCreateSite(List<Site> sites, List<String> tables) {
        List<String> sitesAlreadyHave = sites.stream().map(Site::getName).collect(Collectors.toList());
        List<String> sitesNeedToCreate = new ArrayList<>();
        for (String s : sitesAlreadyHave) {
            if (!tables.contains("statistic_" + s)) {
                sitesNeedToCreate.add(s);
            }
        }
        return sitesNeedToCreate;
    }

    public static List<String> findTablesThatNeedToDeleteTelephony(List<TelephonyCustomer> telephonyCustomers, List<String> tables) {
        List<String> tablesToDelete = new ArrayList<>();
        List<String> sitesAlreadyHave = telephonyCustomers.stream().map(TelephonyCustomer::getName).collect(Collectors.toList());
        for (String table : tables) {
            String siteNameFromTable = table.substring(10);

            if (!sitesAlreadyHave.contains(siteNameFromTable)) {
                tablesToDelete.add(table);
            }
        }
        return tablesToDelete;
    }


    public static List<String> findTablesThatNeedToCreateTelephony(List<TelephonyCustomer> telephonyCustomers, List<String> tables) {
        List<String> sitesAlreadyHave = telephonyCustomers.stream().map(TelephonyCustomer::getName).collect(Collectors.toList());
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
        String email = newCustomer.geteMail();
        String googleId = newCustomer.getGoogleAnalyticsTrackingId();
        int innerPhones = newCustomer.getInnerNumbersCount();
        int outerPhones = newCustomer.getOuterNumbersCount();
        String password = newCustomer.getPassword();

        return "INSERT INTO telephony_users VALUES("
                + "'" + name + "',"
                + "'" + password + "',"
                + "'" + email + "',"
                + "'" + googleId + "',"
                + "'" + innerPhones + "',"
                + "'" + outerPhones + "')";
    }

    public static String getQueryForEditTelephonyCustomer(TelephonyCustomer newCustomer) {
        String name = newCustomer.getName();
        String email = newCustomer.geteMail();
        String googleId = newCustomer.getGoogleAnalyticsTrackingId();
        int innerPhones = newCustomer.getInnerNumbersCount();
        int outerPhones = newCustomer.getOuterNumbersCount();
        String password = newCustomer.getPassword();

        return "UPDATE `telephony_users` SET "
                + "`password`='" + password + "', "
                + "`email`='" + email + "', "
                + "`tracking_id`='" + googleId + "', "
                + "`inner_phones`='" + innerPhones + "', "
                + "`outer_phones`='" + outerPhones + "' "
                + "WHERE "
                + "`name`='" + name + "'";
    }

    public static String createSqlQueryForDeleteTelephonyCustomer(String name) {
        return "DELETE from telephony_users WHERE name = '" + name + "'";
    }
}
