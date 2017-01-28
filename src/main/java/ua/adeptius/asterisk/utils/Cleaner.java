package ua.adeptius.asterisk.utils;


public class Cleaner extends Thread {

    public Cleaner() {
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(5000);
            } catch (Exception ignored) {}
        }
    }
}
