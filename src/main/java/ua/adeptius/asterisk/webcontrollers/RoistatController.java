package ua.adeptius.asterisk.webcontrollers;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.AmoAccountNotFoundException;
import ua.adeptius.amocrm.exceptions.AmoCantCreateDealException;
import ua.adeptius.amocrm.exceptions.AmoUnknownException;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyExeption;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.json.JsonAmoForController;
import ua.adeptius.asterisk.json.JsonRoistatForController;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.RoistatAccount;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/roistat")
public class RoistatController {

    private static boolean safeMode = true;
    private static Logger LOGGER =  LoggerFactory.getLogger(RoistatController.class.getSimpleName());

    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String get(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getAmoAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected Roistat account").toString();
        }

        try {
            return new ObjectMapper().writeValueAsString(user.getRoistatAccount());
        }catch (Exception e){
            LOGGER.error(user.getLogin()+": ошибка получения Roistat аккаунта: ", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String set(@RequestBody JsonRoistatForController jsonRoistat, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        String projectNumber = jsonRoistat.getProjectNumber();
        String apiKey = jsonRoistat.getApiKey();

        if (StringUtils.isAnyBlank(projectNumber, apiKey)) {
            return new Message(Message.Status.Error, "Some params are blank!").toString();
        }

        if (user.getRoistatAccount() == null) {
            user.setRoistatAccount(new RoistatAccount());
            user.getRoistatAccount().setUser(user);
            user.getRoistatAccount().setNextelLogin(user.getLogin());
        }

        RoistatAccount roistatAccount = user.getRoistatAccount();
        roistatAccount.setProjectNumber(projectNumber);
        roistatAccount.setApiKey(apiKey);

        try {
            HibernateController.updateUser(user);
            return new Message(Message.Status.Success, "Roistat account setted").toString();
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка изменения Roistat аккаунта: ", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        } finally {
            if (safeMode)
                user.reloadRoistatAccountFromDb();
        }
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String check(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        if (user.getRoistatAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected roistat account").toString();
        }

        String projectNumber = user.getRoistatAccount().getProjectNumber();
        String userApiKey = user.getRoistatAccount().getApiKey();

        try {
            boolean allOk = testRoistat(projectNumber, userApiKey);
            if (allOk){
                return new Message(Message.Status.Success, "Check complete. It works!").toString();
            }else {
                return new Message(Message.Status.Error, "Project number or API key is wrong").toString();
            }
        }catch (Exception e){
            LOGGER.error("Неизвестная ошибка при проверке аккаунта roistat. Аккаунт nextel: " + user.getLogin(), e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String remove(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        if (user.getRoistatAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected Roistat account").toString();
        }

        try {
            HibernateController.removeRoistatAccount(user);
            return new Message(Message.Status.Success, "Roistat account removed").toString();
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка удаления Roistat аккаунта.", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        } finally {
            if (safeMode)
                user.reloadRoistatAccountFromDb();
        }
    }

    public static boolean testRoistat(String projectNumber, String apiKey) throws Exception{
        HttpResponse<String> response = Unirest
                .post("https://cloud.roistat.com/api/v1/project/phone-call?project=" + projectNumber + "&key=" + apiKey)
                .header("content-type", "application/json")
                .asString();
        String body = response.getBody();
        LOGGER.trace("Ответ от roistat при проверке учетных данных: {}", body);
        JSONObject object = new JSONObject(body);
        String status = object.getString("status");
        if ("error".equals(status)){
            String error = object.getString("error");
            if (error.startsWith("incorrect_request")){
                return false;
            } else if (error.startsWith("request_data_validation_error")){
                return true;
            }
        }
        return false;
    }
}
