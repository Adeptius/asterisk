package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.model.LogCategory;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.GoogleAnalitycs;
import ua.adeptius.asterisk.utils.Mail;
import ua.adeptius.asterisk.utils.MyLogger;

import java.util.List;
import java.util.NoSuchElementException;

import static ua.adeptius.asterisk.model.LogCategory.*;

public class MainController {

    public static List<Site> sites;

    public static Site getSiteByName(String name) throws NoSuchElementException {
        return sites.stream().filter(site -> site.getName().equals(name)).findFirst().get();
    }

    public static String getFreeNumberFromSite(Site site, String googleId, String ip) throws NoSuchElementException {
        MyLogger.log(REQUEST_NUMBER, site.getName() + ": запрос номера googleId: " + googleId);

        //Проверка не находится ли пользователь в черном списке
//        if (site.getBlackIps().stream().anyMatch(s -> s.equals(ip))){
//            MyLogger.log(BLOCKED_BY_IP, site.getName()+": пользователь с ip " + ip + " исключен. Выдан стандартный номер.");
//            return site.getStandartNumber();
//        }

        List<Phone> phones = site.getPhones();
        // проверка: выдан ли номер пользователю по googleID
        for (Phone phone : phones) {
            if (phone.getGoogleId().equals(googleId)) {
                MyLogger.log(REPEATED_REQUEST, site.getName() + ": пользователю " + googleId + " уже выдан номер " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }

//         проверка: выдан ли номер пользователю по ip
        for (Phone phone : phones) {
            if (phone.getIp().equals(ip)) {
                MyLogger.log(REPEATED_REQUEST, site.getName() + ": пользователю c ip " + ip + " уже выдан номер " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }

        for (Phone phone : phones) {
            if (phone.isFree()) {
                phone.setGoogleId(googleId);
                phone.setIp(ip);
                MyLogger.log(SENDING_NUMBER, site.getName() + ": новому пользователю выдан номер: " + phone.getNumber());
                phone.extendTime();
                return phone.getNumber();
            }
        }


        MyLogger.log(NO_NUMBERS_LEFT, site.getName() + ": нет свободных номеров.");
        new Mail().checkTimeAndSendEmail(site, "Закончились свободные номера");

        return site.getStandartNumber();
    }

    public static Site whosePhone(String phone) {
        for (Site site : sites) {
            if (site.getPhones().stream().map(Phone::getNumber).anyMatch(s -> s.equals(phone))) {
                return site;
            }
        }
        return null;
    }

    public static void onNewCall(LogCategory category, String phoneFrom, String phoneTo) {
        Site site = whosePhone(phoneTo);
        if (site != null) {
            if (category == INCOMING_CALL) {
                String googleId = site.getPhones().stream().filter(phone -> phone.getNumber().equals(phoneTo)).findFirst().get().getGoogleId();
                new GoogleAnalitycs(site, googleId, phoneFrom).start();
            } else if (category == ANSWER_CALL) {
                MyLogger.log(ANSWER_CALL, site.getName()+": ответ на звонок " + phoneFrom);

            } else if (category == ENDED_CALL) {
                MyLogger.log(ANSWER_CALL, site.getName()+": разговор окончен " + phoneFrom);
            }
        } else {
            MyLogger.log(INCOMING_CALL_NOT_REGISTER, "Не зарегистрировано: входящий звонок с " + phoneFrom + " на " + phoneTo);
        }
    }
}
