package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.newmodel.User;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.controllers.MainController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.telephony.DestinationType.*;

@Controller
@RequestMapping("/rules")
public class RuleController {


    @RequestMapping(value = "/getAvailableNumbers", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getAvailableNumbers(@RequestParam String name, @RequestParam String password) {

//        if (!MainController.isLogin(name, password)) {
//            return "Error: wrong password";
//        }
//
//        User user;
//        try {
//            user = MainController.getUserByName(name);
//        } catch (NoSuchElementException e) {
//            return "Error: no such user";
//        }
//
//        return new Gson().toJson(user.getAvailableNumbers());
        return "";
    }



    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getRules(@RequestParam String name, @RequestParam String password) {

//        if (!MainController.isLogin(name, password)) {
//            return "Error: wrong password";
//        }
//
//        User user;
//        try {
//            user = MainController.getUserByName(name);
//        } catch (NoSuchElementException e) {
//            return "Error: no such user";
//        }
//
//        List<Rule> rules = user.getRules();
//        return new Gson().toJson(rules);
        return "";
    }


    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getRules(@RequestBody ArrayList<Rule> newRules, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

//         Проверяем есть ли дубликаты среди номеров с
        Set<String> filterSet = new HashSet<>();
        if (newRules.stream().flatMap(rule -> rule.getFrom().stream()).anyMatch(s -> !filterSet.add(s))) {
            return new Message(Message.Status.Error, "Duplicates numbers found").toString();
        }

//         Проверяем правильный ли формат номеров
        List<String> numbersTo = newRules.stream().filter(rule -> rule.getDestinationType() == GSM)
                .flatMap(rule -> rule.getTo().stream()).collect(Collectors.toList());

        for (String s : numbersTo) {
            Matcher regexMatcher = Pattern.compile("^0\\d{9}$").matcher(s);
            if (!regexMatcher.find()) {
                return new Message(Message.Status.Error, "Wrong GSM number format. Must be 0xxxxxxxxx. Regex: ^0\\d{9}$").toString();
            }
        }

        try {
            user.setRules(newRules);
            user.saveRules();
            return new Message(Message.Status.Success, "Saved").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
