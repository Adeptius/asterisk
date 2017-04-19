package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.newmodel.Site;
import ua.adeptius.asterisk.newmodel.Telephony;
import ua.adeptius.asterisk.newmodel.User;
import ua.adeptius.asterisk.webcontrollers.AdminController;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.senders.GoogleAnalitycs;
import ua.adeptius.asterisk.senders.Mail;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.*;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.utils.logging.LogCategory.*;

@SuppressWarnings("Duplicates")
public class MainController {

    public static List<User> users;

    public static User getUserByName(String name) throws NoSuchElementException {
        return users.stream().filter(user -> user.getLogin().equals(name)).findFirst().get();
    }

    public static List<Site> getAllSites(){
        return users.stream().filter(user -> user.getSite() != null).map(User::getSite).collect(Collectors.toList());
    }

    public static boolean isLogin(String name, String pass) { // TODO А может сразу возвращать юзера?
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

    public static Site getSiteByName(String name) throws NoSuchElementException {
        return getUserByName(name).getSite();
    }

    public static Telephony getTelephonyByName(String name) throws NoSuchElementException {
        return getUserByName(name).getTelephony();
    }

    public static String getFreeNumberFromSite(Site site, String googleId, String ip, String pageRequest) throws NoSuchElementException {

        //Проверка не находится ли пользователь в черном списке
        if (site.getBlackListAsList().stream().anyMatch(s -> s.equals(ip))) {
            MyLogger.log(BLOCKED_BY_IP, site.getLogin() + ": пользователь с ip " + ip + " исключен. Выдан стандартный номер.");
            return site.getStandartNumber();
        }

        List<Phone> phones = site.getPhones();
        // проверка: выдан ли номер пользователю по googleID
        for (Phone phone : phones) {
            if (phone.getGoogleId().equals(googleId)) {
                MyLogger.log(REPEATED_REQUEST, site.getLogin() + ": пользователю " + googleId + " уже выдан номер " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }

//         проверка: выдан ли номер пользователю по ip
        for (Phone phone : phones) {
            if (phone.getIp().equals(ip)) {
                MyLogger.log(REPEATED_REQUEST, site.getLogin() + ": пользователю c ip " + ip + " уже выдан номер " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }

        MyLogger.log(REQUEST_NUMBER, site.getLogin() + ": запрос номера googleId: " + googleId);
        for (Phone phone : phones) {
            if (phone.isFree()) {
                phone.setGoogleId(googleId);
                phone.setIp(ip);
                MyLogger.log(SENDING_NUMBER, site.getLogin() + ": новому пользователю выдан номер: " + phone.getNumber());
                phone.extendTime();
                phone.setUtmRequest(pageRequest);
                return phone.getNumber();
            }
        }

        MyLogger.log(NO_NUMBERS_LEFT, site.getLogin() + ": нет свободных номеров.");
        new Mail().checkTimeAndSendEmail(site, "Закончились свободные номера");
        return site.getStandartNumber();
    }

    public static Phone getPhoneByNumber(String number) {
        for (Site site : getAllSites()) {
            List<Phone> phones = site.getPhones();
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
