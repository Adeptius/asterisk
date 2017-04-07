package ua.adeptius.asterisk.controllers;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.model.Customer;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.tracking.MainController;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/rules")
public class RuleController {



    @RequestMapping(value = "/getAvailableNumbers", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getAvailableNumbers(@RequestParam String name, @RequestParam String password) {

        if (!MainController.isLogin(name, password)) {
            return "Error: wrong password";
        }

        Customer customer = null;
        try {
            customer = MainController.getCustomerByName(name);
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

        Customer customer = null;
        try {
            customer = MainController.getCustomerByName(name);
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

        Customer customer = null;
        try {
            customer = MainController.getCustomerByName(name);
        } catch (NoSuchElementException e) {
            return "Error: no such user";
        }

        ArrayList<Rule> rulesList = null;
        try {
            Type listType = new TypeToken<ArrayList<Rule>>() {
            }.getType();
            rulesList = new Gson().fromJson(rules, listType);
        } catch (Exception e) {
            return "Error: wrong syntax";
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
