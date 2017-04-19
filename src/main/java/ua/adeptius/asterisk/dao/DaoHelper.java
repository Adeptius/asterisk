package ua.adeptius.asterisk.dao;


import java.util.ArrayList;
import java.util.Arrays;

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


//    public static String createSqlQueryForDeleteSite(String site) {
//        return "DELETE from users_calltracking WHERE name = '" + site + "'";
//    }

//    public static String createSqlQueryForSaveSite(OldSite oldSite) {
//        String name = oldSite.getName();
//        String email = oldSite.geteMail();
//        String standartNumber = oldSite.getStandartNumber();
//        String googleId = oldSite.getGoogleAnalyticsTrackingId();
//        String blackList = "";
//        String password = oldSite.getPassword();
//        String timeToBlock = oldSite.getTimeToBlock() + "";
//        int outerPhones = oldSite.getOuterNumbersCount();
//
//        List<String> blackIPList = oldSite.getBlackIps();
//        for (String s : blackIPList) {
//            blackList += "," + s;
//        }
//        if (blackList.startsWith(",")) {
//            blackList = blackList.substring(1);
//        }
//
//
//        return "INSERT INTO users_calltracking VALUES("
//                + "'" + name + "',"
//                + "'" + googleId + "',"
//                + "'" + email + "',"
//                + "'" + standartNumber + "',"
//                + "'" + blackList + "',"
//                + "'" + timeToBlock + "',"
//                + "'" + password + "',"
//                + "'" + outerPhones + "')";
//    }


//    public static String getQueryForSaveTelephonyCustomer(TelephonyCustomer newCustomer) {
//        String name = newCustomer.getName();
//        String email = newCustomer.geteMail();
//        String googleId = newCustomer.getGoogleAnalyticsTrackingId();
//        int innerPhones = newCustomer.getInnerNumbersCount();
//        int outerPhones = newCustomer.getOuterNumbersCount();
//        String password = newCustomer.getPassword();
//
//        return "INSERT INTO users_telephony VALUES("
//                + "'" + name + "',"
//                + "'" + password + "',"
//                + "'" + email + "',"
//                + "'" + googleId + "',"
//                + "'" + innerPhones + "',"
//                + "'" + outerPhones + "')";
//    }
//
//    public static String getQueryForEditTelephonyCustomer(TelephonyCustomer newCustomer) {
//        String name = newCustomer.getName();
//        String email = newCustomer.geteMail();
//        String googleId = newCustomer.getGoogleAnalyticsTrackingId();
//        int innerPhones = newCustomer.getInnerNumbersCount();
//        int outerPhones = newCustomer.getOuterNumbersCount();
//        String password = newCustomer.getPassword();
//
//        return "UPDATE `users_telephony` SET "
//                + "`password`='" + password + "', "
//                + "`email`='" + email + "', "
//                + "`tracking_id`='" + googleId + "', "
//                + "`inner_phones`='" + innerPhones + "', "
//                + "`outer_phones`='" + outerPhones + "' "
//                + "WHERE "
//                + "`name`='" + name + "'";
//    }
//
//    public static String createSqlQueryForDeleteTelephonyCustomer(String name) {
//        return "DELETE from users_telephony WHERE name = '" + name + "'";
//    }
}
