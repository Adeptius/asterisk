package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.exceptions.JsonParseException;
import ua.adeptius.asterisk.exceptions.UkrainianNumberParseException;
import ua.adeptius.asterisk.json.*;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.model.telephony.*;
import ua.adeptius.asterisk.monitor.Scheduler;
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.json.Message.Status.Error;
import static ua.adeptius.asterisk.json.Message.Status.Success;
import static ua.adeptius.asterisk.model.telephony.RuleType.NORMAL;
import static ua.adeptius.asterisk.model.telephony.DestinationType.GSM;
import static ua.adeptius.asterisk.model.telephony.DestinationType.SIP;

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
            return new Message(Error, "Internal error");
        }
    }


    @PostMapping("/getAll")
    public Object getAllScenarios(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
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
    public Message removeScenario(HttpServletRequest request, Integer id) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        LOGGER.info("{}: запрос удаления сценария с id {}", user.getLogin(), id);

        if (id == null) {
            LOGGER.debug("{}: id пуст", user.getLogin());
            return new Message(Error, "Id is null");
        }

        Scenario scenario = user.getScenarioById(id);
        if (scenario == null) {
            LOGGER.debug("{}: сценарий по id {} не найден", user.getLogin(), id);
            return new Message(Error, "No such scenario");
        }

        user.removeScenario(scenario);
        try {
            HibernateController.update(user);
            LOGGER.debug("{}: сценарий удалён", user.getLogin());
            return new Message(Success, "Scenario deleted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления сценария c id " + id, e);
            return new Message(Success, "Internal error");
        }
    }


    @PostMapping("/set")
    public Message setScenario(@RequestBody JsonScenario jsonScenario, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        LOGGER.info("{}: запрос добавления сценария {}",user.getLogin(), jsonScenario);
        // создаём обьект Scenario из json запроса
        Scenario inScenario = new Scenario();
        String jName = jsonScenario.getName();
        inScenario.setName(jName);
        inScenario.setId(jsonScenario.getId());
        // сценарий создан

        // создаём правила сценария
        List<JsonRule> jRules = jsonScenario.getRules();
        List<Rule> rules = new ArrayList<>();

        int defaultRulesCount = 0;

        for (JsonRule jRule : jRules) {
            try {
                Rule rule = new Rule(jRule);
                rule.setUser(user); // это для того что бы не вылетел нулл позже при сохранении цепочки
                rules.add(rule); // в конструкторе создаётся rule c цепочкой из json обьекта
            } catch (JsonParseException e) {
                LOGGER.debug("{}: ошибка парсинга правила {}",user.getLogin(), jRule);
                return new Message(Error, "Parsing error. " + e.getMessage());
            }
        }

        // создаём мапу айдишников мелодий и обьектов мелодий для удобной и быстрой валидации
        HashMap<Integer, UserAudio> userMelodyIdAndMelody = new HashMap<>();
        user.getUserAudio().forEach(melody -> userMelodyIdAndMelody.put(melody.getId(), melody));

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

            Integer greeting = rule.getGreetingId();
            if (greeting != null) {
                UserAudio userAudio = userMelodyIdAndMelody.get(greeting);
                if (userAudio == null) {
                    return new Message(Error, name + ": greeting " + greeting + " not exists");
                }
            }

            Integer message = rule.getMessageId();
            if (message != null) {
                UserAudio userAudio = userMelodyIdAndMelody.get(message);
                if (userAudio == null) {
                    return new Message(Error, name + ": message " + message + " not exists");
                }
            }

            try {
                List<String> melodiesFromCache = getMelodiesFromCache();
                String melody = rule.getMelody();
                if (!melodiesFromCache.contains(melody)) {
                    return new Message(Error, name + ": Melody '" + melody + "' does not exist");
                }
            } catch (Exception e) {
                LOGGER.error("Ошибка загрузки мелодий из кэша");
                return new Message(Error, "Internal error");
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
                        } catch (UkrainianNumberParseException e) {
                            return new Message(Error, name + ": Number '" + gsmNumber + "' is not Ukrainian");
                        }
                    }
                    element.setToList(toList);
                }

//                ForwardType forwardType = element.getForwardType();

                if (element.getAwaitingTime() < 1) {
                    return new Message(Error, name + ": Awaiting time must be higher than 0");
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
        LOGGER.debug("{}: сценарий установлен",user.getLogin());
        return new Message(Success, "Scenario setted");
    }


    @PostMapping("/getBindings")
    public Object getBindings(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        Set<OuterPhone> outerPhones = user.getOuterPhones();
        Set<Scenario> scenarios = user.getScenarios();

        return new JsonScenarioBindings(outerPhones, scenarios);
    }

    @PostMapping("/setBindings")
    public Message getBindings(HttpServletRequest request, @RequestBody HashMap<String, Integer> newBindings) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        LOGGER.info("{}: запрос привязок сценариев на номера {}", user.getLogin(), newBindings);

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

        Scheduler.reloadDialPlanForThisUserAtNextScheduler(user);
        Scheduler.rewriteRulesFilesForThisUserAtNextScheduler(user);

        try {
            HibernateController.update(user);
            LOGGER.debug("{}: привязка сценариев к номерам выполнена", user.getLogin());
            return new Message(Success, "Bindings setted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + " ошибка сохранения привязок сценариев к телефонам: " + newBindings, e);
            return new Message(Error, "Internal error");
        }
    }
}