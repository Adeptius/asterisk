package ua.adeptius.asterisk.dao;


import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.Scenario;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RulesConfigDAO {

    private static Logger LOGGER = LoggerFactory.getLogger(RulesConfigDAO.class.getSimpleName());

    private static String folder = Settings.getSetting("forwardingRulesFolder");


    public static void writeAllNeededScenarios() {
        LOGGER.info("Подготовка сценариев на следующий час");
//        List<Scenario> scenarios = UserContainer.getUsers().stream().flatMap(user -> user.getScenarios().stream())
//                .filter(scenario -> scenario.getStatus() == ScenarioStatus.ACTIVATED)
//                .collect(Collectors.toList());
        try {
            RulesConfigDAO.clearRulesFolder();
        } catch (IOException e) {
            LOGGER.error("Ошибка очистки папки сценариев", e);
        }


        Calendar calendar = new GregorianCalendar();

        long currentMillis = calendar.getTimeInMillis();
        long newTimeMillis = currentMillis + (10 * 1000 * 60); // раз в 10 минут
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

        LOGGER.debug("Ищем сценарии на {} день (нумерация с нуля) недели, {} час", day, hour);

//        for (Scenario scenario : scenarios) { // проходимся по всем сценариям и проверяем какие из них можно активировать на следующий час.
//            boolean[] days = scenario.getDays();
//            int startTime = scenario.getStartHour();
//            int endTime = scenario.getEndHour();
//
//            if (!days[day]) {
//                LOGGER.trace("Не тот день для сценария {}", scenario);
//                continue;
//            }
//
//            if (startTime > hour) {
//                LOGGER.trace("Не наступило время сценария {}", scenario);
//                continue;
//            }
//
//            if (endTime <= hour) {
//                LOGGER.trace("Прошло время сценария {}", scenario);
//                continue;
//            }
//
//            try {
//                RulesConfigDAO.writeToFile(scenario);
//            } catch (IOException e) {
//                LOGGER.error(scenario.getLogin() + ": не удалось записать сценарий (id=" + scenario.getId() + ") в файл", e);
//            }
//        }
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


    public static void writeToFile(Scenario scenario) throws IOException {
        LOGGER.trace("{}: запись сценария в файл", scenario.getLogin());
        String filename = scenario.getLogin() + "-" + scenario.getId() + ".conf";
        BufferedWriter writer = new BufferedWriter(new FileWriter(folder + filename));
        writer.write(scenario.getConfig());
        writer.close();
    }


//    public static void writeToFile(String filename, List<OldRule> oldRuleList) throws Exception {
//        LOGGER.trace("{}: запись правил в файл", filename);
//        BufferedWriter writer = new BufferedWriter(new FileWriter(folder + filename + ".conf"));
//        for (OldRule oldRule : oldRuleList) {
//            writer.write(oldRule.getConfig());
//        }
//        writer.close();
//    }

//    public static List<OldRule> readFromFile(String filename) throws Exception {
//        LOGGER.trace("{}: чтение правил из файла", filename);
//        List<String> lines = readStringsFromFile(folder + filename + ".conf");
//        List<OldRule> oldRules = new ArrayList<>();
//        List<String> linesOfRule = new ArrayList<>();
//        for (String line : lines) {
//            if (line.startsWith("; Start Rule")) {
//                linesOfRule.clear();
//            } else if (line.startsWith("; End Rule")) {
//                oldRules.add(new OldRule(linesOfRule));
//            } else {
//                linesOfRule.add(line);
//            }
//        }
//        return oldRules;
//    }


//    public static List<String> readStringsFromFile(String path) throws Exception {
//        List<String> fileEnrty = Files.readAllLines(Paths.get(path));
//        List<String> filteredEntry = new ArrayList<>();
//        for (String s : fileEnrty) {
//            if (!s.endsWith(",1,Noop(${CALLERID(num)})")
//                    && !s.endsWith(",n,Gosub(sub-record-check,s,1(in,${EXTEN},force))")
//                    && !s.endsWith(",n,Set(__FROM_DID=${EXTEN})")
//                    && !s.endsWith(",n,Set(CDR(did)=${FROM_DID})")
//                    && !s.endsWith(",n,Set(num=${CALLERID(num)})")
//                    && !s.equals("\n")
//                    && !s.equals("")
//                    ) {
//                filteredEntry.add(s);
//            }
//        }
//        return filteredEntry;
//    }

    public static void removeFile(String name) throws Exception {
        LOGGER.trace("{}: удаление файла правил", name);
        Files.deleteIfExists(Paths.get(folder + name + ".conf"));
    }

//    public static void removeFileIfNeeded(User user) throws Exception {
//        List<String> customerNumbers = new ArrayList<>();
//        List<OldRule> rulesToDelete = new ArrayList<>();
//        List<OldRule> currentOldRules = user.getOldRules();
//
//        if (user.getTracking() != null) {
//            customerNumbers.addAll(user.getTracking().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList()));
//        }
//        if (user.getTelephony() != null) {
//            customerNumbers.addAll(user.getTelephony().getInnerPhonesList());
//            customerNumbers.addAll(user.getTelephony().getOuterPhonesList());
//        }
//
//        for (OldRule oldRule : currentOldRules) {
//            List<String> numbersInRules = new ArrayList<>();
//            numbersInRules.addAll(oldRule.getFrom());
////            numbersInRules.addAll(rule.getTo());
//
//            for (String s : numbersInRules) {
//                if (!customerNumbers.contains(s)) {
//                    rulesToDelete.add(oldRule);
//                    break;
//                }
//            }
//        }
//
//        currentOldRules.removeAll(rulesToDelete);
//
//        if (currentOldRules.size() == 0) {
//            Files.deleteIfExists(Paths.get(folder + user.getLogin() + ".conf"));
//        } else if (rulesToDelete.size() > 0) {
//            user.saveRules();
//        }
//    }
}
