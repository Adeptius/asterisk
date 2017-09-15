package ua.adeptius.asterisk.controllers;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.model.Call;
import ua.adeptius.asterisk.senders.GoogleAnalitycsCallSender;
import ua.adeptius.asterisk.senders.RoistatCallSender;

import java.util.*;

import static ua.adeptius.asterisk.model.Email.EmailType.NO_OUTER_PHONES_LEFT;


public class TrackingController {

    private static Logger LOGGER = LoggerFactory.getLogger(TrackingController.class.getSimpleName());

    private static RoistatCallSender roistatCallSender = new RoistatCallSender();
    private static GoogleAnalitycsCallSender googleAnalitycsCallSender = new GoogleAnalitycsCallSender();
    private static Settings settings = Main.settings;

    public static String getFreeNumberFromSite(User user, Site site, String googleId, String ip, String pageRequest) throws NoSuchElementException {
        String login = site.getLogin();
        String standardNumber = site.getStandardNumber();
        String siteName = site.getName();



        //Проверка не находится ли пользователь в черном списке
        if (site.getBlackList().stream().anyMatch(s -> s.equals(ip))) {
//            LOGGER.trace("{}: пользователь с ip {} исключен. Выдан стандартный номер.", login, ip);
            return standardNumber;
        }


        List<OuterPhone> phones = site.getOuterPhones();

        // проверка: выдан ли номер пользователю по googleID
        for (OuterPhone phone : phones) {
            if (googleId.equals(phone.getGoogleId())) {
                String currentCustomerNumber = phone.getNumber();
//                LOGGER.trace("{}: пользователю c ID {} уже выдан номер {}", login, googleId, currentCustomerNumber); спамит сильно
                phone.extendTime();
                return currentCustomerNumber;
            }
        }

//         проверка: выдан ли номер пользователю по ip
        for (OuterPhone phone : phones) {
            if (ip.equals(phone.getIp())) {
                String currentCustomerNumber = phone.getNumber();
//                LOGGER.trace("{}: пользователю c IP {} уже выдан номер {}", login, ip, currentCustomerNumber);
                phone.extendTime();
                return currentCustomerNumber;
            }
        }

//        LOGGER.debug("{}: запрос номера для нового пользователя ID: {}", login, googleId);
        for (OuterPhone phone : phones) {
            if (phone.isFree()) {
                phone.setGoogleId(googleId);
                String newNumber = phone.getNumber();
                phone.extendTime();
                phone.setIp(ip);
                if (StringUtils.isBlank(pageRequest)){
                    phone.setUtmRequest(null);
                }else {
                    phone.setUtmRequest(pageRequest);
                }
//                LOGGER.debug("{}: новому пользователю выдаётся номер: {}", login, newNumber);
                return newNumber;
            }
        }

//        LOGGER.debug("{}: нет свободных номеров. Возвращаю стандартный {}", login, standardNumber);
        if (site.didEnoughTimePassFromLastEmail()){
            Email email = new Email(NO_OUTER_PHONES_LEFT, user.getEmail(), siteName, login);
            Main.emailSender.send(email);
        }
        return standardNumber;
    }

    public static void onNewCall(Call call) {
        LOGGER.debug("{}: отправляем звонок во все системы {}", call.getUser().getLogin(), call);
        if (settings.isRemoteServerIsUp()){
            LOGGER.info("Звонок не отправляется - запущен удалённый сервер");
            return;
        }

        OuterPhone outerPhone = call.getOuterPhone();
        if (outerPhone != null){
            call.setUtm(outerPhone.getUtmRequest());
            call.setGoogleId(outerPhone.getGoogleId());
        }

        googleAnalitycsCallSender.send(call);
        roistatCallSender.send(call);
        MySqlStatisticDao.saveCall(call);
    }
}
