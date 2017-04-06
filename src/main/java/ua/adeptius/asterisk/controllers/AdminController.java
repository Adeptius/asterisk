package ua.adeptius.asterisk.controllers;


import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.CustomerType;
import ua.adeptius.asterisk.model.TelephonyCustomer;
import ua.adeptius.asterisk.utils.CustomerGroup;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.tracking.MainController;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.dao.Settings;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    public static final String ADMIN_PASS = "pthy0eds";


    @RequestMapping(value = "/logs", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public LinkedList<String> getLogs(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            LinkedList<String> list = new LinkedList<>();
            list.add("Wrong password");
            return list;
        }
        return MyLogger.logs;
    }

    @RequestMapping(value = "/getallcustomers", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getAllNameOfCustomers(@RequestParam String password) {
        if (isAdminPasswordWrong(password)) {
            return "Wrong password";
        }

        ArrayList<CustomerGroup> types = new ArrayList<>();
        for (Site customer : MainController.sites) {
            types.add(new CustomerGroup(customer.getName(), customer.type));
        }
        for (TelephonyCustomer customer : MainController.telephonyCustomers) {
            types.add(new CustomerGroup(customer.getName(), customer.type));
        }
        return new Gson().toJson(types);
    }

    @RequestMapping(value = "/telephony/add", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String addTelephonyCustomer(@RequestParam String customer, @RequestParam String adminPassword) {

        if (isAdminPasswordWrong(adminPassword)) {
            return "Error: Wrong password";
        }

        TelephonyCustomer newCustomer = null;
        try {
            newCustomer = new Gson().fromJson(customer, TelephonyCustomer.class);
        } catch (Exception e) {
            return "Error: Wrong Syntax";
        }

        Matcher regexMatcher = Pattern.compile("[a-z|A-Z]+").matcher(newCustomer.getName());
        if (!regexMatcher.find()) {
            return "Error: Name must contains only english letters";
        }

        TelephonyCustomer found = null;
        try {
            found = MainController.getTelephonyCustomerByName(newCustomer.getName());
        } catch (NoSuchElementException e) {
            MyLogger.log(LogCategory.DB_OPERATIONS, "Пользователя " + newCustomer + " В базе нет. Создаём нового.");
        }


        try {
            if (found != null) { // такой пользователь есть. Обновляем.
                Main.telephonyDao.editTelephonyCustomer(newCustomer);
                MainController.telephonyCustomers.remove(MainController.getTelephonyCustomerByName(newCustomer.getName()));
                MainController.telephonyCustomers.add(newCustomer);
                MyLogger.log(LogCategory.ELSE, newCustomer.getName() + " изменён");
                return "Updated";
            } else { // пользователя не существует. Создаём.

                // проверяем нет ли сайта с таким же логином
                Site site = null;
                try {
                    site = MainController.getSiteByName(newCustomer.getName());
                } catch (NoSuchElementException ignored) {
                }
                if (site != null) {  // значит есть сайт с тем же логином
                    return "Error: Site with same login already present";
                }

                Main.telephonyDao.saveTelephonyCustomer(newCustomer);
                MainController.telephonyCustomers.add(newCustomer);
                // TODO создать таблицу статистики
//                Main.sitesDao.createOrCleanStatisticsTables();
                MyLogger.log(LogCategory.ELSE, newCustomer.getName() + " добавлен");
                return "Added";
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(LogCategory.ELSE, "Error: " + e);
            return "Error: " + e;
        }
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

        if (isAdminPasswordWrong(adminPassword)) {
            return "Error: Wrong password";
        }

        Matcher regexMatcher = Pattern.compile("[a-z|A-Z]+").matcher(name);
        if (!regexMatcher.find()) {
            return "Error: Name must contains only english letters";
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
            MyLogger.log(LogCategory.DB_OPERATIONS, "Сайта " + name + " В базе нет. Создаём новый.");
        }


        try {
            if (site != null) { // такой сайт есть. Обновляем.
                Main.sitesDao.editSite(newSite);
                MainController.sites.remove(MainController.getSiteByName(newSite.getName()));
                MainController.sites.add(newSite);
                MyLogger.log(LogCategory.ELSE, newSite.getName() + " изменён");
                return "Updated";
            } else { // сайта не существует. Создаём.

                // проверяем нет ли телефонии с таким же логином
                TelephonyCustomer telephonyCustomer = null;
                try {
                    telephonyCustomer = MainController.getTelephonyCustomerByName(newSite.getName());
                } catch (NoSuchElementException ignored) {
                }
                if (telephonyCustomer != null) {  // значит есть пользователь телефонии с тем же именем
                    return "Error: Customer with same login already present";
                }

                Main.sitesDao.saveSite(newSite);
                MainController.sites.add(newSite);
                Main.sitesDao.createOrCleanStatisticsTables();
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
    public String removeSite(@RequestParam String name, @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Wrong password";
        }

        Site site = null;
        try {
            site = MainController.getSiteByName(name);
        } catch (NoSuchElementException e) {
            MyLogger.log(LogCategory.ELSE, name + " не найден в БД");
            return "Not found in db";
        }

        try {
            Main.sitesDao.deleteSite(site.getName());
            MainController.sites.remove(site);
            Main.sitesDao.createOrCleanStatisticsTables();
            MyLogger.log(LogCategory.ELSE, site.getName() + " удалён");
            return "Deleted";
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(LogCategory.ELSE, "Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @RequestMapping(value = "/telephony/remove", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String removeTelephony(@RequestParam String name, @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Wrong password";
        }

        TelephonyCustomer customer = null;
        try {
            customer = MainController.getTelephonyCustomerByName(name);
        } catch (NoSuchElementException e) {
            MyLogger.log(LogCategory.ELSE, name + " не найден в БД");
            return "Not found in db";
        }

        try {
            Main.telephonyDao.deleteTelephonyCustomer(customer.getName());
            MainController.telephonyCustomers.remove(customer);
            // TODO почистить таблицы
//            Main.sitesDao.createOrCleanStatisticsTables();
            MyLogger.log(LogCategory.ELSE, customer.getName() + " удалён");
            return "Deleted";
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(LogCategory.ELSE, "Error: " + e);
            return "Error: " + e;
        }
    }


    @RequestMapping(value = "/script/{name}", method = RequestMethod.GET, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getScript(@PathVariable String name) {
//         return "<script src=\"https://"
//                 + Settings.getSetting("SERVER_ADDRESS_FOR_SCRIPT")
//                 + "/tracking/script/"
//                 + name
//                 + "\"></script>";

        // Локальный хост
        return "<script src=\"http://78.159.55.63:8080/tracking/script/" + name + "\"></script>";
    }


    @RequestMapping(value = "/getsetting", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getSetting(@RequestParam String name,
                             @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Wrong password";
        }
        return Settings.getSetting(name);
    }


    @RequestMapping(value = "/setsetting", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String setSetting(@RequestParam String name,
                             @RequestParam String value,
                             @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Wrong password";
        }
        Settings.setSetting(name, value);
        if (!name.equals("ACTIVE_SITE")) {
            String result = "Сохранено значение " + value + " для " + name;
            MyLogger.log(LogCategory.ELSE, result);
            return result;
        } else {
            return null;
        }
    }

    private boolean isAdminPasswordWrong(String password) {
        return !password.equals(ADMIN_PASS);
    }
}
