package ua.adeptius.asterisk.monitor;


import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.MySqlCalltrackDao;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.OldSite;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.dao.Settings;

import java.util.GregorianCalendar;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.LogCategory.ELSE;
import static ua.adeptius.asterisk.utils.logging.LogCategory.NUMBER_FREE;

public class PhonesWatcher extends Thread {

    List<OldSite> oldSites = MainController.oldSites;

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
        for (OldSite oldSite : oldSites) {
            List<Phone> phones = oldSite.getPhones();
            for (Phone phone : phones) {
                if (!phone.isFree()) { // если телефон не простаивает
                    processBusyPhone(oldSite, phone);
                }
            }
        }
    }

    private void processBusyPhone(OldSite oldSite, Phone phone){
        long currentTime = new GregorianCalendar().getTimeInMillis();
        long phoneTime = phone.getUpdatedTime();

        // продливаем время аренды номера
        long past = currentTime - phoneTime;
        int timeToDeleteOldPhones = Integer.parseInt(Settings.getSetting("SECONDS_TO_REMOVE_OLD_PHONES"))*1000;
        if (past > timeToDeleteOldPhones) {
            MyLogger.log(NUMBER_FREE, oldSite.getName()+": номер " + phone.getNumber() + " освободился. Был занят " + phone.getBusyTimeText());
            phone.markFree();
        }

        if (phone.getUpdatedTime() != 0) {
            // считаем сколько времени номер занят
            past = currentTime - phone.getStartedBusy();
            phone.setBusyTime(past);
        }

        long timeToBlock = oldSite.getTimeToBlock()*60*1000;

        if (past > timeToBlock){
//            site.getBlackIps().add(phone.getIp());
            try {
                MyLogger.log(ELSE, oldSite.getName() + ": IP " + phone.getIp() + " заблокирован по времени.");
                MySqlCalltrackDao.addIpToBlackList(oldSite.getName(), phone.getIp());
                phone.markFree();
            } catch (Exception e) {
                MyLogger.log(DB_OPERATIONS, oldSite.getName() + ": ошибка добавления " + phone.getIp() + " в БД");
            }
        }
    }
}
