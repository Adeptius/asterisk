package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Email;
import ua.adeptius.asterisk.model.RegisterQuery;
import ua.adeptius.asterisk.model.User;

import java.util.regex.Pattern;

import static ua.adeptius.asterisk.json.Message.Status.Error;
import static ua.adeptius.asterisk.json.Message.Status.Success;

@Controller
@RequestMapping(value = "/registration", produces = "application/json; charset=UTF-8")
@ResponseBody
public class RegistrationWebController {

    private static Logger LOGGER = LoggerFactory.getLogger(RegistrationWebController.class.getSimpleName());

    private static Pattern emailRegex = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|" +
            "}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x" +
            "0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5" +
            "]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x" +
            "01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");



    @PostMapping("/register")
        public Object register(String login, String email, String password) {
        LOGGER.info("Запрос регистрации для {}, email: {}, длинна пароля: {}", login, email, password.length());

        // Проверка логина
        if (login.length() < 4) {
            LOGGER.info("Логин {} - короткий", login);
            return new Message(Error, "Login is short");
        }

        String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < login.length(); i++) {
            String s = login.substring(i, i + 1);
            if (!str.contains(s)) {
                LOGGER.info("Логин {} содержит спец символы", login);
                return new Message(Error, "Login contains non-eng symbols");
            }
        }

        User user = UserContainer.getUserByName(login);
        if (user != null) {
            LOGGER.info("Логин {} занят", login);
            return new Message(Error, "Login is busy");
        }

        if (!emailRegex.matcher(email).find()) {
            LOGGER.info("Email {} не правильный", email);
            return new Message(Error, "Email is wrong");
        }

        user = UserContainer.getUserByEmail(email); // todo проверить
        if (user != null) {
            LOGGER.info("Email {} уже зарегистрирован", email);
            return new Message(Error, "Email already registered");
        }

//        if (UserContainer.pendingUsers.stream().anyMatch(pu -> pu.getEmail().equals(email))) {// todo проверить
//            LOGGER.info("На еmail {} уже было отправлено письмо", email);
//            return new Message(Error, "Email was sended already");
//        }

        if (password.length() < 20) {
            return new Message(Error, "Password lenth less than 20");
        }

        String str2 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < password.length(); i++) {
            String s = password.substring(i, i + 1);
            if (!str2.contains(s)) {
                return new Message(Error, "Password contains non-eng symbols");
            }
        }

        RegisterQuery registerQuery = new RegisterQuery(login, password, email);
        try {
            HibernateController.saveOrUpdate(registerQuery);
            Main.emailSender.send(new Email(registerQuery));
            LOGGER.info("Ключ отправлен на {}", email);
            return new Message(Success, "Key sended to " + email);
        }catch (Exception e){
            LOGGER.error(login + " ошибка сохранения запроса на регистрацию", e);
            return new Message(Error, "Internal error");
        }
    }

    @PostMapping("/key")
    public Object registerConfirm(String key) {
       RegisterQuery registerQuery = HibernateController.getRegisterQueryByKey(key);
       if (registerQuery ==null){
           LOGGER.info("Ключ {} неправильный или просроченный", key);
           return new Message(Error, "Key wrong or expired");
       }
       try {
           User newUser = new User(registerQuery.getLogin(), registerQuery.getPassword(), registerQuery.getEmail(), null);
           HibernateController.saveUser(newUser);
           UserContainer.putUser(newUser);
           HibernateController.removeRegisterQueryByLogin(registerQuery.getLogin());
           LOGGER.info("Пользователь {} успешно зарегистрирован", registerQuery.getLogin());
           return new Message(Success, UserContainer.createMd5(newUser)); // возвращаем токен, что бы сразу авторизоватся
       }catch (Exception e){
           LOGGER.error("Ошибка создания пользователя из RegisterQuery, login " + registerQuery.getLogin(), e);
           return new Message(Error, "Internal error");
       }
    }
    // todo Восстановление пароля
}
