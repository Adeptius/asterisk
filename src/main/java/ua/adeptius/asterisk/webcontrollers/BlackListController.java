package ua.adeptius.asterisk.webcontrollers;

import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/blacklist")
public class BlackListController {


    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String addToBlackList(@RequestParam String ip, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have no tracking").toString();
        }
        try {
            Matcher regexMatcher = Pattern.compile("(\\d{1,3}[.]){3}\\d{1,3}").matcher(ip.trim());
            regexMatcher.find();
            regexMatcher.group();
        }catch (Exception e){
            return new Message(Message.Status.Error, "Wrong ip").toString();
        }
        user.getTracking().addIpToBlackList(ip);
        try {
            HibernateController.updateUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }
        return new Message(Message.Status.Success, "Added").toString();
    }


    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String removeFromBlackList(@RequestParam String ip, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have no tracking").toString();
        }
        user.getTracking().removeIpFromBlackList(ip);
        try {
            HibernateController.updateUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }
        return new Message(Message.Status.Success, "Removed").toString();
    }


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getBlackList(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have no tracking").toString();
        }

        try {
            LinkedList<String> list = user.getTracking().getBlackList();
            return new Gson().toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
