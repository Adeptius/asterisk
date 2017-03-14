package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.LogCategory;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;

import java.util.GregorianCalendar;
import java.util.List;

import static ua.adeptius.asterisk.model.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.model.LogCategory.ELSE;
import static ua.adeptius.asterisk.model.LogCategory.NUMBER_FREE;

public class PhonesWatcher extends Thread {

    List<Site> sites = MainController.sites;

    public PhonesWatcher() {
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()){
            try {
                Thread.sleep(3000);
                checkAllPhones();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkAllPhones(){
        for (Site site : sites) {
            List<Phone> phones = site.getPhones();
            for (Phone phone : phones) {
                if (!phone.isFree()) { // если телефон не простаивает
                    processBusyPhone(site, phone);
                }
            }
        }
    }

    private void processBusyPhone(Site site, Phone phone){
        long currentTime = new GregorianCalendar().getTimeInMillis();
        long phoneTime = phone.getUpdatedTime();

        // продливаем время аренды номера
        long past = currentTime - phoneTime;
        int timeToDeleteOldPhones = Integer.parseInt(Settings.getSetting("SECONDS_TO_REMOVE_OLD_PHONES"))*1000;
        if (past > timeToDeleteOldPhones) {
            MyLogger.log(NUMBER_FREE, site.getName()+": номер " + phone.getNumber() + " освободился. Был занят " + phone.getBusyTimeText());
            phone.markFree();
        }

        if (phone.getUpdatedTime() != 0) {
            // считаем сколько времени номер занят
            past = currentTime - phone.getStartedBusy();
            phone.setBusyTime(past);
        }

        long timeToBlock = site.getTimeToBlock()*60*1000;

        if (past > timeToBlock){
//            site.getBlackIps().add(phone.getIp());
            try {
                MyLogger.log(ELSE, site.getName() + ": IP " + phone.getIp() + " заблокирован по времени.");
                Main.mySqlDao.addIpToBlackList(site.getName(), phone.getIp());
                phone.markFree();
            } catch (Exception e) {
                MyLogger.log(DB_OPERATIONS, site.getName() + ": ошибка добавления " + phone.getIp() + " в БД");
            }
        }
    }
}
