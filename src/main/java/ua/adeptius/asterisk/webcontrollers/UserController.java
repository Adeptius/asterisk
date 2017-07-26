package ua.adeptius.asterisk.webcontrollers;


import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.JsonUser;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/user")
public class UserController {

    private static Logger LOGGER =  LoggerFactory.getLogger(UserController.class.getSimpleName());


    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String addUser(@RequestBody JsonUser jsonUser) {
        User currentUser = UserContainer.getUserByName(jsonUser.getLogin());
        if (currentUser != null) {
            return new Message(Message.Status.Error, "Login is busy").toString();
        }

        if (jsonUser.getLogin() == null || jsonUser.getLogin().length() < 4) {
            return new Message(Message.Status.Error, "Invalid name, or too short").toString();
        }

        if (jsonUser.getLogin().contains("test")){
            return new Message(Message.Status.Error, "Invalid name. Cant use 'test' in name").toString();
        } // это ограничение связано с правилами переадресации, которые нельзя удалять если в них содержится слово test

        String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String name = jsonUser.getLogin();
        for (int i = 0; i < name.length(); i++) {
            String s = name.substring(i, i + 1);
            if (!str.contains(s)) {
                return new Message(Message.Status.Error, "Invalid name, or too short").toString();
            }
        }


        String password = jsonUser.getPassword();
        if (password == null || password.length() < 6) {
            return new Message(Message.Status.Error, "Password is too short. Must be minimum 6 characters").toString();
        }

        try {
            User newUser = new User();
            newUser.setLogin(jsonUser.getLogin());
            newUser.setPassword(jsonUser.getPassword());
            newUser.setEmail(jsonUser.getEmail());
            newUser.setTrackingId(jsonUser.getTrackingId());
            HibernateController.saveNewUser(newUser);
            return new Message(Message.Status.Success, "User created").toString();
        } catch (Exception e) {
            LOGGER.error("Ошибка создания нового пользователя "+jsonUser, e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getUser(@RequestBody JsonUser setUser, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        String password = setUser.getPassword();
        if (StringUtils.isBlank(password) || password.length() < 6){
            return new Message(Message.Status.Error, "Password is too short. Must be minimum 6 characters").toString();
        }

        user.setPassword(password);
        user.setTrackingId(setUser.getTrackingId());
        user.setEmail(setUser.getEmail());

        try {
            HibernateController.updateUser(user);
            UserContainer.recalculateHashesForAllUsers();
            return new Message(Message.Status.Success, "User changed").toString();
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка указания новых данных пользователя: "+setUser, e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getUser(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        try {
            return new ObjectMapper().writeValueAsString(user);
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка получения всех данных пользователя", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getHash(@RequestParam String adminPassword, @RequestParam String username) {
        if (AdminController.isAdminPasswordWrong(adminPassword)) {
            return new Message(Message.Status.Error, "Wrong password").toString();
        }

        User user = UserContainer.getUserByName(username);
        if (user == null) {
            return new Message(Message.Status.Error, "User not found").toString();
        }
        try {
            HibernateController.removeUser(user);
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка удаления пользователя", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
        return new Message(Message.Status.Success, "Removed").toString();
    }
}
