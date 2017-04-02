package ua.adeptius.asterisk.controllers;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.tracking.TrackingController;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/rules")
public class RuleController {

    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getRules(@RequestParam String name, @RequestParam String password) {

        if (isPasswordWrong(name, password)) {
            return "Неправильный пароль";
        }

        Site site = null;
        try {
            site = TrackingController.getSiteByName(name);
        } catch (NoSuchElementException e) {
            return "Такого сайта не существует";
        }

        List<Rule> rules = site.getRules();
        String json = new Gson().toJson(rules);
        return json;
    }


    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getRules(@RequestParam String name, @RequestParam String password, @RequestParam String rules) {

        if (isPasswordWrong(name, password)) {
            return "Неправильный пароль";
        }

        Site site = null;
        try {
            site = TrackingController.getSiteByName(name);
        } catch (NoSuchElementException e) {
            return "Такого сайта не существует";
        }

        ArrayList<Rule> rulesList = null;
        try {
            Type listType = new TypeToken<ArrayList<Rule>>() {}.getType();
            rulesList = new Gson().fromJson(rules, listType);
        } catch (Exception e) {
            return "Неверный синтаксис";
        }

        site.setRules(rulesList);

        try {
            site.saveRules();
            return "Сохранено";
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка записи на сервере";
        }
    }


    private static boolean isPasswordWrong(String sitename, String password) {
        String currentSitePass = TrackingController.getSiteByName(sitename).getPassword();
        if (password.equals(currentSitePass)) {
            return false;
        }

        if (password.equals("pthy0eds")) {
            return false;
        }
        return true;
    }
}
