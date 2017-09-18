package ua.adeptius.asterisk.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.model.telephony.OuterPhone;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;


public class PhoneWatcher extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(PhoneWatcher.class.getSimpleName());
    private static Settings settings = Main.settings;


    public PhoneWatcher() {
        setName("PhoneWatcher");
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                long t0 = System.nanoTime()/1000;
                checkAllPhones();
                long t1 = System.nanoTime()/1000;
                long difference = t1 - t0;
                if (difference > 3000){
                    LOGGER.warn("На контроль всех внешних номеров ушло {} микросекунд.", difference);
                }

                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }


    // тут надо что-то делать. Наверняка в сайте кешировать телефоны, а тут - сайты
//    private static List<OuterPhone> outerPhonesCache;
//
//    private List<OuterPhone> getOuterPhonesCache(){
//        if (outerPhonesCache == null){
////            outerPhonesCache = UserContainer.getUsers().stream()
////                    .flatMap(user -> user.getSites().stream())
////                    .flatMap(site -> site.getUser().getOuterPhones().stream())
////                    .filter(phone -> !phone.isFree())
////                    .collect(Collectors.toList());
//
//            outerPhonesCache = UserContainer.getUsers().stream()
//                    .flatMap(user -> user.getOuterPhones().stream())
//                    .filter(phone -> !phone.isFree())
//                    .collect(Collectors.toList());
//        }
//        return outerPhonesCache;
//    }


    private void checkAllPhones(){
        List<Site> sites = UserContainer.getUsers().stream()
                .flatMap(user -> user.getSites().stream())
                .collect(Collectors.toList());

        for (Site site : sites) {


            List<OuterPhone> outerPhones = site.getOuterPhones();
            for (OuterPhone outerPhone : outerPhones) {
                if (!outerPhone.isFree()){
                    processBusyPhone(site, outerPhone);
                }
            }
        }
    }

    private void processBusyPhone(Site site, OuterPhone phone){
        long currentTime = new GregorianCalendar().getTimeInMillis();
        long phoneTime = phone.getUpdatedTime();

        // продливаем время аренды номера
        long past = currentTime - phoneTime;
        int timeToDeleteOldPhones = Integer.parseInt(Settings.getSetting("SECONDS_TO_REMOVE_OLD_PHONES"))*1000;
        if (past > timeToDeleteOldPhones) {
//            MyLogger.log(NUMBER_FREE, tracking.getLogin() + ": номер " + oldPhone.getNumber() + " освободился. Был занят " + oldPhone.getBusyTimeText());
            phone.markFree();
        }

        if (phone.getUpdatedTime() != 0) {
            // считаем сколько времени номер занят
            past = currentTime - phone.getStartedBusy();
            phone.setBusyTimeMillis(past);
        }

        long timeToBlock = site.getTimeToBlock()*60*1000;

        if (past > timeToBlock){
            try {
                LOGGER.trace("{}: IP {} заблокирован по времени.", site.getLogin(), phone.getIp());
                site.addIpToBlackList(phone.getIp());
                phone.markFree();
            } catch (Exception e) {
                LOGGER.error(site.getLogin() + ": IP " + phone.getIp() + " не удалось заблокировать", e);
            }
        }
    }
}
