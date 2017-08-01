package ua.adeptius.asterisk.webcontrollers;


import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.AmoAccountNotFoundException;
import ua.adeptius.amocrm.exceptions.AmoCantCreateDealException;
import ua.adeptius.amocrm.exceptions.AmoUnknownException;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyExeption;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.JsonAmoForController;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;

import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@RequestMapping("/widget")
public class AmoWidgetController {

    private static boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(AmoWidgetController.class.getSimpleName());

//    @Produces()
    @RequestMapping(value = "/c2c", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String check(HttpServletRequest request,
                        @RequestParam int amoUserId,
                        @RequestParam String amoUserName,
                        @RequestParam String amoUserLogin,
                        @RequestParam String pressedPhone
    ) {

        System.out.println(amoUserId);
        System.out.println(amoUserName);
        System.out.println(amoUserLogin);
        System.out.println(pressedPhone);

        OriginateAction action = AsteriskActionsGenerator.callToOutside("2001036", pressedPhone);
        try {
            Main.monitor.sendAction(action, 5000);
            return "Calling!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error...";
        }
    }
}
