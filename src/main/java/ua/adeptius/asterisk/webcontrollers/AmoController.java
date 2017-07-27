package ua.adeptius.asterisk.webcontrollers;


import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.AmoAccountNotFoundException;
import ua.adeptius.amocrm.exceptions.AmoCantCreateDealException;
import ua.adeptius.amocrm.exceptions.AmoUnknownException;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyExeption;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.json.JsonAmoForController;
import ua.adeptius.asterisk.json.JsonTracking;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.CallProcessor;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/amo")
public class AmoController {

    private static boolean safeMode = true;
    private static Logger LOGGER =  LoggerFactory.getLogger(AmoController.class.getSimpleName());

    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String get(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return new Message(Message.Status.Error, "User have not connected amo account").toString();
        }

        try {
            return new ObjectMapper().writeValueAsString(amoAccount);
        }catch (Exception e){
            LOGGER.error(user.getLogin()+": ошибка получения амо аккаунта: ", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String set(@RequestBody JsonAmoForController jsonAmoAccount, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        String domain = jsonAmoAccount.getDomain();
        String amoLogin = jsonAmoAccount.getAmoLogin();
        String apiKey = jsonAmoAccount.getApiKey();

        if (StringUtils.isAnyBlank(domain, amoLogin, apiKey)) {
            return new Message(Message.Status.Error, "Some params are blank!").toString();
        }

        if (user.getAmoAccount() == null) {
            user.setAmoAccount(new AmoAccount());
            user.getAmoAccount().setUser(user);
            user.getAmoAccount().setNextelLogin(user.getLogin());
        }

        AmoAccount amoAccount = user.getAmoAccount();
        amoAccount.setDomain(domain);
        amoAccount.setAmoLogin(amoLogin);
        amoAccount.setApiKey(apiKey);
        amoAccount.setPhoneId(null);
        amoAccount.setPhoneEnumId(null);

        try {
            HibernateController.updateUser(user);
            return new Message(Message.Status.Success, "Amo account setted").toString();
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка изменения амо аккаунта: ", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        } finally {
            if (safeMode)
                user.reloadAmoAccountFromDb();
        }
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String check(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        if (user.getAmoAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected amo account").toString();
        }

        String domain = user.getAmoAccount().getDomain();
        String userLogin = user.getAmoAccount().getAmoLogin();
        String userApiKey = user.getAmoAccount().getApiKey();

        try {
            AmoDAO.checkAllAccess(domain, userLogin, userApiKey);
            return new Message(Message.Status.Success, "Check complete. It works!").toString();
        } catch (AmoAccountNotFoundException e) {
            return new Message(Message.Status.Error, "Amo account not found").toString();
        }catch (AmoWrongLoginOrApiKeyExeption e){
            return new Message(Message.Status.Error, "Wrong login or password").toString();
        }catch (AmoCantCreateDealException e) {
            return new Message(Message.Status.Error, "User have not enough rights for create contacts and deals").toString();
        }catch (UnirestException e){
            LOGGER.error("Ошибка соединения или парсинга для проверки аккаунта амо. Аккаунт Nextel "+user.getLogin(), e);
            return new Message(Message.Status.Error, "Internal error. Please try again").toString();
        }catch (AmoUnknownException e){
            LOGGER.error("Неизвестный код ответа от АМО", e);
            return new Message(Message.Status.Error, "Internal error. Please contact us.").toString();
        }catch (Exception e){
            LOGGER.error("Неизвестная ошибка при проверке аккаунта амо. Аккаунт nextel: " + user.getLogin(), e);
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

        if (user.getAmoAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected Amo account").toString();
        }

        try {
            HibernateController.removeAmoAccount(user);
            return new Message(Message.Status.Success, "Amo account removed").toString();
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка удаления амо аккаунта. Возвращаем амо обратно.", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        } finally {
            if (safeMode)
                user.reloadAmoAccountFromDb();
        }
    }
}
