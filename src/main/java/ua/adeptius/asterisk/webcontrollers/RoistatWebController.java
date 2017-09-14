package ua.adeptius.asterisk.webcontrollers;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.JsonRoistatForController;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.RoistatAccount;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;

@Controller
@ResponseBody
@RequestMapping(value = "/roistat", produces = "application/json; charset=UTF-8")
public class RoistatWebController {

    private static boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(RoistatWebController.class.getSimpleName());


    @PostMapping("/get")
    public Object get(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }
        if (user.getAmoAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected Roistat account");
        }

        try {
            return user.getRoistatAccount();
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка получения Roistat аккаунта: ", e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }


    @PostMapping("/set")
    public Message set(@RequestBody JsonRoistatForController jsonRoistat, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String projectNumber = jsonRoistat.getProjectNumber();
        String apiKey = jsonRoistat.getApiKey();

        if (StringUtils.isAnyBlank(projectNumber, apiKey)) {
            return new Message(Message.Status.Error, "Some params are blank!");
        }

        RoistatAccount roistatAccount = user.getRoistatAccount();

        if (roistatAccount == null) {
            roistatAccount = new RoistatAccount();
        }

        roistatAccount.setProjectNumber(projectNumber);
        roistatAccount.setApiKey(apiKey);
        user.setRoistatAccount(roistatAccount);

        try {
            HibernateController.update(user);
            return new Message(Message.Status.Success, "Roistat account setted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка изменения Roistat аккаунта: ", e);
            return new Message(Message.Status.Error, "Internal error");
        } finally {
            if (safeMode) {

            }
        }
    }

    @PostMapping("/test")
    public Message check(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        if (user.getRoistatAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected roistat account");
        }

        String projectNumber = user.getRoistatAccount().getProjectNumber();
        String userApiKey = user.getRoistatAccount().getApiKey();

        try {
            boolean allOk = testRoistat(projectNumber, userApiKey);
            if (allOk) {
                return new Message(Message.Status.Success, "Check complete. It works!");
            } else {
                return new Message(Message.Status.Error, "Project number or API key is wrong");
            }
        } catch (Exception e) {
            LOGGER.error("Неизвестная ошибка при проверке аккаунта roistat. Аккаунт nextel: " + user.getLogin(), e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }

    @PostMapping("/remove")
    public Message remove(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        if (user.getRoistatAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected Roistat account");
        }

        try {
            user.setRoistatAccount(null);
            HibernateController.update(user);
            return new Message(Message.Status.Success, "Roistat account removed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления Roistat аккаунта.", e);
            return new Message(Message.Status.Error, "Internal error");
        } finally {
            if (safeMode) {

            }
        }
    }

    public static boolean testRoistat(String projectNumber, String apiKey) throws Exception {
        HttpResponse<String> response = Unirest
                .post("https://cloud.roistat.com/api/v1/project/phone-call?project=" + projectNumber + "&key=" + apiKey)
                .header("content-type", "application/json")
                .asString();
        String body = response.getBody();
        LOGGER.trace("Ответ от roistat при проверке учетных данных: {}", body);
        JSONObject object = new JSONObject(body);
        String status = object.getString("status");
        if ("error".equals(status)) {
            String error = object.getString("error");
            if (error.startsWith("incorrect_request")) {
                return false;
            } else if (error.startsWith("request_data_validation_error")) {
                return true;
            }
        }
        return false;
    }
}
