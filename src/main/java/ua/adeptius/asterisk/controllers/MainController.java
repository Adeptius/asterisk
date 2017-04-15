package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.TelephonyDao;
import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.webcontrollers.AdminController;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.senders.GoogleAnalitycs;
import ua.adeptius.asterisk.senders.Mail;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.*;

import static ua.adeptius.asterisk.utils.logging.LogCategory.*;

@SuppressWarnings("Duplicates")
public class MainController {

    public static List<Site> sites;
    public static List<TelephonyCustomer> telephonyCustomers;

    public static List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        customers.addAll(sites);
        customers.addAll(telephonyCustomers);
        return customers;
    }

    public static Customer getCustomerByName(String name) throws NoSuchElementException {
        return getAllCustomers().stream().filter(cust -> cust.getName().equals(name)).findFirst().get();
    }

    public static boolean isLogin(String name, String pass) {
        if (pass.equals(AdminController.ADMIN_PASS)) {
            return true;
        }
        Customer customer;
        try {
            customer = getCustomerByName(name);
        } catch (NoSuchElementException e) {
            return false;
        }
        return customer.getPassword().equals(pass);
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

    public static Site getSiteByName(String name) throws NoSuchElementException {
        return sites.stream().filter(site -> site.getName().equals(name)).findFirst().get();
    }

    public static TelephonyCustomer getTelephonyCustomerByName(String name) throws NoSuchElementException {
        return telephonyCustomers.stream().filter(tc -> tc.getName().equals(name)).findFirst().get();
    }

    public static String getFreeNumberFromSite(Site site, String googleId, String ip, String pageRequest) throws NoSuchElementException {

        //Проверка не находится ли пользователь в черном списке
        if (site.getBlackIps().stream().anyMatch(s -> s.equals(ip))) {
            MyLogger.log(BLOCKED_BY_IP, site.getName() + ": пользователь с ip " + ip + " исключен. Выдан стандартный номер.");
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

    public static Phone getPhoneByNumber(String number) {
        for (Site site : sites) {
            List<Phone> phones = site.getPhones();
            for (Phone phone : phones) {
                if (phone.getNumber().equals(number)) {
                    return phone;
                }
            }
        }
        throw new RuntimeException("Телефон " + number + " не найден");
    }

    public static void onNewSiteCall(Call call) {
        Site site = (Site) call.getCustomer();
        Phone phone = getPhoneByNumber(call.getTo());
        String googleId = phone.getGoogleId();
        String request = phone.getUtmRequest() == null ? "" : phone.getUtmRequest();
        new GoogleAnalitycs(site, googleId, call.getFrom()).start();
        Main.sitesDao.saveCall(call, googleId, request);
    }

    public static void onNewTelephonyCall(Call call) {
        TelephonyDao.saveCall(call);
    }
}
