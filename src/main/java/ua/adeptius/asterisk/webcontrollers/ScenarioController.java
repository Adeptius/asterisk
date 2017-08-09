package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.json.Message;

import java.util.Date;
import java.util.List;

@Deprecated
@Controller
@RequestMapping(value = "/scenario", produces = "application/json; charset=UTF-8")
@ResponseBody
public class ScenarioController {

//    private static boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(ScenarioController.class.getSimpleName());
//

    private static List<String> melodies;
    private static long melodiesTimeCache;

    private static void loadMelodies() throws Exception {
        melodies = HibernateDao.getMelodies();
        melodiesTimeCache = new Date().getTime();
    }

    @PostMapping("/getMelodies")
    public Object getHistory() {
        try {
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
        } catch (Exception e) {
            LOGGER.error("Ошибка получения списка мелодий", e);
            return new Message(Message.Status.Error, "Internal error").toString();
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