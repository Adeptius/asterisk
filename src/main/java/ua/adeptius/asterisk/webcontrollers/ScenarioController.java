package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.dao.MySqlCalltrackDao;
import ua.adeptius.asterisk.json.JsonScenario;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Scenario;
import ua.adeptius.asterisk.model.ScenarioStatus;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.telephony.OldRule;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.telephony.DestinationType.GSM;

@Controller
@RequestMapping("/scenario")
public class ScenarioController {

    private static Logger LOGGER = LoggerFactory.getLogger(ScenarioController.class.getSimpleName());


//    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
//    @ResponseBody
//    public String addScenario(@RequestBody Scenario newOldRules, HttpServletRequest request) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//        return "";
//    }


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getScenarios(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        try {
//            List<Scenario> scenarios = user.getScenarios(); //#optimization
            List<Scenario> scenarios = HibernateDao.getAllScenariosByUser(user);
            return new ObjectMapper().writeValueAsString(scenarios);
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка получения сценариев", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/set", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8",produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String setScenarios(HttpServletRequest request, @RequestBody JsonScenario jsonScenario) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        System.out.println("Пришел сценарий " + jsonScenario);



        // TODO status не применять.
        if (jsonScenario.getId() != 0){
            Scenario scenario = user.getScenarioById(jsonScenario.getId());

            //TODO любое значение может прийти null
            scenario.setName(jsonScenario.getName());
            scenario.setFromList(jsonScenario.getFromNumbers()); // TODO валидация всех данных
            scenario.setToList(jsonScenario.getToNumbers());
            scenario.setDestinationType(jsonScenario.getDestinationType());
            scenario.setForwardType(jsonScenario.getForwardType());
            scenario.setDays(jsonScenario.getDays()); // TODO если меняем активный сценарий - нужно сначала проверить не возникнет ли конфликтов после изменения
            scenario.setAwaitingTime(jsonScenario.getAwaitingTime());
            scenario.setEndHour(jsonScenario.getEndHour());
            scenario.setStartHour(jsonScenario.getStartHour());
            scenario.setMelody(jsonScenario.getMelody());

            try{
                HibernateDao.update(user);
                user.setScenarios(HibernateDao.getAllScenariosByUser(user));
                return new Message(Message.Status.Success, "Сценарий обновлён").toString();
            }catch (Exception e){
                try{
                    user.setScenarios(HibernateDao.getAllScenariosByUser(user));
                }catch (Exception e1){
                    LOGGER.error("Не получилось отбекапится при неудаче изменения сценария пользователя"
                            +user.getLogin()+":\nСценарий "+ scenario, e1);
                }
                LOGGER.error("Не удалось обновить сценарий ID "+scenario.getId(), e);
                return new Message(Message.Status.Error, "Internal error").toString();
            }
        }else {
            System.out.println("Новый сценарий");

            Scenario scenario = new Scenario();
            scenario.setName(jsonScenario.getName());
            scenario.setFromList(jsonScenario.getFromNumbers());
            scenario.setToList(jsonScenario.getToNumbers());
            scenario.setDestinationType(jsonScenario.getDestinationType());
            scenario.setForwardType(jsonScenario.getForwardType());
            scenario.setDays(jsonScenario.getDays());
            scenario.setAwaitingTime(jsonScenario.getAwaitingTime());
            scenario.setEndHour(jsonScenario.getEndHour());
            scenario.setStartHour(jsonScenario.getStartHour());
            scenario.setMelody(jsonScenario.getMelody());
            scenario.setStatus(ScenarioStatus.DEACTIVATED);
            scenario.setUser(user);

            try {
                user.addScenario(scenario);
                HibernateDao.update(user);
                user.setScenarios(HibernateDao.getAllScenariosByUser(user)); // синхронизация с БД
                return new Message(Message.Status.Success, "Сценарий обновлён").toString();
            }catch (Exception e){
                try{
                    user.setScenarios(HibernateDao.getAllScenariosByUser(user));
                }catch (Exception e1){
                    LOGGER.error("Не получилось отбекапится при неудаче добавления сценария пользователя"
                            +user.getLogin()+":\nСценарий "+ scenario, e1);
                }
                LOGGER.error("Ошибка добавления сценария", e);
                return new Message(Message.Status.Error, e.getMessage()).toString();
            }
        }
    }




//    @RequestMapping(value = "/getAvailableNumbers", method = RequestMethod.POST, produces = "application/json")
//    @ResponseBody
//    public String getAvailableNumbers(HttpServletRequest request) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//
//        try{
//            return new ObjectMapper().writeValueAsString(user.getAvailableNumbers());
//        }catch (Exception e){
//            LOGGER.error(user.getLogin()+": ошибка получения доступных номеров для правил", e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        }
//    }


//    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json")
//    @ResponseBody
//    public String getRules(HttpServletRequest request) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//        try{
//            List<OldRule> oldRules = user.getOldRules();
//            return new ObjectMapper().writeValueAsString(oldRules);
//        }catch (Exception e){
//            LOGGER.error(user.getLogin()+": ошибка получения правил", e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        }
//    }


//    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json")
//    @ResponseBody
//    public String getRules(@RequestBody ArrayList<OldRule> newOldRules, HttpServletRequest request) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//
////         Проверяем есть ли дубликаты среди номеров с
//        Set<String> filterSet = new HashSet<>();
//        if (newOldRules.stream().flatMap(oldRule -> oldRule.getFrom().stream()).anyMatch(s -> !filterSet.add(s))) {
//            return new Message(Message.Status.Error, "Duplicates numbers found").toString();
//        }
//
////         Проверяем правильный ли формат номеров
//        List<String> numbersTo = newOldRules.stream().filter(oldRule -> oldRule.getDestinationType() == GSM)
//                .flatMap(oldRule -> oldRule.getTo().stream()).collect(Collectors.toList());
//
//        for (String s : numbersTo) {
//            Matcher regexMatcher = Pattern.compile("^0\\d{9}$").matcher(s);
//            if (!regexMatcher.find()) {
//                return new Message(Message.Status.Error, "Wrong GSM number format. Must be 0xxxxxxxxx. Regex: ^0\\d{9}$").toString();
//            }
//        }
//
//        try {
//            user.setOldRules(newOldRules);
//            user.saveRules();
//            return new Message(Message.Status.Success, "Saved").toString();
//        } catch (Exception e) {
//            LOGGER.error(user.getLogin()+": ошибка задания правил", e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        }
//    }

//    @RequestMapping(value = "/getMelodies", method = RequestMethod.POST, produces = "application/json")
//    @ResponseBody
//    public String getHistory() {
//        try {
//            return new Gson().toJson(MySqlCalltrackDao.getMelodies());
//        } catch (Exception e) {
//            LOGGER.error("Ошибка получения списка мелодий", e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        }
//    }
}