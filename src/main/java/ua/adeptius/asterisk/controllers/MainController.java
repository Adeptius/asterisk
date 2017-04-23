package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.newmodel.Tracking;
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


    public static String getFreeNumberFromSite(Tracking tracking, String googleId, String ip, String pageRequest) throws NoSuchElementException {

        //Проверка не находится ли пользователь в черном списке
        if (tracking.getBlackList().stream().anyMatch(s -> s.equals(ip))) {
            MyLogger.log(BLOCKED_BY_IP, tracking.getLogin() + ": пользователь с ip " + ip + " исключен. Выдан стандартный номер.");
            return tracking.getStandartNumber();
        }

        List<Phone> phones = tracking.getPhones();
        // проверка: выдан ли номер пользователю по googleID
        for (Phone phone : phones) {
            if (phone.getGoogleId().equals(googleId)) {
                MyLogger.log(REPEATED_REQUEST, tracking.getLogin() + ": пользователю " + googleId + " уже выдан номер " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }

//         проверка: выдан ли номер пользователю по ip
        for (Phone phone : phones) {
            if (phone.getIp().equals(ip)) {
                MyLogger.log(REPEATED_REQUEST, tracking.getLogin() + ": пользователю c ip " + ip + " уже выдан номер " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }

        MyLogger.log(REQUEST_NUMBER, tracking.getLogin() + ": запрос номера googleId: " + googleId);
        for (Phone phone : phones) {
            if (phone.isFree()) {
                phone.setGoogleId(googleId);
                phone.setIp(ip);
                MyLogger.log(SENDING_NUMBER, tracking.getLogin() + ": новому пользователю выдан номер: " + phone.getNumber());
                phone.extendTime();
                phone.setUtmRequest(pageRequest);
                return phone.getNumber();
            }
        }

        MyLogger.log(NO_NUMBERS_LEFT, tracking.getLogin() + ": нет свободных номеров.");
        new Mail().checkTimeAndSendEmail(tracking, "Закончились свободные номера");
        return tracking.getStandartNumber();
    }

    public static Phone getPhoneByNumber(String number) {
        for (Tracking tracking : UserContainer.getAllSites()) {
            List<Phone> phones = tracking.getPhones();
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
