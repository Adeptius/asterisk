package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.newmodel.User;
import ua.adeptius.asterisk.webcontrollers.AdminController;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.senders.GoogleAnalitycs;
import ua.adeptius.asterisk.senders.Mail;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.*;

import static ua.adeptius.asterisk.utils.logging.LogCategory.*;

@SuppressWarnings("Duplicates")
public class MainController {

    public static List<OldSite> oldSites;
    public static List<TelephonyCustomer> telephonyCustomers;

    public static List<User> users;

    public static User getUserByName(String name) throws NoSuchElementException {
        return users.stream().filter(user -> user.getLogin().equals(name)).findFirst().get();
    }

    public static boolean isLogin(String name, String pass) {
        if (pass.equals(AdminController.ADMIN_PASS)) {
            return true;
        }
        User user;
        try {
            user = getUserByName(name);
        } catch (NoSuchElementException e) {
            return false;
        }
        return user.getPassword().equals(pass);
    }

    public static boolean isTelephonyLogin(String name, String pass) {
        if (pass.equals(AdminController.ADMIN_PASS)) {
            return true;
        }

        try {
            return getTelephonyCustomerByName(name).getPassword().equals(pass);
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static boolean isSiteLogin(String name, String pass) {
        if (pass.equals(AdminController.ADMIN_PASS)) {
            return true;
        }
        try {
            return getSiteByName(name).getPassword().equals(pass);
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static OldSite getSiteByName(String name) throws NoSuchElementException {
        return oldSites.stream().filter(site -> site.getName().equals(name)).findFirst().get();
    }

    public static TelephonyCustomer getTelephonyCustomerByName(String name) throws NoSuchElementException {
        return telephonyCustomers.stream().filter(tc -> tc.getName().equals(name)).findFirst().get();
    }

    public static String getFreeNumberFromSite(OldSite oldSite, String googleId, String ip, String pageRequest) throws NoSuchElementException {

        //Проверка не находится ли пользователь в черном списке
        if (oldSite.getBlackIps().stream().anyMatch(s -> s.equals(ip))) {
            MyLogger.log(BLOCKED_BY_IP, oldSite.getName() + ": пользователь с ip " + ip + " исключен. Выдан стандартный номер.");
            return oldSite.getStandartNumber();
        }

        List<Phone> phones = oldSite.getPhones();
        // проверка: выдан ли номер пользователю по googleID
        for (Phone phone : phones) {
            if (phone.getGoogleId().equals(googleId)) {
                MyLogger.log(REPEATED_REQUEST, oldSite.getName() + ": пользователю " + googleId + " уже выдан номер " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }

//         проверка: выдан ли номер пользователю по ip
        for (Phone phone : phones) {
            if (phone.getIp().equals(ip)) {
                MyLogger.log(REPEATED_REQUEST, oldSite.getName() + ": пользователю c ip " + ip + " уже выдан номер " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }

        MyLogger.log(REQUEST_NUMBER, oldSite.getName() + ": запрос номера googleId: " + googleId);
        for (Phone phone : phones) {
            if (phone.isFree()) {
                phone.setGoogleId(googleId);
                phone.setIp(ip);
                MyLogger.log(SENDING_NUMBER, oldSite.getName() + ": новому пользователю выдан номер: " + phone.getNumber());
                phone.extendTime();
                phone.setUtmRequest(pageRequest);
                return phone.getNumber();
            }
        }

        MyLogger.log(NO_NUMBERS_LEFT, oldSite.getName() + ": нет свободных номеров.");
        new Mail().checkTimeAndSendEmail(oldSite, "Закончились свободные номера");
        return oldSite.getStandartNumber();
    }

    public static Phone getPhoneByNumber(String number) {
        for (OldSite oldSite : oldSites) {
            List<Phone> phones = oldSite.getPhones();
            for (Phone phone : phones) {
                if (number.length() > 1 && phone.getNumber().endsWith(number)) {
                    return phone;
                }
            }
        }
        MyLogger.log(DB_OPERATIONS, "Телефон " + number + " не найден");
        throw new RuntimeException("Телефон " + number + " не найден");
    }

    public static void onNewSiteCall(Call call) {
        Phone phone = getPhoneByNumber(call.getFirstCall());
        String request = phone.getUtmRequest() == null ? "" : phone.getUtmRequest();
        call.setGoogleId(request);
        call.setGoogleId(phone.getGoogleId());

        new GoogleAnalitycs(call);

        MySqlStatisticDao.saveCall(call);
    }

    public static void onNewTelephonyCall(Call call) {
        MySqlStatisticDao.saveCall(call);
        new GoogleAnalitycs(call);
    }
}
