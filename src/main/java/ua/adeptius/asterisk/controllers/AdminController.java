package ua.adeptius.asterisk.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.LogCategory;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.MyLogger;
import ua.adeptius.asterisk.utils.Settings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    public static final String ADMIN_PASS = "pthy0eds";


    @RequestMapping(value = "/logs", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public LinkedList<String> getLogs(@RequestParam String adminPassword) {
         if (isAdminPasswordWrong(adminPassword)){
            LinkedList<String> list = new LinkedList<>();
            list.add("Wrong password");
            return list;
        }
        return MyLogger.logs;
    }

    @RequestMapping(value = "/getallsites", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String[] getAllNameOfSites(
            @RequestParam String password) {
        if (isAdminPasswordWrong(password)){
            return new String[]{"Wrong password"};
        }
        List<String> list = MainController.sites.stream().map(Site::getName).collect(Collectors.toList());
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }


    @RequestMapping(value = "/site/add", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getSiteByName(@RequestParam String name,
                                @RequestParam String phones,
                                @RequestParam String standartNumber,
                                @RequestParam String googleAnalyticsTrackingId,
                                @RequestParam String email,
                                @RequestParam String blackIps,
                                @RequestParam int timeToBlock,
                                @RequestParam String password,
                                @RequestParam String adminPassword) {

        if (isAdminPasswordWrong(adminPassword)){
            return "Wrong password";
        }
        List<Phone> phoneList = new ArrayList<>();
        for (String s : phones.split(",")) {
            phoneList.add(new Phone(s));
        }
        List<String> blackList = new ArrayList<>();
        for (String s : blackIps.split(",")) {
            blackList.add(s);
        }

        Site newSite = new Site(name, phoneList, standartNumber, googleAnalyticsTrackingId, email, blackList, password, timeToBlock);


        Site site = null;
        try {
            site = MainController.getSiteByName(name);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }


        try {
            if (site != null) { // такой сайт есть. Обновляем.
                Main.mySqlDao.editSite(newSite);
                MainController.sites.remove(MainController.getSiteByName(newSite.getName()));
                MainController.sites.add(newSite);
                MyLogger.log(LogCategory.ELSE, newSite.getName() + " изменён");
                return "Updated";
            } else { // сайта не существует. Создаём.
                Main.mySqlDao.saveSite(newSite);
                MainController.sites.add(newSite);
                Main.mySqlDao.createOrCleanStatisticsTables();
                MyLogger.log(LogCategory.ELSE, newSite.getName() + " добавлен");
                return "Added";
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(LogCategory.ELSE, "Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }


    @RequestMapping(value = "/site/remove", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getSiteByName(@RequestParam String name,
                                @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)){
            return "Wrong password";
        }

        Site site = null;
        try {
            site = MainController.getSiteByName(name);
        } catch (NoSuchElementException e) {
            MyLogger.log(LogCategory.ELSE, site.getName() + " не найден в БД");
            return "Not found in db";
        }

        try {
            Main.mySqlDao.deleteSite(site.getName());
            MainController.sites.remove(site);
            Main.mySqlDao.createOrCleanStatisticsTables();
            MyLogger.log(LogCategory.ELSE, site.getName() + " удалён");
            return "Deleted";
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(LogCategory.ELSE, "Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }


    @RequestMapping(value = "/script/{name}", method = RequestMethod.GET, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getScript(@PathVariable String name) {
         return "<script src=\"http://194.44.37.30:8080/tracking/script/" + name + "\"></script>";
    }


    @RequestMapping(value = "/getsetting", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getSetting(@RequestParam String name,
                             @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)){
            return "Wrong password";
        }
        return Settings.getSetting(name);
    }


    @RequestMapping(value = "/setsetting", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String setSetting(@RequestParam String name,
                             @RequestParam String value,
                             @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)){
            return "Wrong password";
        }
        Settings.setSetting(name, value);
        String result = "Сохранено значение " + value + " для " + name;
        MyLogger.log(LogCategory.ELSE, result);
        return result;
    }

    private boolean isAdminPasswordWrong(String password){
        return !password.equals(ADMIN_PASS);
    }

}
