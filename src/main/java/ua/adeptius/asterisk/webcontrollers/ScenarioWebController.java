package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.exceptions.JsonParseException;
import ua.adeptius.asterisk.json.*;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.json.Message.Status.Error;
import static ua.adeptius.asterisk.json.Message.Status.Success;
import static ua.adeptius.asterisk.model.RuleType.NORMAL;
import static ua.adeptius.asterisk.telephony.DestinationType.GSM;
import static ua.adeptius.asterisk.telephony.DestinationType.SIP;
import static ua.adeptius.asterisk.telephony.ForwardType.QUEUE;
import static ua.adeptius.asterisk.telephony.ForwardType.TO_ALL;

@Controller
@RequestMapping(value = "/scenario", produces = "application/json; charset=UTF-8")
@ResponseBody
public class ScenarioWebController {

    //    private static boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(ScenarioWebController.class.getSimpleName());

    private static List<String> melodies;
    private static long melodiesTimeCache;

    private static void loadMelodies() throws Exception {
        melodies = HibernateController.getMelodies();
        melodiesTimeCache = new Date().getTime();
    }

    private List<String> getMelodiesFromCache() throws Exception {
        if (melodies == null) {
            loadMelodies();
        }

        long currentTime = new Date().getTime();
        long past = currentTime - melodiesTimeCache;
        long updateEvery = 3600000; // обновление каждый час

        if (past > updateEvery) {
            loadMelodies();
        }
        return melodies;
    }

    @PostMapping("/getMelodies")
    public Object getHistory() {
        try {
            return getMelodiesFromCache();
        } catch (Exception e) {
            LOGGER.error("Ошибка получения списка мелодий", e);
            return new Message(Error, "Internal error").toString();
        }
    }


    @PostMapping("/getAll")
    public Object getAllScenarios(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid").toString();
        }
        return user.getScenarios();
    }

    @PostMapping("/get")
    public Object getScenarioById(Integer id, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        if (id == null) {
            return new Message(Error, "Id is null");
        }

        Scenario scenario = user.getScenarioById(id);
        if (scenario == null) {
            return new Message(Error, "No such scenario");
        }
        return scenario;
    }

    @PostMapping("/remove")
    public Object removeScenario(HttpServletRequest request, Integer id) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid").toString();
        }

        if (id == null) {
            return new Message(Error, "Id is null");
        }

        Scenario scenario = user.getScenarioById(id);
        if (scenario == null) {
            return new Message(Error, "No such scenario");
        }

        user.removeScenario(scenario);
        try {
            HibernateController.update(user);
            return new Message(Success, "Scenario deleted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления сценария c id " + id, e);
            return new Message(Success, "Internal error");
        }
    }


    @PostMapping("/set")
    public Object setScenario(@RequestBody JsonScenario jsonScenario, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid").toString();
        }

        // создаём обьект Scenario из json запроса
        Scenario inScenario = new Scenario();
        String jName = jsonScenario.getName();
        inScenario.setName(jName);
        inScenario.setId(jsonScenario.getId());
        // сценарий создан

        // создаём правила сценария
        List<JsonRule> jRules = jsonScenario.getRules();
        List<Rule> rules = new ArrayList<>();

//        HashMap<Rule, List<ChainElement>> rulesAndChains = new HashMap<>();


        int defaultRulesCount = 0;

        for (JsonRule jRule : jRules) {
            try {
                Rule rule = new Rule(jRule);
                rule.setUser(user); // это для того что бы не вылетел нулл позже при сохранении цепочки
//                HashMap<Integer, JsonChainElement> jchain = jRule.getChain();
//                HashMap<Integer, ChainElement> chain = new HashMap<>();
//                for (Map.Entry<Integer, ChainElement> entry : chain.entrySet()) {
//                    chain.put(entry.getKey(), new ChainElement(entry.getValue()));
//                }
//
//                rule.setChainByDirectFieldOnlyForWebControllerDontUseRegulary(chain);
                rules.add(rule); // в конструкторе создаётся rule c цепочкой из json обьекта
            } catch (JsonParseException e) {
                return new Message(Error, "Parsing error. " + e.getMessage());
            }
        }

        // создали inScenario и rules. теперь валидация
        for (Rule rule : rules) {
            String name = rule.getName();

            if (rule.getType() == RuleType.DEFAULT) {
                defaultRulesCount++;
                rule.setStartHour(0);
                rule.setEndHour(24);
                rule.setDays(new boolean[]{true, true, true, true, true, true, true});
            }

            int startHour = rule.getStartHour();
            if (startHour < 0 || startHour > 23) {
                return new Message(Error, name + ": Wrong start hour");
            }

            int endHour = rule.getEndHour();
            if (endHour < 1 || endHour > 24) {
                return new Message(Error, name + ": Wrong end hour");
            }

            if (startHour == endHour || startHour > endHour) {
                return new Message(Error, name + ": Wrong time range. From " + startHour + " to " + endHour);
            }


            HashMap<Integer, ChainElement> chain = rule.getChain();
            if (chain.size() == 0 && chain.get(0) == null) {
                return new Message(Error, name + ": no chain");
            }

            for (int i = 0; i < chain.size(); i++) {
                ChainElement element = chain.get(i);
                if (element == null) {
                    return new Message(Error, name + ": wrong numeration");
                }

                element.setPosition(i); // добавляем нумерацию

                // валидация номеров телефонов
                DestinationType destinationType = element.getDestinationType();
                List<String> toList = element.getToList();
                if (toList.size() == 0) {
                    return new Message(Error, name + ": ToList is empty");
                }

                if (destinationType == SIP) {
                    for (String sipNumber : toList) {
                        if (!user.isThatUserSipNumber(sipNumber)) {
                            return new Message(Error, name + ": Number '" + sipNumber + "' is not SIP");
                        }
                    }
                } else if (destinationType == GSM) {
                    for (int j = 0; j < toList.size(); j++) {
                        String gsmNumber = toList.get(j);
                        try {
                            toList.set(j, MyStringUtils.cleanAndValidateUkrainianPhoneNumber(gsmNumber));
                        } catch (IllegalArgumentException e) {
                            return new Message(Error, name + ": Number '" + gsmNumber + "' is not Ukrainian");
                        }
                    }
                    element.setToList(toList);
                }

//                ForwardType forwardType = element.getForwardType();

                if (element.getAwaitingTime() < 1) {
                    return new Message(Error, name + ": Awaiting time must be higher than 0");
                }


                try {
                    List<String> melodiesFromCache = getMelodiesFromCache();
                    String melody = element.getMelody();
                    if (!melodiesFromCache.contains(melody)) {
                        return new Message(Error, name + ": Melody '" + melody + "' does not exist");
                    }
                } catch (Exception e) {
                    LOGGER.error("Ошибка загрузки мелодий из кэша");
                    return new Message(Error, "Internal error");
                }
            }

        }
        if (defaultRulesCount != 1) {
            return new Message(Error, "Scenario must contains one default rule.");
        }

//        данные валидированы. Теперь ищем конфликты.
        List<Rule> normalRules = rules.stream().filter(rule -> rule.getType() == NORMAL).collect(Collectors.toList());
        // Сравниваем всё со всеми
        for (Rule first : normalRules) {
            for (Rule second : normalRules) {
                if (first.equals(second)) { // если сравниваемое правило сравнивается с ним же.
                    continue;
                }
                if (!first.isThisRuleCompatibleWith(second)) {
                    return new Message(Error, "Rule '" + first.getName() + "' has time conflict with '" + second.getName() + "'");
                }
            }
        }

        int inScenarioId = inScenario.getId();


//        ищем cценарий с тем же id
        Scenario scenarioByName = user.getScenarioByName(jName);
        if (scenarioByName != null && scenarioByName.getId() != inScenarioId) {
            return new Message(Error, "Scenario '" + jName + "' already present");
        }

        // валидация завершена. Сохраняем всё.

        Scenario presentScenario = user.getScenarioById(inScenarioId);
        if (presentScenario != null) {
            user.removeScenarioButLeaveIdInPhone(presentScenario);
        }

        user.addScenario(inScenario);

        for (Rule rule : rules) {
            Collection<ChainElement> chainElements = rule.getChain().values();
            for (ChainElement chainElement : chainElements) {
                rule.addChainElement(chainElement);
            }
            inScenario.addRule(rule);
        }


        // как только сохраняется сценарий - проверить на каких номерах он активирован и поменять имя в телефонах, если оно изменилось.
        HibernateController.update(user);
        return new Message(Success, "Scenario setted");
    }


    @PostMapping("/getBindings")
    public Object getBindings(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid").toString();
        }

        Set<OuterPhone> outerPhones = user.getOuterPhones();
        Set<Scenario> scenarios = user.getScenarios();

        return new JsonScenarioBindings(outerPhones, scenarios);
    }

    @PostMapping("/setBindings")
    public Object getBindings(HttpServletRequest request, @RequestBody HashMap<String, Integer> newBindings) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid").toString();
        }

        // проверим все ли присланные id сценариев существуют
        for (Integer id : newBindings.values()) {
            if (user.getScenarioById(id) == null) {
                return new Message(Error, "No scenario with id " + id);
            }
        }

        for (OuterPhone outerPhone : user.getOuterPhones()) {
            Integer scenarioId = newBindings.get(outerPhone.getNumber());
            outerPhone.setScenarioId(scenarioId);// если ключа в мапе нет - вернётся тгдд и телефон освободится.
        }

        try {
            HibernateController.update(user);
            return new Message(Success, "Bindings setted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + " ошибка сохранения привязок сценариев к телефонам: " + newBindings, e);
            return new Message(Error, "Internal error");
        }

    }
}