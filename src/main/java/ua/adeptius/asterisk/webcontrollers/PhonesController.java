package ua.adeptius.asterisk.webcontrollers;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/phones")
public class PhonesController {

    private static Logger LOGGER =  LoggerFactory.getLogger(PhonesController.class.getSimpleName());

    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getBlackList(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        Tracking tracking = user.getTracking();
        if (tracking == null) {
            return new Message(Message.Status.Error, "User have no tracking").toString();
        }

        try {
            return new Gson().toJson(tracking.getPhones());
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка получения состояния телефонов", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
