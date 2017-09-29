package ua.adeptius.asterisk.webcontrollers;


import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.hibernate.boot.jaxb.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.exceptions.UkrainianNumberParseException;
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

    @GetMapping(value = "/{username}/{number}/{siteName}")
    @ResponseBody
    public Object plaintext(@PathVariable String username, @PathVariable String number, @PathVariable String siteName) {
        User user = UserContainer.getUserByName(username);
        if (user == null) {
            return "BAD_REQUEST";
        }

        try {
            number = MyStringUtils.cleanAndValidateUkrainianPhoneNumber(number);
        } catch (UkrainianNumberParseException e) {
            return new Message(Message.Status.Error, "Wrong number");
        }

//        OriginateAction originateAction = AsteriskActionsGenerator.callToOutsideFromOuter("0443211118", number, "C2C "+number);
        OriginateAction originateAction = AsteriskActionsGenerator
                .callToOutside("2001036", number, "C2C " + siteName + " " + number);
        // todo нужно сделать какую-то внутреннюю линию
        try {
            ManagerResponse managerResponse = Main.monitor.sendAction(originateAction, 10);
            System.out.println(managerResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException ignored){
            System.out.println("Timeout");
        }
        return new Message(Message.Status.Success, "Success");
    }
}
