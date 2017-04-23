package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.newmodel.HibernateController;
import ua.adeptius.asterisk.newmodel.User;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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


    @RequestMapping(value = "/getAllUsers", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getAllNameOfCustomers(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Wrong password";
        }
        return new Gson().toJson(UserContainer.getUsers().stream().map(User::getLogin).collect(Collectors.toList()));
    }

    @RequestMapping(value = "/getHash", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getHash(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return "Wrong password";
        }
        HashMap<String, String> hashMap = new HashMap<>();

        for (Map.Entry<String, User> entry : UserContainer.getHashes().entrySet()) {
            hashMap.put(entry.getValue().getLogin(), entry.getKey());
        }
        return new Gson().toJson(hashMap);
    }

    @RequestMapping(value = "/removeUser", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getHash(@RequestParam String adminPassword, @RequestParam String username) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }

        User user = UserContainer.getUserByName(username);
        if (user == null){
            return new Message(Message.Status.Error, "User not found").toString();
        }
        try {
            HibernateController.removeUser(user);
        }catch (Exception e){
            return new Message(Message.Status.Error, "Internal error").toString();
        }
        return new Message(Message.Status.Success, "Removed").toString();
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
            return "{\"freeInner\":" + freeInner + ",\"freeOuter\":" + freeOuter + ",\"busyInner\":" + busyInner + ",\"busyOuter\":" + busyOuter + "}";
        } catch (Exception e) {
            return "Error: db Error";
        }
    }

    private boolean isAdminPasswordWrong(String password) {
        return !password.equals(ADMIN_PASS);
    }
}
