package ua.adeptius.asterisk.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.json.RoistatPhoneCall;
import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.senders.GoogleAnalitycsCallSender;
import ua.adeptius.asterisk.senders.Mail;
import ua.adeptius.asterisk.senders.RoistatCallSender;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.*;

import static ua.adeptius.asterisk.utils.logging.LogCategory.*;

@SuppressWarnings("Duplicates")
public class MainController {

    private static Logger LOGGER = LoggerFactory.getLogger(MainController.class.getSimpleName());

    private static RoistatCallSender roistatCallSender = new RoistatCallSender();
    private static GoogleAnalitycsCallSender googleAnalitycsCallSender = new GoogleAnalitycsCallSender();

    public static String getFreeNumberFromSite(Tracking tracking, String googleId, String ip, String pageRequest) throws NoSuchElementException {
        String login = tracking.getLogin();

        //Проверка не находится ли пользователь в черном списке
        if (tracking.getBlackList().stream().anyMatch(s -> s.equals(ip))) {
            MyLogger.log(BLOCKED_BY_IP, login + ": пользователь с ip " + ip + " исключен. Выдан стандартный номер.");
            LOGGER.trace("{}: пользователь с ip {} исключен. Выдан стандартный номер.", login, ip);
            return tracking.getStandartNumber();
        }

        List<Phone> phones = tracking.getPhones();
        // проверка: выдан ли номер пользователю по googleID
        for (Phone phone : phones) {
            if (phone.getGoogleId().equals(googleId)) {
                String currentCustomerNumber = phone.getNumber();
                MyLogger.log(REPEATED_REQUEST, login + ": пользователю " + googleId + " уже выдан номер " + currentCustomerNumber);
                LOGGER.trace("{}: пользователю c ID {} уже выдан номер {}", login, googleId, currentCustomerNumber);
                phone.extendTime();
                return currentCustomerNumber;
            }
        }

//         проверка: выдан ли номер пользователю по ip
        for (Phone phone : phones) {
            if (phone.getIp().equals(ip)) {
                String currentCustomerNumber = phone.getNumber();
                MyLogger.log(REPEATED_REQUEST, login + ": пользователю c ip " + ip + " уже выдан номер " + currentCustomerNumber);
                LOGGER.trace("{}: пользователю c IP {} уже выдан номер {}", login, ip, currentCustomerNumber);
                phone.extendTime();
                return currentCustomerNumber;
            }
        }

        MyLogger.log(REQUEST_NUMBER, login + ": запрос номера googleId: " + googleId);
        LOGGER.debug("{}: запрос номера для нового пользователя ID: {}", login, googleId);
        for (Phone phone : phones) {
            if (phone.isFree()) {
                phone.setGoogleId(googleId);
                phone.setIp(ip);
                String newNumber = phone.getNumber();
                MyLogger.log(SENDING_NUMBER, login + ": новому пользователю выдан номер: " + newNumber);
                LOGGER.debug("{}: новому пользователю выдаётся номер: {}", login, newNumber);
                phone.extendTime();
                phone.setUtmRequest(pageRequest);
                return newNumber;
            }
        }

        String standartNumber = tracking.getStandartNumber();
        LOGGER.debug("{}: нет свободных номеров. Возвращаю стандартный {}", login, standartNumber);
        MyLogger.log(NO_NUMBERS_LEFT, login + ": нет свободных номеров.");
        new Mail().checkTimeAndSendEmail(tracking, "Закончились свободные номера");
        return standartNumber;
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
        LOGGER.error("Получение телефона по номеру: номер {} не найден", number);
        MyLogger.log(DB_OPERATIONS, "Телефон " + number + " не найден");
        throw new RuntimeException("Телефон " + number + " не найден");
    }

    public static void onNewSiteCall(Call call) {
        Phone phone = getPhoneByNumber(call.getFirstCall());
        call.setUtm(phone.getUtmRequest());
        call.setGoogleId(phone.getGoogleId());

        googleAnalitycsCallSender.send(call);
        roistatCallSender.send(call);
        MySqlStatisticDao.saveCall(call);
    }

    public static void onNewTelephonyCall(Call call) {
        googleAnalitycsCallSender.send(call);
        roistatCallSender.send(call);
        MySqlStatisticDao.saveCall(call);
    }
}
