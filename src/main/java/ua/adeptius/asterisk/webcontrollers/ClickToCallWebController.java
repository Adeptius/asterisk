package ua.adeptius.asterisk.webcontrollers;


import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;
import ua.adeptius.asterisk.utils.MyStringUtils;

import java.io.IOException;
import java.util.HashMap;


@Controller
@RequestMapping(value = "/c2c", produces = "application/json; charset=UTF-8")
@ResponseBody
public class ClickToCallWebController {

    private static Logger LOGGER = LoggerFactory.getLogger(ClickToCallWebController.class.getSimpleName());

    private static HashMap<String, User> usersCache = new HashMap<>();

    public static void clearCache() {
        usersCache.clear();
    }

    @GetMapping(value = "/{user}/{number}")
    @ResponseBody
    public Object plaintext(@PathVariable String user, @PathVariable String number) {
        User userObject = UserContainer.getUserByName(user);
        if (userObject == null) {
            return "BAD_REQUEST";
        }

        try {
            number = MyStringUtils.cleanAndValidateUkrainianPhoneNumber(number);
        } catch (IllegalArgumentException e) {
            return new Message(Message.Status.Error, "Wrong number");
        }

        OriginateAction originateAction = AsteriskActionsGenerator.callToOutsideFromOuter("0443211118", number, "C2C "+number);
        // todo нужно сделать какую-то внутреннюю линию
        try {
            ManagerResponse managerResponse = Main.monitor.sendAction(originateAction, 10);
            System.out.println(managerResponse);
        } catch (IOException e) {
//            e.printStackTrace();
        } catch (TimeoutException e) {
//            e.printStackTrace();
        }
        return new Message(Message.Status.Success, "Success");
    }


//    @GetMapping(value = "/", produces = "text/html; charset=UTF-8")
//    public String main() {
//        return "main";
//    }
//
//
//    @GetMapping(value = "/login", produces = "text/html; charset=UTF-8")
//    public String login() {
//        return "login";
//    }


//    @PostMapping(value = "/getToken", produces = "application/json; charset=UTF-8")
//    @ResponseBody
//    public Object checkLogin(@RequestParam String login, @RequestParam String password) {
//        User user = UserContainer.getUserByName(login);
//        if (user == null) {
//            return new Message(Message.Status.Error, "Wrong login or password");
//        }
//        if (!user.getPassword().equals(password)) {
//            return new Message(Message.Status.Error, "Wrong login or password");
//        }
//        String hash = UserContainer.getHashOfUser(user);
//        if (hash == null) {
//            return new Message(Message.Status.Error, "Wrong login or password");
//        }
//
//        return "{\"token\":\"" + hash + "\"}";
//    }
}
