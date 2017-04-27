package ua.adeptius.asterisk.monitor;


import java.util.Calendar;
import java.util.GregorianCalendar;

public class DailyCleaner extends Thread {

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
                    startClean();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startClean(){
        CallProcessor.calls.clear();
    }
}
