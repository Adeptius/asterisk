package ua.adeptius.asterisk.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.Site;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/userconfig")
public class UserConfigurationController {


    @RequestMapping(value = "/addtoblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String addToBlackList(@RequestParam String name,
                                 @RequestParam String password,
                                 @RequestParam String ip,
                                 HttpServletResponse response,
                                 HttpServletRequest request) {
        String accessControlAllowOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
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
            Site site = MainController.getSiteByName(name);
            site.getBlackIps().add(ip);
            return "IP " + ip + " заблокирован.";
        } catch (Exception e) {
            return "Ошибка БД или неправильный IP";
        }
    }

        @RequestMapping(value = "/removefromblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
        @ResponseBody
        public String removeFromBlackList (@RequestParam String name,
                @RequestParam String password,
                @RequestParam String ip,
                HttpServletResponse response,
                HttpServletRequest request){
            String accessControlAllowOrigin = request.getHeader("Origin");
            response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
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
                @RequestParam String password,
                HttpServletResponse response,
                HttpServletRequest request){
            String accessControlAllowOrigin = request.getHeader("Origin");
            response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);


            try {
                Site site = MainController.getSiteByName(login);
                String passInDB = site.getPassword();
                if (passInDB.equals(password)) {
                    return "true";
                }
            } catch (NoSuchElementException ignored) {
            }
            return "false";
        }

    private static boolean isPasswordWrong(String sitename, String password) {
        String currentSitePass = MainController.getSiteByName(sitename).getPassword();
        if (password.equals(currentSitePass)) {
            return false;
        }
        if (password.equals(AdminController.ADMIN_PASS)) {
            return false;
        }
        return true;
    }


}
