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
import ua.adeptius.asterisk.telephony.OldRule;

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
            List<OldRule> oldRules = user.getOldRules();
            return new ObjectMapper().writeValueAsString(oldRules);
        }catch (Exception e){
            LOGGER.error(user.getLogin()+": ошибка получения правил", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getRules(@RequestBody ArrayList<OldRule> newOldRules, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

//         Проверяем есть ли дубликаты среди номеров с
        Set<String> filterSet = new HashSet<>();
        if (newOldRules.stream().flatMap(oldRule -> oldRule.getFrom().stream()).anyMatch(s -> !filterSet.add(s))) {
            return new Message(Message.Status.Error, "Duplicates numbers found").toString();
        }

//         Проверяем правильный ли формат номеров
        List<String> numbersTo = newOldRules.stream().filter(oldRule -> oldRule.getDestinationType() == GSM)
                .flatMap(oldRule -> oldRule.getTo().stream()).collect(Collectors.toList());

        for (String s : numbersTo) {
            Matcher regexMatcher = Pattern.compile("^0\\d{9}$").matcher(s);
            if (!regexMatcher.find()) {
                return new Message(Message.Status.Error, "Wrong GSM number format. Must be 0xxxxxxxxx. Regex: ^0\\d{9}$").toString();
            }
        }

        try {
            user.setOldRules(newOldRules);
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