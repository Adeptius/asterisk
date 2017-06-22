package ua.adeptius.asterisk.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DailyCleaner extends Thread {

    private static Logger LOGGER =  LoggerFactory.getLogger(DailyCleaner.class.getSimpleName());


    public DailyCleaner() {
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()){
            try {
                Thread.sleep(1000 * 60 * 60);// час
                Calendar calendar = new GregorianCalendar();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour == 4){
                    LOGGER.debug("Запуск ночной очистки");
                    startClean();
                }
            } catch (Exception e) {
                LOGGER.error("Ошибка очистки", e);
            }
        }
    }

    private void startClean(){
        LOGGER.trace("Очистка карты number <-> Call");
        CallProcessor.calls.clear();
    }
}
