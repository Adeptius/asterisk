package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AdminController {
    public static final String ADMIN_PASS = "pthy0eds";


    @RequestMapping(value = "/logs", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getLogs(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }
        return new Gson().toJson(MyLogger.logs);
    }


    @RequestMapping(value = "/getAllUsers", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getAllNameOfCustomers(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }
        return new Gson().toJson(UserContainer.getUsers().stream().map(User::getLogin).collect(Collectors.toList()));
    }

    @RequestMapping(value = "/getTokens", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getHash(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }
        HashMap<String, String> hashMap = new HashMap<>();

        for (Map.Entry<String, User> entry : UserContainer.getHashes().entrySet()) {
            hashMap.put(entry.getValue().getLogin(), entry.getKey());
        }
        return new Gson().toJson(hashMap);
    }


    @RequestMapping(value = "/getsetting", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getSetting(@RequestParam String name, @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }
        return Settings.getSetting(name);
    }

    @RequestMapping(value = "/getAllSettings", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getSetting(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }

        try {
            Map<String, String> map = new HashMap<>();
                   map.put("REQUEST_NUMBER", Settings.getSetting("REQUEST_NUMBER"));
                   map.put("BLOCKED_BY_IP", Settings.getSetting("BLOCKED_BY_IP"));
                   map.put("REPEATED_REQUEST", Settings.getSetting("REPEATED_REQUEST"));
                   map.put("SENDING_NUMBER", Settings.getSetting("SENDING_NUMBER"));
                   map.put("NUMBER_FREE", Settings.getSetting("NUMBER_FREE"));
                   map.put("NO_NUMBERS_LEFT", Settings.getSetting("NO_NUMBERS_LEFT"));
                   map.put("ENDED_CALL", Settings.getSetting("ENDED_CALL"));
                   map.put("DB_ERROR_CONNECTING", Settings.getSetting("DB_ERROR_CONNECTING"));
                   map.put("SENDING_ANALYTICS", Settings.getSetting("SENDING_ANALYTICS"));
                   map.put("MAIL_SENDING_LOG", Settings.getSetting("MAIL_SENDING_LOG"));
                   map.put("MAIL_SENDING_ERRORS", Settings.getSetting("MAIL_SENDING_ERRORS"));
                   map.put("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE", Settings.getSetting("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE"));
                   map.put("SECONDS_TO_REMOVE_OLD_PHONES", Settings.getSetting("SECONDS_TO_REMOVE_OLD_PHONES"));
                   map.put("MAIL_ANTISPAM", Settings.getSetting("MAIL_ANTISPAM"));
            return new ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }

    }


    @RequestMapping(value = "/setsetting", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String setSetting(@RequestParam String name,
                             @RequestParam String value,
                             @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }
        Settings.setSetting(name, value);
        if (!name.equals("ACTIVE_SITE")) {
            String result = "Success: saved value " + value + " for " + name;
            MyLogger.log(LogCategory.DB_OPERATIONS, result);
            return new Message(Message.Status.Success, "Saved").toString();
        } else {
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/getNumbersCount", method = RequestMethod.POST, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getFreeNumbersCount(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }
        try {
            int freeOuter = PhonesDao.getFreePhones(false).size();
            int busyOuter = PhonesDao.getBusyOuterPhones().size();
            int busyInner = PhonesDao.getBusyInnerPhones().size();
            return "{\"freeOuter\":" + freeOuter + ",\"busyInner\":" + busyInner + ",\"busyOuter\":" + busyOuter + "}";
        } catch (Exception e) {
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    public static boolean isAdminPasswordWrong(String password) {
        return !password.equals(ADMIN_PASS);
    }
}
