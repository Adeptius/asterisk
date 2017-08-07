package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;

import java.util.Set;


@Controller
public class WebController {

    private static Logger LOGGER =  LoggerFactory.getLogger(WebController.class.getSimpleName());

    // todo кеширование юзеров в мапу

    @GetMapping(value = "/getnumber/{user}/{site}/{googleid}/{ip}/{pagerequest}", produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String plaintext(@PathVariable String user,
                     @PathVariable String site,
                     @PathVariable String googleid,
                     @PathVariable String ip,
                     @PathVariable String pagerequest) {
        User userObject = UserContainer.getUserByName(user); // todo тут нуллпоинтеры
        Site siteObject = userObject.getSiteByName(site);
        String phone = MainController.getFreeNumberFromSite(userObject, siteObject, googleid, ip, pagerequest);
        return convertPhone(phone);
    }


    @GetMapping(value = "/", produces = "text/html; charset=UTF-8")
    public String main() {
        return "main";
    }


    @GetMapping(value = "/login", produces = "text/html; charset=UTF-8")
    public String login() {
        return "login";
    }


    @PostMapping(value = "/getToken", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String checkLogin(@RequestParam String login, @RequestParam String password) {
        User user = UserContainer.getUserByName(login);
        if (user == null) {
            return new Message(Message.Status.Error, "Wrong login or password").toString();
        }
        if (!user.getPassword().equals(password)) {
            return new Message(Message.Status.Error, "Wrong login or password").toString();
        }
        String hash = UserContainer.getHashOfUser(user);
        if (hash == null) {
            return new Message(Message.Status.Error, "Wrong login or password").toString();
        }

        return "{\"token\":\"" + hash + "\"}";
    }


    public static String convertPhone(String source) {
        if (source.length() > 8) {
            int len = source.length();
            String s4 = source.substring(len - 2, len);
            String s3 = source.substring(len - 4, len - 2);
            String s2 = source.substring(len - 7, len - 4);
            String s1 = source.substring(0, len - 7);
            return String.format("(%s) %s-%s-%s", s1, s2, s3, s4);
        }
        return source;
    }
}
