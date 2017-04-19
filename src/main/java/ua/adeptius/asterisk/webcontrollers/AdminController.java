package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.utils.CustomerGroup;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
@Controller
@RequestMapping("/admin")
public class AdminController {
    public static final String ADMIN_PASS = "pthy0eds";


    @RequestMapping(value = "/logs", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getLogs(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Wrong password";
        }
        return new Gson().toJson(MyLogger.logs);
    }

    @RequestMapping(value = "/getallcustomers", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getAllNameOfCustomers(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Wrong password";
        }

        ArrayList<CustomerGroup> types = new ArrayList<>();
        for (OldSite customer : MainController.oldSites) {
            types.add(new CustomerGroup(customer.getName(), customer.type));
        }
        for (TelephonyCustomer customer : MainController.telephonyCustomers) {
            types.add(new CustomerGroup(customer.getName(), customer.type));
        }
        return new Gson().toJson(types);
    }

    @RequestMapping(value = "/telephony/add", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String addTelephonyCustomer(@RequestParam String telephonyCustomer, @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Error: Wrong password";
        }

        TelephonyCustomer newCustomer;
        try {
            newCustomer = new Gson().fromJson(telephonyCustomer, TelephonyCustomer.class);
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
            MyLogger.log(LogCategory.DB_OPERATIONS, "Пользователя " + newCustomer.getName() + " В базе нет. Создаём нового.");
        }

        try {
            if (found != null) { // такой пользователь есть. Обновляем.
                newCustomer.updateNumbers();
                newCustomer.setRules(found.getRules());
                MySqlCalltrackDao.editTelephonyCustomer(newCustomer);
                MainController.telephonyCustomers.remove(MainController.getTelephonyCustomerByName(newCustomer.getName()));
                MainController.telephonyCustomers.add(newCustomer);
                MyLogger.log(LogCategory.ELSE, newCustomer.getName() + " изменён");
                RulesConfigDAO.removeFileIfNeeded(newCustomer);
                CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер
                return "Updated";
            } else { // пользователя не существует. Создаём.
                // проверяем нет ли сайта с таким же логином
                OldSite oldSite = null;
                try {
                    oldSite = MainController.getSiteByName(newCustomer.getName());
                } catch (NoSuchElementException ignored) {
                }
                if (oldSite != null) {  // значит есть сайт с тем же логином
                    return "Error: Site with same login already present";
                }

                MySqlCalltrackDao.saveTelephonyCustomer(newCustomer);
                MainController.telephonyCustomers.add(newCustomer);
                MySqlStatisticDao.createOrCleanStatisticsTables();
                newCustomer.updateNumbers();
                MyLogger.log(LogCategory.ELSE, newCustomer.getName() + " добавлен");
                CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер
                return "Added";
            }
        } catch (NotEnoughNumbers e){
            try{
                int freeOuter = PhonesDao.getFreePhones(false).size();
                MyLogger.log(LogCategory.ELSE, "Error: not enough free numbers. Available outer: "+freeOuter);
                return "Error: not enough free numbers. Available outer: "+freeOuter;
            }catch (Exception e2){
                MyLogger.log(LogCategory.ELSE, "Error: not enough free numbers.");
                return "Error: not enough free numbers.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(LogCategory.ELSE, "Error: " + e);
            return "Error: " + e;
        }
    }


    @RequestMapping(value = "/site/add", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getSiteByName(@RequestParam String siteCustomer, @RequestParam String adminPassword) {

        if (isAdminPasswordWrong(adminPassword)) {
            return "Error: Wrong password";
        }

        OldSite newOldSite;
        try {
            newOldSite = new Gson().fromJson(siteCustomer, OldSite.class);
        } catch (Exception e) {
            return "Error: Wrong Syntax";
        }

        Matcher regexMatcher = Pattern.compile("[a-z|A-Z]+").matcher(newOldSite.getName());
        if (!regexMatcher.find()) {
            return "Error: Name must contains only english letters";
        }

        OldSite oldSite = null;
        try {
            oldSite = MainController.getSiteByName(newOldSite.getName());
        } catch (NoSuchElementException e) {
            MyLogger.log(LogCategory.DB_OPERATIONS, "Сайта " + newOldSite.getName() + " В базе нет. Создаём новый.");
        }


        try {
            if (oldSite != null) { // такой сайт есть. Обновляем.
                newOldSite.updateNumbers();
                newOldSite.setRules(oldSite.getRules()); // перекидываем правила со старого на новый
                MySqlCalltrackDao.editSite(newOldSite);
                MainController.oldSites.remove(MainController.getSiteByName(newOldSite.getName()));
                MainController.oldSites.add(newOldSite);
                MyLogger.log(LogCategory.ELSE, newOldSite.getName() + " изменён");
                RulesConfigDAO.removeFileIfNeeded(newOldSite);
                CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер
                return "Updated";
            } else { // сайта не существует. Создаём.

                // проверяем нет ли телефонии с таким же логином
                TelephonyCustomer telephonyCustomer = null;
                try {
                    telephonyCustomer = MainController.getTelephonyCustomerByName(newOldSite.getName());
                } catch (NoSuchElementException ignored) {
                }
                if (telephonyCustomer != null) {  // значит есть пользователь телефонии с тем же именем
                    return "Error: Customer with same login already present";
                }

                MySqlCalltrackDao.saveSite(newOldSite);
                MainController.oldSites.add(newOldSite);
                MySqlStatisticDao.createOrCleanStatisticsTables();
                newOldSite.updateNumbers();
                MyLogger.log(LogCategory.ELSE, newOldSite.getName() + " добавлен");
                CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер
                return "Added";
            }
        } catch (NotEnoughNumbers e){
            try{
                int freeOuter = PhonesDao.getFreePhones(false).size();
                MyLogger.log(LogCategory.ELSE, "Error: not enough free numbers. Available outer: "+freeOuter);
                return "Error: not enough free numbers. Available outer: "+freeOuter;
            }catch (Exception e2){
                MyLogger.log(LogCategory.ELSE, "Error: not enough free numbers.");
                return "Error: not enough free numbers.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(LogCategory.ELSE, "Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }


    @RequestMapping(value = "/userremove", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String removeUser(@RequestParam String name, @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Error: wrong password";
        }

        Customer customer;
        try {
            customer = MainController.getUserByName(name);
        } catch (NoSuchElementException e) {
            MyLogger.log(LogCategory.ELSE, name + " не найден в БД");
            return "Error: user not found in db";
        }

        try {
            if (customer instanceof OldSite) {
                MySqlCalltrackDao.deleteSite(customer.getName());
                MainController.oldSites.remove(customer);
                MySqlStatisticDao.createOrCleanStatisticsTables();
            } else {
                MySqlCalltrackDao.deleteTelephonyCustomer(customer.getName());
                MainController.telephonyCustomers.remove(customer);
                MySqlStatisticDao.createOrCleanStatisticsTables();
            }
            PhonesController.releaseAllCustomerNumbers(customer);
            customer.removeRulesFile();
            MyLogger.log(LogCategory.ELSE, customer.getName() + " удалён");
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
         return "<script src=\"https://"
                 + Settings.getSetting("SERVER_ADDRESS_FOR_SCRIPT")
                 + "/tracking/script/"
                 + name
                 + "\"></script>";

        //TODO сменить адрес
        // Локальный хост
//        return "<script src=\"http://78.159.55.63:8080/tracking/script/" + name + "\"></script>";
    }


    @RequestMapping(value = "/getsetting", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getSetting(@RequestParam String name, @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Error: wrong password";
        }
        return Settings.getSetting(name);
    }


    @RequestMapping(value = "/setsetting", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String setSetting(@RequestParam String name,
                             @RequestParam String value,
                             @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Error: wrong password";
        }
        Settings.setSetting(name, value);
        if (!name.equals("ACTIVE_SITE")) {
            String result = "Success: saved value " + value + " for " + name;
            MyLogger.log(LogCategory.ELSE, result);
            return result;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/getNumbersCount", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getFreeNumbersCount(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Error: wrong password";
        }
        try {
            int freeOuter = PhonesDao.getFreePhones(false).size();
            int freeInner = PhonesDao.getFreePhones(true).size();
            int busyOuter = PhonesDao.getBusyOuterPhones().size();
            int busyInner = PhonesDao.getBusyInnerPhones().size();
            return "{\"freeInner\":"+freeInner+",\"freeOuter\":"+freeOuter+",\"busyInner\":"+busyInner+",\"busyOuter\":"+busyOuter+"}";
        }catch (Exception e){
            return "Error: db error";
        }
    }

    private boolean isAdminPasswordWrong(String password) {
        return !password.equals(ADMIN_PASS);
    }
}
