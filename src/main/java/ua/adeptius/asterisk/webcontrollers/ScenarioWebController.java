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

        if (id == null){
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

        if (id == null){
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
            LOGGER.error(user.getLogin()+": ошибка удаления сценария c id " + id, e);
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

            if (rule.getType() == RuleType.DEFAULT){
                defaultRulesCount++;
                rule.setStartHour(0);
                rule.setEndHour(24);
                rule.setDays(new boolean[]{true,true,true,true,true,true,true});
            }

            int startHour = rule.getStartHour();
            if (startHour < 0 || startHour > 23){
                return new Message(Error, name + ": Wrong start hour");
            }

            int endHour = rule.getEndHour();
            if (endHour < 1 || endHour > 24){
                return new Message(Error, name + ": Wrong end hour");
            }

            if (startHour == endHour || startHour > endHour){
                return new Message(Error, name + ": Wrong time range. From " + startHour + " to " + endHour);
            }


            HashMap<Integer, ChainElement> chain = rule.getChain();
            if (chain.size()==0 && chain.get(0) == null){
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
                if (toList.size()==0){
                    return new Message(Error, name + ": ToList is empty");
                }

                if (destinationType == SIP){
                    for (String sipNumber : toList) {
                        if (!user.isThatUserSipNumber(sipNumber)){
                            return new Message(Error, name + ": Number '" + sipNumber + "' is not SIP");
                        }
                    }
                }else if (destinationType == GSM){
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

                ForwardType forwardType = element.getForwardType();
                if (forwardType == TO_ALL){
                    element.setAwaitingTime(600);
                }else if (forwardType == QUEUE){
                    if (element.getAwaitingTime() < 1){
                        return new Message(Error, name + ": Awaiting time must be higher than 0");
                    }
                }


                try {
                    List<String> melodiesFromCache = getMelodiesFromCache();
                    String melody = element.getMelody();
                    if (!melodiesFromCache.contains(melody)){
                        return new Message(Error, name + ": Melody '" + melody + "' does not exist");
                    }
                } catch (Exception e) {
                    LOGGER.error("Ошибка загрузки мелодий из кэша");
                    return new Message(Error, "Internal error");
                }
            }

        }
        if (defaultRulesCount != 1){
            return new Message(Error, "Scenario must contains one default rule.");
        }

//        данные валидированы. Теперь ищем конфликты.
        List<Rule> normalRules = rules.stream().filter(rule -> rule.getType() == NORMAL).collect(Collectors.toList());
        // Сравниваем всё со всеми
        for (Rule first : normalRules) {
            for (Rule second : normalRules) {
                if (first.equals(second)){ // если сравниваемое правило сравнивается с ним же.
                    continue;
                }
                if(!first.isThisRuleCompatibleWith(second)){
                    return new Message(Error, "Rule '" + first.getName() + "' has time conflict with '" + second.getName() + "'");
                }
            }
        }

        int inScenarioId = inScenario.getId();


//        ищем cценарий с тем же id
        Scenario scenarioByName = user.getScenarioByName(jName);
        if (scenarioByName != null && scenarioByName.getId() != inScenarioId){
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
            if (user.getScenarioById(id) == null){
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



//    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
//    @ResponseBody
//    public String getScenarios(HttpServletRequest request) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//        try {
//            List<Scenario> scenarios = user.getScenarios();
//            return new ObjectMapper().writeValueAsString(scenarios);
//        } catch (Exception e) {
//            LOGGER.error(user.getLogin() + ": ошибка получения сценариев", e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        }
//    }
//
//    @RequestMapping(value = "/activate", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
//    @ResponseBody
//    public String activateScenario(HttpServletRequest request, @RequestParam int id) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//
//        Scenario scenario;
//        try {
//            scenario = user.getScenarioById(id);
//        } catch (NoSuchElementException e) {
//            return new Message(Message.Status.Error, "No such scenario by id " + id).toString();
//        }
//
//        try {
//            user.activateScenario(id);
//        } catch (ScenarioConflictException e) {
//            return new Message(Message.Status.Error, e.getMessage()).toString();
//        }
//
//        try {
//            HibernateDao.update(user);
//            return new Message(Message.Status.Success, "Scenario activated").toString();
//        } catch (Exception e) {
//            LOGGER.error("Ошибка БД при активации сценария " + scenario, e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        } finally {
//            if (safeMode)
//                user.reloadScenariosFromDb();
//        }
//    }
//
//
//    @RequestMapping(value = "/deactivate", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
//    @ResponseBody
//    public String deactivateScenario(HttpServletRequest request, @RequestParam int id) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//
//        Scenario scenario;
//        try {
//            scenario = user.getScenarioById(id);
//        } catch (NoSuchElementException e) {
//            return new Message(Message.Status.Error, "No such scenario by id " + id).toString();
//        }
//
//        user.deactivateScenario(id);
//
//        try {
//            HibernateDao.update(user);
//            return new Message(Message.Status.Success, "Scenario deactivated").toString();
//        } catch (Exception e) {
//            LOGGER.error("Ошибка БД при деактивации сценария " + scenario, e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        } finally {
//            if (safeMode)
//                user.reloadScenariosFromDb();
//        }
//    }
//
//
//    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
//    @ResponseBody
//    public String removeScenario(HttpServletRequest request, @RequestParam int id) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//
//        Scenario scenario;
//        try {
//            scenario = user.getScenarioById(id);
//        } catch (NoSuchElementException e) {
//            return new Message(Message.Status.Error, "No such scenario by id " + id).toString();
//        }
//
//        user.getScenarios().remove(scenario);
//
//        try {
//            HibernateDao.update(user);
//            return new Message(Message.Status.Success, "Scenario removed").toString();
//        } catch (Exception e) {
//            LOGGER.error("Ошибка БД при удалении сценария " + scenario, e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        } finally {
//            if (safeMode)
//                user.reloadScenariosFromDb();
//        }
//    }
//
//
//    @SuppressWarnings("Duplicates")
//    @RequestMapping(value = "/set", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8", produces = "application/json; charset=UTF-8")
//    @ResponseBody
//    public String setScenarios(HttpServletRequest request, @RequestBody JsonScenario jsonScenario) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//
//        System.out.println("Пришел сценарий " + jsonScenario);
//
//        String name = jsonScenario.getName();
//        List<String> fromNumbers = jsonScenario.getFromNumbers();
//        List<String> toNumbers = jsonScenario.getToNumbers();
//        DestinationType destinationType = jsonScenario.getDestinationType();
//        ForwardType forwardType = jsonScenario.getForwardType();
//        boolean[] days = jsonScenario.getDays();
//        int awaitingTime = jsonScenario.getAwaitingTime();
//        Integer endHour = jsonScenario.getEndHour();
//        Integer startHour = jsonScenario.getStartHour();
//        String melody = jsonScenario.getMelody();
//        int id = jsonScenario.getId();
//
//        if (fromNumbers != null) {
//            List<String> wrongNumbers = new ArrayList<>();
//            for (String number : fromNumbers) {
//                if (!user.isThatUsersOuterNumber(number)) {
//                    wrongNumbers.add(number);
//                }
//            }
//            fromNumbers.removeAll(wrongNumbers);
//        }
//
//
//        if (id != 0) { // обновление сценария
//            System.out.println("обновление сценария");
//            Scenario scenario;
//
//            try {
//                scenario = user.getScenarioById(id);
//            } catch (NoSuchElementException e) {
//                return new Message(Message.Status.Error, "Wrong id").toString();
//            }
//
//            if (name != null) {
//                List<String> currentNames = new ArrayList<>();
//                for (Scenario sc : user.getScenarios()) {
//                    if (sc.getId() != id) {
//                        currentNames.add(sc.getName());
//                    }
//                }
//                if (currentNames.contains(name)) {
//                    return new Message(Message.Status.Error, "Such scenario name already present").toString();
//                }
//                scenario.setName(name);
//            }
//
//            if (fromNumbers != null) {
//                scenario.setFromList(fromNumbers);
//            }
//
//            if (destinationType != null) {
//                scenario.setDestinationType(destinationType);
//            }
//
//            if (toNumbers != null) {
//                if (scenario.getDestinationType() == DestinationType.SIP) {
//                    List<String> needRemove = new ArrayList<>();
//                    for (String number : toNumbers) {
//                        if (!user.isThatUsersInnerNumber(number)) {
//                            needRemove.add(number);
//                        }
//                    }
//                    toNumbers.removeAll(needRemove);
//                }
//                scenario.setToList(toNumbers);
//            }
//
//
//            if (forwardType != null) {
//                scenario.setForwardType(forwardType);
//            }
//
//            if (days != null) {
//                scenario.setDays(days);
//            }
//
//            if (awaitingTime != 0) {
//                scenario.setAwaitingTime(awaitingTime);
//            }
//
//            if (endHour != null) {
//                scenario.setEndHour(endHour);
//            }
//
//            if (startHour != null) {
//                scenario.setStartHour(startHour);
//            }
//
//            if (melody != null) {
//                scenario.setMelody(melody);
//            }
//
//            boolean wasActivated = scenario.getStatus() == ScenarioStatus.ACTIVATED;
//            scenario.setStatus(ScenarioStatus.DEACTIVATED);
//            boolean errorWhileActivation = false;
//            String errorMessage = null;
//
//            if (wasActivated) {
//                try {
//                    user.activateScenario(id);
//                } catch (ScenarioConflictException e) {
//                    errorWhileActivation = true;
//                    errorMessage = e.getMessage();
//                }
//            }
//
//            try {
//                HibernateDao.update(user);
//                user.setScenarios(HibernateDao.getAllScenariosByUser(user));
//
//                if (!errorWhileActivation) { // если при активации не произошло ошибки
//                    return new Message(Message.Status.Success, "Scenario updated").toString();
//
//                } else { // была ошибка активации
//                    return new Message(Message.Status.Error, "Updated but deactivated: " + errorMessage).toString();
//                }
//            } catch (Exception e) {
//                LOGGER.error("Не удалось обновить сценарий " + scenario, e);
//                return new Message(Message.Status.Error, "Internal error").toString();
//            } finally {
//                if (safeMode)
//                    user.reloadScenariosFromDb();
//            }
//
//
//        } else {// Создание сценария
//            System.out.println("Создание сценария");
//
//            Scenario scenario = new Scenario();
//            if (StringUtils.isBlank(name)) {
//                return new Message(Message.Status.Error, "Scenario name is blank").toString();
//            } else {
//                scenario.setName(name);
//            }
//
//            if (fromNumbers != null) {
//                scenario.setFromList(fromNumbers);
//            }
//
//            if (toNumbers != null) {
//                if (destinationType == DestinationType.SIP) {
//                    List<String> needRemove = new ArrayList<>();
//                    for (String number : toNumbers) {
//                        if (!user.isThatUsersInnerNumber(number)) {
//                            needRemove.add(number);
//                        }
//                    }
//                    toNumbers.removeAll(needRemove);
//                }
//                scenario.setToList(toNumbers);
//            }
//
//            if (destinationType != null) {
//                scenario.setDestinationType(destinationType);
//            } else {
//                return new Message(Message.Status.Error, "Destination type is wrong or empty").toString();
//            }
//
//            if (forwardType != null) {
//                scenario.setForwardType(forwardType);
//            } else {
//                return new Message(Message.Status.Error, "Forward type is wrong or empty").toString();
//            }
//
//            if (days != null) {
//                scenario.setDays(days);
//            } else {
//                scenario.setDays(new boolean[]{true, true, true, true, true, true, true});
//            }
//
//            if (awaitingTime != 0) {
//                scenario.setAwaitingTime(awaitingTime);
//            } else {
//                scenario.setAwaitingTime(600);
//            }
//
//            if (endHour != null) {
//                scenario.setEndHour(endHour);
//            } else {
//                scenario.setEndHour(24);
//            }
//
//            if (startHour != null) {
//                scenario.setStartHour(startHour);
//            } else {
//                scenario.setStartHour(0);
//            }
//
//            if (melody != null) {
//                scenario.setMelody(melody);
//            } else {
//                scenario.setMelody("none");
//            }
//
//            scenario.setStatus(ScenarioStatus.DEACTIVATED);
//            scenario.setUser(user);
//
//            try {
//                user.addScenario(scenario);
//                HibernateDao.update(user);
//                return new Message(Message.Status.Success, "Scenario added").toString();
//            } catch (ScenarioConflictException e) {
//                LOGGER.debug("Ошибка добавления сценария в модель: " + e.getMessage());
//                return new Message(Message.Status.Error, e.getMessage()).toString();
//            } catch (Exception e) {
//                LOGGER.error("Ошибка добавления сценария " + scenario, e);
//                return new Message(Message.Status.Error, "Internal error").toString();
//            } finally {
//                if (safeMode)
//                    user.reloadScenariosFromDb();
//            }
//        }
//    }
}