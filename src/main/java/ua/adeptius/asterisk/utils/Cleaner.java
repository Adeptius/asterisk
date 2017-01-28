package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;

import java.util.GregorianCalendar;
import java.util.List;

public class Cleaner extends Thread {

    List<Site> sites = MainController.sites;

    public Cleaner() {
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(5000);
                checkAllPhones();
            } catch (Exception ignored) {}
        }
    }

    private void checkAllPhones(){
        long currentTime = new GregorianCalendar().getTimeInMillis();
        for (Site site : sites) {
            List<Phone> phones = site.getPhones();
            for (Phone phone : phones) {
                long phoneTime = phone.getUpdatedTime();
                if (phoneTime !=0) {
                    long past = currentTime - phoneTime;
                    if (past > 12000) {
                        phone.markFree();
                        MyLogger.log(site.getName()+": номер " + phone.getNumber() + " освободился", Cleaner.class);
                    }
                }
            }
        }
    }
}
