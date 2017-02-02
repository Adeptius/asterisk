package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.LogCategory;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;

import java.util.GregorianCalendar;
import java.util.List;

import static ua.adeptius.asterisk.model.LogCategory.NUMBER_FREE;

public class PhonesWatcher extends Thread {

    List<Site> sites = MainController.sites;

    public PhonesWatcher() {
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(5000);
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
        if (past > 12000) {
            MyLogger.log(NUMBER_FREE, site.getName()+": номер " + phone.getNumber() + " освободился. Был занят " + phone.getBusyTimeText());
            phone.markFree();
        }

        if (phone.getUpdatedTime() != 0) {
            // считаем сколько времени номер занят
            past = currentTime - phone.getStartedBusy();
            phone.setBusyTime(past);
        }
    }
}
