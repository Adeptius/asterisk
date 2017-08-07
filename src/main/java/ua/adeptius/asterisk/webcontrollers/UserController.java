package ua.adeptius.asterisk.webcontrollers;


import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.json.JsonUser;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;

@Controller
@ResponseBody
@RequestMapping(value = "/user", produces = "application/json; charset=UTF-8")
public class UserController {

    private static Logger LOGGER = LoggerFactory.getLogger(UserController.class.getSimpleName());

    @PostMapping("/add") //todo дата регистрации пользователя и дата последнего посещения или как-то так
    public Object addUser(@RequestBody JsonUser jsonUser) {
        User currentUser = UserContainer.getUserByName(jsonUser.getLogin());
        if (currentUser != null) {
            return new Message(Message.Status.Error, "Login is busy");
        }

        if (jsonUser.getLogin() == null || jsonUser.getLogin().length() < 4) {
            return new Message(Message.Status.Error, "Invalid name, or too short");
        }

        if (jsonUser.getLogin().contains("test")) {
            return new Message(Message.Status.Error, "Invalid name. Cant use 'test' in name");
        } // это ограничение связано с правилами переадресации, которые нельзя удалять если в них содержится слово test


        String name = jsonUser.getLogin();
        if (!MyStringUtils.validateThatContainsOnlyEngLettersAndNumbers(name)) {
            return new Message(Message.Status.Error, "Invalid name, or too short");
        }


        String password = jsonUser.getPassword();
        if (password == null || password.length() < 6) {
            return new Message(Message.Status.Error, "Password is too short. Must be minimum 6 characters");
        }

        try {
            User newUser = new User();
            newUser.setLogin(jsonUser.getLogin());
            newUser.setPassword(jsonUser.getPassword());
            newUser.setEmail(jsonUser.getEmail());
            newUser.setTrackingId(jsonUser.getTrackingId());
            HibernateDao.saveUser(newUser);
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
        user.setTrackingId(setUser.getTrackingId());
        user.setEmail(setUser.getEmail());

        try {
            HibernateDao.update(user);
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
        try {
            return user;
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка получения всех данных пользователя", e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }

    @PostMapping("/remove")
    public Object remove(HttpServletRequest request) {
        String hash = request.getHeader("Authorization");
        User user = UserContainer.getUserByHash(hash);
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        try {
            HibernateDao.delete(user);
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
