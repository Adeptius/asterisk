package ua.adeptius.asterisk.dao;


import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.telephony.Rule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RulesConfigDAO {

    private static String folder = Settings.getSetting("___forwardingRulesFolder");

    public static void writeToFile(String filename, List<Rule> ruleList) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(folder + filename + ".conf"));
        for (Rule rule : ruleList) {
            writer.write(rule.getConfig());
        }
        writer.close();
    }

    public static List<Rule> readFromFile(String filename) throws Exception {
        List<String> lines = readStringsFromFile(folder + filename + ".conf");
        List<Rule> rules = new ArrayList<>();
        List<String> linesOfRule = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("; Start Rule")) {
                linesOfRule.clear();
            } else if (line.startsWith("; End Rule")) {
                rules.add(new Rule(linesOfRule));
            } else {
                linesOfRule.add(line);
            }
        }
        return rules;
    }


    public static List<String> readStringsFromFile(String path) throws Exception {
        List<String> fileEnrty = Files.readAllLines(Paths.get(path));
        List<String> filteredEntry = new ArrayList<>();
        for (String s : fileEnrty) {
            if (!s.endsWith(",1,Noop(${CALLERID(num)})")
                    && !s.endsWith(",n,Gosub(sub-record-check,s,1(in,${EXTEN},force))")
                    && !s.endsWith(",n,Set(__FROM_DID=${EXTEN})")
                    && !s.endsWith(",n,Set(CDR(did)=${FROM_DID})")
                    && !s.endsWith(",n,Set(num=${CALLERID(num)})")
                    && !s.equals("\n")
                    && !s.equals("")
                    ) {
                filteredEntry.add(s);
            }
        }
        return filteredEntry;
    }

    public static void removeFile(String name) throws Exception {
        Files.deleteIfExists(Paths.get(folder + name + ".conf"));
    }

    public static void removeFileIfNeeded(User user) throws Exception {
        List<String> customerNumbers = new ArrayList<>();
        List<Rule> rulesToDelete = new ArrayList<>();
        List<Rule> currentRules = user.getRules();

        if (user.getTracking() != null){
            customerNumbers.addAll(user.getTracking().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList()));
        }
        if (user.getTelephony() != null) {
            customerNumbers.addAll(user.getTelephony().getInnerPhonesList());
            customerNumbers.addAll(user.getTelephony().getOuterPhonesList());
        }

        for (Rule rule : currentRules) {
            List<String> numbersInRules = new ArrayList<>();
            numbersInRules.addAll(rule.getFrom());
//            numbersInRules.addAll(rule.getTo());

            for (String s : numbersInRules) {
                if (!customerNumbers.contains(s)){
                    rulesToDelete.add(rule);
                    break;
                }
            }
        }

        currentRules.removeAll(rulesToDelete);

        if (currentRules.size() == 0){
            Files.deleteIfExists(Paths.get(folder + user.getLogin() + ".conf"));
        }else if (rulesToDelete.size() > 0){
            user.saveRules();
        }
    }
}
