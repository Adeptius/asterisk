package ua.adeptius.asterisk.webcontrollers;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.JsonUser;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.regex.Pattern;

@Controller
@ResponseBody
@RequestMapping(value = "/user", produces = "application/json; charset=UTF-8")
public class UserWebController {

    private static Logger LOGGER = LoggerFactory.getLogger(UserWebController.class.getSimpleName());

    private static Pattern emailRegex = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|" +
            "}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x" +
            "0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5" +
            "]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x" +
            "01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");


    /**
     * Только для меня. В документации отсутствует.
     */
    @PostMapping("/add") // дата регистрации пользователя и дата последнего посещения или как-то так
    public Object addUser(@RequestBody JsonUser jsonUser) {
        String login = jsonUser.getLogin();

        User currentUser = UserContainer.getUserByName(login);
        if (currentUser != null) {
            return new Message(Message.Status.Error, "Login is busy");
        }

        String name = login;
        if (name == null || name.length() < 4 || !MyStringUtils.validateThatContainsOnlyEngLettersAndNumbers(name)) {
            return new Message(Message.Status.Error, "Invalid name, or too short");
        }

        if (login.contains("test")) {
            return new Message(Message.Status.Error, "Invalid name. Cant use 'test' in name");
        } // это ограничение связано с правилами переадресации, которые нельзя удалять если в них содержится слово test

        String password = jsonUser.getPassword();
        if (password == null || password.length() < 6) {
            return new Message(Message.Status.Error, "Password is too short. Must be minimum 6 characters");
        }

        String email = jsonUser.getEmail();
        if (email == null || !emailRegex.matcher(email).find()){
            return new Message(Message.Status.Error, "Email is wrong.");
        }

        try {
            RootWebController.clearCache();
            User newUser = new User(login, jsonUser.getPassword(), email, jsonUser.getTrackingId());
            HibernateController.saveUser(newUser);
            UserContainer.putUser(newUser);
            return new Message(Message.Status.Success, "User created");
        } catch (Exception e) {
            LOGGER.error("Ошибка создания нового пользователя " + jsonUser, e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }


    @PostMapping("/set")
    public Object getUser(@RequestBody JsonUser setUser, HttpServletRequest request) {
        String oldHash = request.getHeader("Authorization");
        User user = UserContainer.getUserByHash(oldHash);
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String password = setUser.getPassword();
        if (StringUtils.isBlank(password) || password.length() < 6) {
            return new Message(Message.Status.Error, "Password is too short. Must be minimum 6 characters");
        }
        user.setPassword(password);


        String email = setUser.getEmail();
        if (email == null || !emailRegex.matcher(email).find()){
            return new Message(Message.Status.Error, "Email is wrong.");
        }
        user.setEmail(email);

        user.setTrackingId(setUser.getTrackingId());

        try {
            HibernateController.update(user);
            UserContainer.recalculateHashesForUser(oldHash, user);
            return new Message(Message.Status.Success, "User changed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка указания новых данных пользователя: " + setUser, e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }


    @PostMapping("/get")
    public Object getUser(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        return new JsonUser(user);
    }

    @PostMapping("/getFull")
    public Object getFullUser(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        return user;
    }

    @PostMapping("/remove")
    public Object remove(HttpServletRequest request) {
        String hash = request.getHeader("Authorization");
        User user = UserContainer.getUserByHash(hash);
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        try {
            RootWebController.clearCache();
            HibernateController.delete(user);
            UserContainer.removeUser(user);
            UserContainer.getHashes().remove(hash);
            PhonesController.removeAllInnerNumbersConfigFiles(user);
            return new Message(Message.Status.Success, "User removed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления пользователя", e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }
}
