package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.GoogleAnalitycs;
import ua.adeptius.asterisk.utils.Mail;
import ua.adeptius.asterisk.utils.MyLogger;

import java.util.List;
import java.util.NoSuchElementException;

public class MainController {

    public static List<Site> sites;

    public static Site getSiteByName(String name) throws NoSuchElementException{
            return sites.stream().filter(site -> site.getName().equals(name)).findFirst().get();
    }

    public static String getFreeNumberFromSite(Site site, String googleId, String ip) throws NoSuchElementException{

        List<Phone> phones = site.getPhones();

        for (Phone phone : phones) {
            if (phone.getGoogleId().equals(googleId)){
//                MyLogger.log(site.getName()+": пользователю "+googleId+" уже выдан номер " + phone.getNumber(), MainController.class);
                phone.extendTime();
                return phone.getNumber();
            }
        }

//        for (Phone phone : phones) {
//            if (phone.getIp().equals(ip)){
//                MyLogger.log(site.getName()+": пользователю c ip "+ip+" уже выдан номер " + phone.getNumber(), MainController.class);
//                phone.extendTime();
//                return phone.getNumber();
//            }
//        }

//        MyLogger.log(site.getName()+": запрос номера googleId: " + googleId, MainController.class);
        for (Phone phone : phones) {
            if (phone.isFree()){
                phone.setGoogleId(googleId);
                phone.setIp(ip);
                MyLogger.log(site.getName()+": новому пользователю выдан номер: " + phone.getNumber(), MainController.class);
                phone.extendTime();
                return phone.getNumber();
            }
        }


        MyLogger.log(site.getName()+": нет свободных номеров.", MainController.class);
        new Mail().checkTimeAndSendEmail(site, "Закончились свободные номера");

        return site.getStandartNumber();
    }

    public static Site whosePhone(String phone){
        for (Site site : sites) {
            if (site.getPhones().stream().map(Phone::getNumber).anyMatch(s -> s.equals(phone))){
                return site;
            }
        }
        return null;
    }

    public static void onNewCall(String phoneFrom, String phoneTo){
        Site site = whosePhone(phoneTo);
        if (site != null){
            MyLogger.log(site.getName()+": входящий звонок с "+phoneFrom+" на "+phoneTo, MainController.class);
            String googleId = site.getPhones().stream().filter(phone -> phone.getNumber().equals(phoneTo)).findFirst().get().getGoogleId();
            new GoogleAnalitycs(site, googleId ,phoneFrom).start();
        }else {
            MyLogger.log("Не зарегистрировано: входящий звонок с "+phoneFrom+" на "+phoneTo, MainController.class);
        }
    }
}
