package ua.adeptius.asterisk.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.tracking.TrackingController;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/userconfig")
public class UserConfigurationController {


    @RequestMapping(value = "/setblocktime", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String addToBlackList(@RequestParam String name,
                                 @RequestParam String password,
                                 @RequestParam int time) {
        if (isPasswordWrong(name, password)) {
            return "Wrong password";
        }
        try {
            Main.mySqlDao.setTimeToBlock(name, time);
            TrackingController.getSiteByName(name).setTimeToBlock(time);
            return "Задано время автоматической блокировки: " + time + " минут";
        } catch (Exception e) {
            return "Ошибка";
        }
    }


    @RequestMapping(value = "/addtoblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String addToBlackList(@RequestParam String name,
                                 @RequestParam String password,
                                 @RequestParam String ip) {
        if (isPasswordWrong(name, password)) {
            return "Wrong password";
        }
        if ("".equals(ip)) {
            return "IP пуст";
        }
        try {
            Matcher regexMatcher = Pattern.compile("\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}").matcher(ip);
            regexMatcher.find();
            regexMatcher.group();
            Main.mySqlDao.addIpToBlackList(name, ip);
            return "IP " + ip + " заблокирован.";
        } catch (Exception e) {
            return "Ошибка БД или неправильный IP";
        }
    }

        @RequestMapping(value = "/removefromblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
        @ResponseBody
        public String removeFromBlackList (@RequestParam String name,
                @RequestParam String password,
                @RequestParam String ip){
            if (isPasswordWrong(name, password)) {
                return "Wrong password";
            }
            if ("".equals(ip)) {
                return "IP пуст";
            }
            try {
                Matcher regexMatcher = Pattern.compile("\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}").matcher(ip);
                regexMatcher.find();
                regexMatcher.group();
                String result = Main.mySqlDao.deleteFromBlackList(name, ip);
                return result;
            } catch (Exception e) {
                return "Ошибка удаления";
            }
        }


        @RequestMapping(value = "/checklogin", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
        @ResponseBody
        public String checkLogin (@RequestParam String login,
                @RequestParam String password){

            try {
                Site site = TrackingController.getSiteByName(login);
                String passInDB = site.getPassword();
                if (passInDB.equals(password)) {
                    return "true";
                }
            } catch (NoSuchElementException ignored) {
            }
            return "false";
        }

    private static boolean isPasswordWrong(String sitename, String password) {
        String currentSitePass = TrackingController.getSiteByName(sitename).getPassword();
        if (password.equals(currentSitePass)) {
            return false;
        }
        if (password.equals(AdminController.ADMIN_PASS)) {
            return false;
        }
        return true;
    }


}
