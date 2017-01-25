package com.luxoft.webapplication.utils;


import com.luxoft.webapplication.controllers.DBController;

public class Cleaner extends Thread {

    private DBController dbController;

    public Cleaner(DBController dbController) {
        this.dbController = dbController;
        setDaemon(true);
        start();
    }


    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(5000);
                dbController.removeOld();
            } catch (Exception ignored) {}
        }
    }
}
