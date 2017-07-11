package ua.adeptius.amocrm.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.model.TimePairCookie;

import java.util.*;

public class AmoCookieCleaner extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoCookieCleaner.class.getSimpleName());

    public AmoCookieCleaner() {
        setDaemon(true);
        LOGGER.info("Чистильщик Cookie AmoCRM запущен");
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(1000 * 50);// 50 сек
                cleanCookie();
            } catch (Exception e) {
                LOGGER.error("Ошибка очистки cookie", e);
            }
        }
    }

    private void cleanCookie() {
        List<String> cookieToRemove = new ArrayList<>();
        long currentTime = new GregorianCalendar().getTimeInMillis();

        for (Map.Entry<String, TimePairCookie> entry : AmoDAO.cookiesRepo.entrySet()) {
            if ((currentTime - entry.getValue().getTimeCreated()) > 14*60*1000 ){ // если прошло 14 минут
                LOGGER.trace("Cookie пользователя {} устарел", entry.getKey());
                cookieToRemove.add(entry.getKey());
            }
        }

        for (String s : cookieToRemove) {
            AmoDAO.cookiesRepo.remove(s);
        }
    }
}
