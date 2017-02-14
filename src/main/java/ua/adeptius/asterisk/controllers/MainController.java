package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.LogCategory;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Statistic;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.GoogleAnalitycs;
import ua.adeptius.asterisk.utils.Mail;
import ua.adeptius.asterisk.utils.MyLogger;
import ua.adeptius.asterisk.utils.Utils;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static ua.adeptius.asterisk.model.LogCategory.*;

public class MainController {

    public static List<Site> sites;

    public static Site getSiteByName(String name) throws NoSuchElementException {
        return sites.stream().filter(site -> site.getName().equals(name)).findFirst().get();
    }

    public static String getFreeNumberFromSite(Site site, String googleId, String ip, String pageRequest) throws NoSuchElementException {

        //Проверка не находится ли пользователь в черном списке
        if (site.getBlackIps().stream().anyMatch(s -> s.equals(ip))){
            MyLogger.log(BLOCKED_BY_IP, site.getName()+": пользователь с ip " + ip + " исключен. Выдан стандартный номер.");
            return site.getStandartNumber();
        }

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

        MyLogger.log(REQUEST_NUMBER, site.getName() + ": запрос номера googleId: " + googleId);
        for (Phone phone : phones) {
            if (phone.isFree()) {
                phone.setGoogleId(googleId);
                phone.setIp(ip);
                MyLogger.log(SENDING_NUMBER, site.getName() + ": новому пользователю выдан номер: " + phone.getNumber());
                phone.extendTime();
                phone.setUtmRequest(pageRequest);
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

    public static Phone getPhoneByNumber(String number){
        for (Site site : sites) {
            List<Phone> phones = site.getPhones();
            for (Phone phone : phones) {
                if (phone.getNumber().equals(number)){
                    return phone;
                }
            }
        }
        throw new RuntimeException("Телефон " + number + " не найден");
    }

    private static HashMap<String, Statistic> phonesTime = new HashMap<>();

    public static void onNewCall(LogCategory category, String phoneFrom, String phoneTo, String callUniqueId) {
        Site site = whosePhone(phoneTo);
        if (site != null) {
            if (category == INCOMING_CALL) {
                String googleId = site.getPhones().stream().filter(phone -> phone.getNumber().equals(phoneTo)).findFirst().get().getGoogleId();
                MyLogger.log(INCOMING_CALL, site.getName()+": входящий звонок c "+phoneFrom+" на " + phoneTo);
                new GoogleAnalitycs(site, googleId, phoneFrom).start();

                Statistic statistic = new Statistic();
                statistic.setFrom(phoneFrom);
                statistic.setTo(phoneTo);
                statistic.setCalled(new GregorianCalendar().getTimeInMillis());
                statistic.setSite(site);
                phonesTime.put(phoneFrom, statistic);



            } else if (category == ANSWER_CALL) {
                MyLogger.log(ANSWER_CALL, site.getName()+": "+phoneTo+" ответил на звонок " + phoneFrom);
                try{
                    Statistic statistic = phonesTime.get(phoneFrom);
                    statistic.setAnswered(new GregorianCalendar().getTimeInMillis());
                }catch (NullPointerException ignored){
                    // Тут вылетит ошибка только если в момент запуска сервера уже были активные звонки
                }


            } else if (category == ENDED_CALL) {
                MyLogger.log(ENDED_CALL, site.getName()+": "+phoneTo+" закончил разговор " + phoneFrom);

                try{
                    Phone phone = getPhoneByNumber(phoneTo);
                    String googleId = phone.getGoogleId();
                    String request = phone.getUtmRequest();
                    Statistic statistic = phonesTime.get(phoneFrom);
                    statistic.setEnded(new GregorianCalendar().getTimeInMillis());
                    statistic.setGoogleId(googleId);
                    statistic.setRequest(request);
                    statistic.setCallUniqueId(callUniqueId);

                    String report = statistic.getSite().getName()
                            + ": Закончен разговор "
                            + statistic.getFrom()
                            + " с "
                            + statistic.getTo()
                            + " ответил за: "
                            + statistic.getTimeToAnswer()
                            + ", время разговора: "
                            + statistic.getSpeakTime();
                    MyLogger.log(PHONE_TIME_REPORT, report);
                    Main.mySqlDao.saveStatisticToTable(site, statistic);

                }catch (NullPointerException e){
                    e.printStackTrace();
                    // Тут вылетит ошибка только если в момент запуска сервера уже были активные звонки
                }

            }
        } else if (category == INCOMING_CALL){
            MyLogger.log(INCOMING_CALL_NOT_REGISTER, "Не зарегистрировано: входящий звонок с " + phoneFrom + " на " + phoneTo);
        }
    }
}
