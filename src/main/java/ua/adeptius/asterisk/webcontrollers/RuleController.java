package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.model.Customer;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.controllers.MainController;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.telephony.DestinationType.*;

@Controller
@RequestMapping("/rules")
public class RuleController {


    @RequestMapping(value = "/getAvailableNumbers", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getAvailableNumbers(@RequestParam String name, @RequestParam String password) {

        if (!MainController.isLogin(name, password)) {
            return "Error: wrong password";
        }

        Customer customer;
        try {
            customer = MainController.getUserByName(name);
        } catch (NoSuchElementException e) {
            return "Error: no such user";
        }

        return new Gson().toJson(customer.getAvailableNumbers());
    }


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getRules(@RequestParam String name, @RequestParam String password) {

        if (!MainController.isLogin(name, password)) {
            return "Error: wrong password";
        }

        Customer customer;
        try {
            customer = MainController.getUserByName(name);
        } catch (NoSuchElementException e) {
            return "Error: no such user";
        }

        List<Rule> rules = customer.getRules();
        return new Gson().toJson(rules);
    }


    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getRules(@RequestParam String name, @RequestParam String password, @RequestParam String rules) {
        if (!MainController.isLogin(name, password)) {
            return "Error: wrong password";
        }

        Customer customer;
        try {
            customer = MainController.getUserByName(name);
        } catch (NoSuchElementException e) {
            return "Error: no such user";
        }

        ArrayList<Rule> rulesList;
        try {
            Type listType = new TypeToken<ArrayList<Rule>>() {
            }.getType();
            rulesList = new Gson().fromJson(rules, listType);
        } catch (Exception e) {
            return "Error: wrong syntax";
        }

        // Проверяем есть ли дубликаты среди номеров с
        Set<String> filterSet = new HashSet<>();
        if (rulesList.stream().flatMap(rule -> rule.getFrom().stream()).anyMatch(s -> !filterSet.add(s))) {
            return "Error: duplicates numbers found";
        }

        // Проверяем правильный ли формат номеров
        List<String> numbersTo = rulesList.stream().filter(rule -> rule.getDestinationType() == GSM)
                .flatMap(rule -> rule.getTo().stream()).collect(Collectors.toList());

        for (String s : numbersTo) {
            Matcher regexMatcher = Pattern.compile("^0\\d{9}$").matcher(s);
            if (!regexMatcher.find()) {
                return "Error: wrong GSM number format. Must be 0xxxxxxxxx. Regex: ^0\\d{9}$";
            }
        }

        try {
            customer.setRules(rulesList);
            customer.saveRules();
            return "Success: Saved";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: DB error";
        }
    }
}
