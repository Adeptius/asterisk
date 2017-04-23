package ua.adeptius.asterisk.webcontrollers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.MySqlCalltrackDao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/userconfig")
public class UserConfigurationController {


    @RequestMapping(value = "/setblocktime", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String addToBlackList(@RequestParam String name, @RequestParam String password, @RequestParam int time) {
//        if (!MainController.isSiteLogin(name, password)) {
//            return "Error: wrong password";
//        }
//
//        try {
//            MySqlCalltrackDao.setTimeToBlock(name, time);
//            MainController.getSiteByName(name).setTimeToBlock(time);
//            return "Success: Time to block set: " + time + " minutes";
//        } catch (Exception e) {
//            return "Error: dataBase Error or no such site";
//        }
        return "";
    }


    @RequestMapping(value = "/addtoblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String addToBlackList(@RequestParam String name, @RequestParam String password, @RequestParam String ip) {
//        if (!MainController.isSiteLogin(name, password)) {
//            return "Error: wrong password";
//        }
//
//        try {
//            Matcher regexMatcher = Pattern.compile("\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}").matcher(ip.trim());
//            regexMatcher.find();
//            regexMatcher.group();
//            MySqlCalltrackDao.addIpToBlackList(name, ip.trim());
//            return "Success: IP " + ip + " blocked.";
//        } catch (Exception e) {
//            return "Error: DB Error or wrong IP";
//        }
        return "";
    }


    @RequestMapping(value = "/removefromblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String removeFromBlackList(@RequestParam String name, @RequestParam String password, @RequestParam String ip) {
//        if (!MainController.isSiteLogin(name, password)) {
//            return "Error: wrong password";
//        }
//
//        try {
//            Matcher regexMatcher = Pattern.compile("\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}[.]\\d{1,3}").matcher(ip);
//            regexMatcher.find();
//            regexMatcher.group();
//            return MySqlCalltrackDao.deleteFromBlackList(name, ip);
//        } catch (Exception e) {
//            return "Error: DB Error or wrong IP";
//        }
        return "";
    }
}
