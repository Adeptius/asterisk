package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.model.Site;

import java.util.List;
import java.util.NoSuchElementException;

public class MainController {

    public static List<Site> sites;

    public static Site getSiteByName(String name) throws NoSuchElementException{
            return sites.stream().filter(site -> site.getName().equals(name)).findFirst().get();
    }

    public static String getFreeNumberFromSite(Site site) throws NoSuchElementException{
        return site.getPhones().stream().filter(phone -> !phone.isBusy()).findFirst().get().getNumber();
    }

    public  static String getFreeNumberFromSite(String name) throws NoSuchElementException{
        return getFreeNumberFromSite(getSiteByName(name));
    }



}
