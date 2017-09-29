package ua.adeptius.asterisk.dao;


import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.OuterPhone;
import ua.adeptius.asterisk.model.telephony.Rule;
import ua.adeptius.asterisk.model.telephony.Scenario;
import ua.adeptius.asterisk.monitor.AgiInProcessor;
import ua.adeptius.asterisk.monitor.Scheduler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RulesConfigDAO {

    private static Logger LOGGER = LoggerFactory.getLogger(RulesConfigDAO.class.getSimpleName());

    private static String folder = Main.settings.getFolderRules();


    @SuppressWarnings("Duplicates")
    public static void writeAllNeededScenarios() throws Exception {

        // Список телефонов, на которых вообще назначены какие либо сценарии.
        List<OuterPhone> outerPhonesWithScenario = UserContainer.getUsers().stream()
                .flatMap(user -> user.getOuterPhones().stream())
                .filter(phone -> phone.getScenarioId() != null)
                .collect(Collectors.toList());

        // Сюда будем ложить айдишки сценариев и их список телефонов
        HashMap<Integer, ArrayList<OuterPhone>> scenarioIdAndPhones = new HashMap<>();

        LOGGER.debug("Всего номеров со сценариями: {}. Задействованных сценариев: {}",
                outerPhonesWithScenario.size(), scenarioIdAndPhones.size());

        for (OuterPhone outerPhone : outerPhonesWithScenario) { // собственно наполняем мапу айдишками сценариев и соответствующих телефонов
            Integer scenarioId = outerPhone.getScenarioId();
            ArrayList<OuterPhone> outerPhones = scenarioIdAndPhones.get(scenarioId);
            if (outerPhones == null) {
                ArrayList<OuterPhone> list = new ArrayList<>();
                list.add(outerPhone);
                scenarioIdAndPhones.put(scenarioId, list);
            } else {
                outerPhones.add(outerPhone);
            }
        }

        List<Scenario> scenarios = UserContainer.getUsers().stream() // Получаем список всех сценариев, айдишки которых
                .flatMap(user -> user.getScenarios().stream())       // имеются в мапе
                .filter(scenario -> scenarioIdAndPhones.containsKey(scenario.getId()))
                .collect(Collectors.toList());

        RulesConfigDAO.clearRulesFolder();

        Calendar calendar = new GregorianCalendar();
        long currentMillis = calendar.getTimeInMillis();
        long newTimeMillis = currentMillis + (3600000); // Просчитываем следующий час
        calendar.setTimeInMillis(newTimeMillis);


        int day = LocalDate.now().getDayOfWeek().getValue();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        LOGGER.debug("Ищем правила на {} день недели, {} час", day, hour);

        // Тут нужна какая-то мапа <номер телефона, правило>
        HashMap<String, Rule> phoneNumbersAndRules = new HashMap<>();

        for (Scenario scenario : scenarios) { // проходимся по всем сценариям и проверяем какие из них можно активировать на следующий час.
            Rule rule = scenario.getRuleByTime(day, hour);
            if (rule == null) {
                LOGGER.error("{}: при поиске активного правила в сценарии {} на {} день, {} час - вернулся null",
                        scenario.getLogin(), scenario.getName(), day, hour);
                continue;
            }

            List<String> fromNumbers = scenarioIdAndPhones.get(scenario.getId()).stream()
                    .map(OuterPhone::getNumber)
                    .collect(Collectors.toList());

            for (String fromNumber : fromNumbers) { // Наполняем мапу для AGI
                phoneNumbersAndRules.put(fromNumber, rule);
            }

            try {
                RulesConfigDAO.writeToFile(rule, fromNumbers);

            } catch (IOException e) {
                LOGGER.error(scenario.getLogin() + ": не удалось записать сценарий (id=" + scenario.getId() + ") в файл", e);
            }
        }

        AgiInProcessor.setPhoneNumbersAndRules(phoneNumbersAndRules); // меняем ссылку на новую мапу
    }


    public static void writeUsersRuleFile(User user){

        Set<OuterPhone> outerPhonesWithScenarios = user.getOuterPhones().stream()
                .filter(outerPhone -> outerPhone.getScenarioId() != null)
                .collect(Collectors.toSet());

        // Сюда будем ложить айдишки сценариев и их список телефонов
        HashMap<Integer, ArrayList<OuterPhone>> scenarioIdAndPhones = new HashMap<>();

        for (OuterPhone outerPhone : outerPhonesWithScenarios) {
            Integer scenarioId = outerPhone.getScenarioId();


            ArrayList<OuterPhone> outerPhones = scenarioIdAndPhones.computeIfAbsent(scenarioId, k -> new ArrayList<>());
            outerPhones.add(outerPhone);
        }

        try {
            removeRuleFile(user.getLogin());
        } catch (IOException e) {
            LOGGER.error(user.getLogin() + ": не удалось удалить файл конфига для живой замены", e);
            return;
        }

        for (Map.Entry<Integer, ArrayList<OuterPhone>> entry : scenarioIdAndPhones.entrySet()) {
            Integer scenarioId = entry.getKey();
            List<String> numbers = entry.getValue().stream().map(OuterPhone::getNumber).collect(Collectors.toList());

            Scenario scenario = user.getScenarioById(scenarioId);
            if (scenario == null) {
                LOGGER.warn("{}: при живой замене конфигов сценарий с id {} не нашелся. Сценарии: {}",
                        user.getLogin(), scenarioId, user.getScenarios());
                return;
            }

            Rule rule = scenario.getRuleForNow();

            try {
                writeToFile(rule, numbers);
            } catch (IOException e) {
                LOGGER.error(scenario.getLogin() + ": не удалось записать сценарий (id=" + scenario.getId() + ") в файл", e);
            }
        }
    }



    public static void removeRuleFile(String name) throws IOException {
        Files.deleteIfExists(Paths.get(folder + name + ".conf"));
    }

    public static void clearRulesFolder() throws IOException {
        Collection<File> files = FileUtils.listFiles(new File(folder), null, false);
        files.forEach(file -> {
            String name = file.getName();
            if (!name.equals("binotel.conf") && !name.contains("test")) {
                try {
                    FileUtils.forceDelete(new File(folder + name));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
//        FileUtils.cleanDirectory(new File(folder));
    }


    public static void writeToFile(Rule rule, List<String> fromNumbers) throws IOException {
//        LOGGER.trace("{}: запись сценария в файл", rule.getLogin());
        String filename = rule.getLogin() + ".conf";
        BufferedWriter writer = new BufferedWriter(new FileWriter(folder + filename, true));
        writer.write(rule.getConfig(fromNumbers));
        writer.close();
    }
}
