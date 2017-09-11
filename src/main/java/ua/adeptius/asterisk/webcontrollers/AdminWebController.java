package ua.adeptius.asterisk.webcontrollers;

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
public class AdminWebController {

    public static final String ADMIN_PASS = "csadmx84";
    private static Logger LOGGER = LoggerFactory.getLogger(AdminWebController.class.getSimpleName());


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
