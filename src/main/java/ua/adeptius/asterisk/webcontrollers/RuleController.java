package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.MySqlCalltrackDao;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.telephony.Rule;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.telephony.DestinationType.*;

@Controller
@RequestMapping("/rules")
public class RuleController {

    private static Logger LOGGER =  LoggerFactory.getLogger(RuleController.class.getSimpleName());


    @RequestMapping(value = "/getAvailableNumbers", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getAvailableNumbers(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        try{
            return new ObjectMapper().writeValueAsString(user.getAvailableNumbers());
        }catch (Exception e){
            LOGGER.error(user.getLogin()+": ошибка получения доступных номеров для правил", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getRules(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        try{
            List<Rule> rules = user.getRules();
            return new ObjectMapper().writeValueAsString(rules);
        }catch (Exception e){
            LOGGER.error(user.getLogin()+": ошибка получения правил", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json")
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
            LOGGER.error(user.getLogin()+": ошибка задания правил", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/getMelodies", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getHistory() {
        try {
            return new Gson().toJson(MySqlCalltrackDao.getMelodies());
        } catch (Exception e) {
            LOGGER.error("Ошибка получения списка мелодий", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}