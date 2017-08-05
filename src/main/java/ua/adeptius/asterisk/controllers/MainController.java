package ua.adeptius.asterisk.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.model.NewCall;
import ua.adeptius.asterisk.senders.AmoCallSender;
import ua.adeptius.asterisk.senders.GoogleAnalitycsCallSender;
import ua.adeptius.asterisk.senders.Mail;
import ua.adeptius.asterisk.senders.RoistatCallSender;

import java.util.*;
import java.util.stream.Collectors;


@SuppressWarnings("Duplicates")
public class MainController {

    private static Logger LOGGER = LoggerFactory.getLogger(MainController.class.getSimpleName());

    private static RoistatCallSender roistatCallSender = new RoistatCallSender();
    public static AmoCallSender amoCallSender = new AmoCallSender();
    private static GoogleAnalitycsCallSender googleAnalitycsCallSender = new GoogleAnalitycsCallSender();

    public static String getFreeNumberFromSite(User user, Site site, String googleId, String ip, String pageRequest) throws NoSuchElementException {
        String login = site.getLogin();
        String standardNumber = site.getStandardNumber();
        String siteName = site.getName();

        //Проверка не находится ли пользователь в черном списке
        if (site.getBlackList().stream().anyMatch(s -> s.equals(ip))) {
            LOGGER.trace("{}: пользователь с ip {} исключен. Выдан стандартный номер.", login, ip);
            return standardNumber;
        }

        //Todo кешировать
        List<OuterPhone> phones = user.getOuterPhones().stream().filter(outerPhone -> siteName.equals(outerPhone.getSitename())).collect(Collectors.toList());

        // проверка: выдан ли номер пользователю по googleID
        for (OuterPhone phone : phones) {
            if (phone.getGoogleId().equals(googleId)) {
                String currentCustomerNumber = phone.getNumber();
                LOGGER.trace("{}: пользователю c ID {} уже выдан номер {}", login, googleId, currentCustomerNumber);
                phone.extendTime();
                return currentCustomerNumber;
            }
        }

//         проверка: выдан ли номер пользователю по ip
        for (OuterPhone phone : phones) {
            if (phone.getIp().equals(ip)) {
                String currentCustomerNumber = phone.getNumber();
                LOGGER.trace("{}: пользователю c IP {} уже выдан номер {}", login, ip, currentCustomerNumber);
                phone.extendTime();
                return currentCustomerNumber;
            }
        }

        LOGGER.debug("{}: запрос номера для нового пользователя ID: {}", login, googleId);
        for (OuterPhone phone : phones) {
            if (phone.isFree()) {
                phone.setGoogleId(googleId);
                String newNumber = phone.getNumber();
                phone.extendTime();
                phone.setIp(ip);
                phone.setUtmRequest(pageRequest);
                LOGGER.debug("{}: новому пользователю выдаётся номер: {}", login, newNumber);
                return newNumber;
            }
        }

        LOGGER.debug("{}: нет свободных номеров. Возвращаю стандартный {}", login, standardNumber);
//        new Mail().checkTimeAndSendEmail(tracking, "Закончились свободные номера");todo email
        return standardNumber;
    }

//    public static OldPhone getPhoneByNumber(String number) {
//        for (Tracking tracking : UserContainer.getAllSites()) {
//            List<OldPhone> oldPhones = tracking.getOldPhones();
//            for (OldPhone oldPhone : oldPhones) {
//                if (number.length() > 1 && oldPhone.getNumber().endsWith(number)) {
//                    return oldPhone;
//                }
//            }
//        }
//        LOGGER.error("Получение телефона по номеру: номер {} не найден", number);
//        throw new RuntimeException("Телефон " + number + " не найден");
//    }

    public static void onNewSiteCall(NewCall call) {
        String numberFirstCall = call.getFirstCall(); // Находим телефон на который позвонили изначально и берём у него utm и googleid
        User user = call.getUser();
        Optional<OuterPhone> first = user.getOuterPhones().stream().filter(OP -> OP.getNumber().equals(numberFirstCall)).findFirst();
        if (first.isPresent()){
            OuterPhone outerPhone = first.get();
            call.setUtm(outerPhone.getUtmRequest());
            call.setGoogleId(outerPhone.getGoogleId());
        }

        googleAnalitycsCallSender.send(call);
        roistatCallSender.send(call);
        MySqlStatisticDao.saveCall(call);
    }

    public static void onNewTelephonyCall(NewCall call) {
        googleAnalitycsCallSender.send(call);
        roistatCallSender.send(call);
        MySqlStatisticDao.saveCall(call);
    }
}
