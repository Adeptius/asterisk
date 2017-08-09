package ua.adeptius.asterisk.webcontrollers;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "/admin", produces = "application/json; charset=UTF-8")
@ResponseBody
public class AdminController {

    public static final String ADMIN_PASS = "csadmx84";
    private static Logger LOGGER = LoggerFactory.getLogger(AdminController.class.getSimpleName());
    private static ObjectMapper mapper = new ObjectMapper();


    @PostMapping(value = "/getAllUsers")
    public Object getAllNameOfCustomers(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password");
        }
        return UserContainer.getUsers().stream().map(User::getLogin).collect(Collectors.toList());
    }

    @PostMapping(value = "/getTokens", produces = "application/json")
    public Object getHash(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password");
        }
        HashMap<String, String> hashMap = new HashMap<>();

        for (Map.Entry<String, User> entry : UserContainer.getHashes().entrySet()) {
            hashMap.put(entry.getValue().getLogin(), entry.getKey());
        }
        return hashMap;
    }


//    @PostMapping(value = "/getsetting", produces = "text/html; charset=UTF-8")
//    public String getSetting(@RequestParam String name, @RequestParam String adminPassword) {
//        if (isAdminPasswordWrong(adminPassword)) {
//            return "Wrong password";
//        }
//        return Settings.getSetting(name);
//    }

    @PostMapping("/getAllSettings")
    public Object getSetting(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password");
        }

        Map<String, String> map = new HashMap<>();
        map.put("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE", Settings.getSetting("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE"));
        map.put("SECONDS_TO_REMOVE_OLD_PHONES", Settings.getSetting("SECONDS_TO_REMOVE_OLD_PHONES"));
        map.put("MAIL_ANTISPAM", Settings.getSetting("MAIL_ANTISPAM"));
        return map;
    }


    @PostMapping("/setSetting")
    public Object setSetting(@RequestParam String name,
                             @RequestParam String value,
                             @RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password");
        }
        Settings.setSetting(name, value);

        String result = "Success: saved value " + value + " for " + name;
        return new Message(Message.Status.Success, "Saved");
    }

    @PostMapping("/getNumbersCount")
    public Object getFreeNumbersCount(@RequestParam String adminPassword) {
        if (isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password");
        }
        try {
            int freeOuter = PhonesDao.getFreeOuterPhones().size();
            int busyOuter = PhonesDao.getBusyOuterPhones().size();
            int busyInner = PhonesDao.getBusyInnerPhones().size();
            return "{\"freeOuter\":" + freeOuter + ",\"busyInner\":" + busyInner + ",\"busyOuter\":" + busyOuter + "}";
        } catch (Exception e) {
            return new Message(Message.Status.Error, "Internal error");
        }
    }

    public static boolean isAdminPasswordWrong(String password) {
        return !password.equals(ADMIN_PASS);
    }
}
