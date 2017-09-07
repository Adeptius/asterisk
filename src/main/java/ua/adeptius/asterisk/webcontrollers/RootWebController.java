package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.User;

import java.util.HashMap;


@Controller
public class RootWebController {

    private static Logger LOGGER = LoggerFactory.getLogger(RootWebController.class.getSimpleName());

    private static HashMap<String, User> usersCache = new HashMap<>();

    public static void clearCache() {
        usersCache.clear();
    }

    @GetMapping(value = "/getnumber/{user}/{site}/{googleid}/{ip}/{pagerequest}", produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String plaintext(@PathVariable String user,
                            @PathVariable String site,
                            @PathVariable String googleid,
                            @PathVariable String ip,
                            @PathVariable String pagerequest) {

        User userObject = UserContainer.getUserByName(user);
        if (userObject == null) {
            return "BAD_REQUEST";
        }

        Site siteObject = userObject.getSiteByName(site);
        if (site == null) {
            return "BAD_REQUEST";
        }

        return MainController.getFreeNumberFromSite(userObject, siteObject, googleid, ip, pagerequest);
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


    @PostMapping(value = "/getToken", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public Object checkLogin(@RequestParam String login, @RequestParam String password) {
        User user = UserContainer.getUserByName(login);
        if (user == null) {
            return new Message(Message.Status.Error, "Wrong login or password");
        }
        if (!user.getPassword().equals(password)) {
            return new Message(Message.Status.Error, "Wrong login or password");
        }
        String hash = UserContainer.getHashOfUser(user);
        if (hash == null) {
            return new Message(Message.Status.Error, "Wrong login or password");
        }

        return "{\"token\":\"" + hash + "\"}";
    }


    @RequestMapping(value = "/404")
    public String checkLogin() {
        return "error404";
    }



}
