package ua.adeptius.asterisk.dao;


import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.OuterPhone;
import ua.adeptius.asterisk.model.Rule;
import ua.adeptius.asterisk.model.Scenario;
import ua.adeptius.asterisk.test.AgiInProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RulesConfigDAO {

    private static Logger LOGGER = LoggerFactory.getLogger(RulesConfigDAO.class.getSimpleName());

    private static String folder = Settings.getSetting("folder.rules");


    public static void writeAllNeededScenarios() throws Exception {
        LOGGER.info("Подготовка сценариев на следующий час");

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

        int dayByCalendar = calendar.get(Calendar.DAY_OF_WEEK);

        int day = 0; // День. Понедельник = 0
        if (dayByCalendar == Calendar.TUESDAY) {
            day = 1;
        } else if (dayByCalendar == Calendar.WEDNESDAY) {
            day = 2;
        } else if (dayByCalendar == Calendar.THURSDAY) {
            day = 3;
        } else if (dayByCalendar == Calendar.FRIDAY) {
            day = 4;
        } else if (dayByCalendar == Calendar.SATURDAY) {
            day = 5;
        } else if (dayByCalendar == Calendar.SUNDAY) {
            day = 6;
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Час дня. час ночи = 1

        LOGGER.debug("Ищем правила на {} день (нумерация с нуля) недели, {} час", day, hour);

        // Тут нужна какая-то мапа <номер телефона, правило>
        HashMap<String, Rule> phoneNumbersAndRules = new HashMap<>();

        for (Scenario scenario : scenarios) { // проходимся по всем сценариям и проверяем какие из них можно активировать на следующий час.
            Rule rule = scenario.getRuleByTime(day, hour);
            if (rule == null) {
                LOGGER.error("{}: при поиске активного правила на {} день, {} час - вернулся null", scenario.getLogin(), day, hour);
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
