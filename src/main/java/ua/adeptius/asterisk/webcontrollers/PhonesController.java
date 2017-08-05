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
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Controller
@RequestMapping("/phones")
public class PhonesController {
//
    private static Logger LOGGER =  LoggerFactory.getLogger(PhonesController.class.getSimpleName());
//
    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getBlackList(HttpServletRequest request, String sitename) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Message.Status.Error, "User have no tracking sites").toString();
        }
        Site site = user.getSiteByName(sitename);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site").toString();
        }

        try {
            return new Gson().toJson(site.getOuterPhones());
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка получения состояния телефонов", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
