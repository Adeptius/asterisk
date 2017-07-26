package ua.adeptius.asterisk.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.RulesConfigDAO;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ScenarioWriter extends Thread {

    private static Logger LOGGER =  LoggerFactory.getLogger(ScenarioWriter.class.getSimpleName());

    public ScenarioWriter() {
        setDaemon(true);
        setName("ScenarioWriter");
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()){
            try {
                checkTime();
                Thread.sleep(5 * 1000 * 60); // 5 минут
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int lastHourOfWorks = 9999;

    private void checkTime(){
        Calendar calendar = new GregorianCalendar();
        int minutes = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

//        if (minutes <= 50){ // если до нового часа более 10 минут
//            LOGGER.trace("Конфиги не пишем: до нового часа более 10 минут."); // TODO раскомитить для продакшена
//            return;
//        }

        if (hour == lastHourOfWorks){ // в этом часу уже писали конфиги
            LOGGER.trace("Конфиги не пишем: в этом часу уже конфиги писались.");
            return;
        }

        lastHourOfWorks = hour;
        generateConfig();
    }

    private void generateConfig(){
        LOGGER.trace("Начинается запись всех конфигов астериска в файлы.");
        RulesConfigDAO.writeAllNeededScenarios();
    }
}
