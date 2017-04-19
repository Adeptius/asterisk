package ua.adeptius.asterisk.monitor;


import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.newmodel.Site;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.dao.Settings;

import java.util.GregorianCalendar;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.LogCategory.ELSE;
import static ua.adeptius.asterisk.utils.logging.LogCategory.NUMBER_FREE;

public class PhonesWatcher extends Thread {

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
        for (Site site : MainController.getAllSites()) {
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
            MyLogger.log(NUMBER_FREE, site.getLogin() + ": номер " + phone.getNumber() + " освободился. Был занят " + phone.getBusyTimeText());
            phone.markFree();
        }

        if (phone.getUpdatedTime() != 0) {
            // считаем сколько времени номер занят
            past = currentTime - phone.getStartedBusy();
            phone.setBusyTime(past);
        }

        long timeToBlock = site.getTimeToBlock()*60*1000;

        if (past > timeToBlock){
            try {
                MyLogger.log(ELSE, site.getLogin() + ": IP " + phone.getIp() + " заблокирован по времени.");
//                MySqlCalltrackDao.addIpToBlackList(oldSite.getName(), phone.getIp()); //TODO сделать
                phone.markFree();
            } catch (Exception e) {
                MyLogger.log(DB_OPERATIONS, site.getLogin() + ": ошибка добавления " + phone.getIp() + " в БД");
            }
        }
    }
}
