package ua.adeptius.asterisk.webcontrollers;


import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.AmoAccountNotFoundException;
import ua.adeptius.amocrm.exceptions.AmoCantCreateDealException;
import ua.adeptius.amocrm.exceptions.AmoUnknownException;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyExeption;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.json.JsonAmoForController;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Controller
@RequestMapping(value = "/amo", produces = "application/json; charset=UTF-8")
@ResponseBody
public class AmoController {

    private static boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(AmoController.class.getSimpleName());
    private static ObjectMapper mapper = new ObjectMapper();


    @PostMapping("/get")
    public Object get(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return new Message(Message.Status.Error, "User have not connected amo account");
        }

        return amoAccount;
    }

    @PostMapping("/set")
    public Object set(@RequestBody JsonAmoForController jsonAmoAccount, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String domain = jsonAmoAccount.getDomain();
        String amoLogin = jsonAmoAccount.getAmoLogin();
        String apiKey = jsonAmoAccount.getApiKey();

        if (StringUtils.isAnyBlank(domain, amoLogin, apiKey)) {
            return new Message(Message.Status.Error, "Some params are blank!");
        }

        AmoAccount amoAccount = user.getAmoAccount();

        if (amoAccount == null) {
            amoAccount = new AmoAccount();
        }

        amoAccount.setDomain(domain);
        amoAccount.setAmoLogin(amoLogin);
        amoAccount.setApiKey(apiKey);
        amoAccount.setPhoneId(null);
        amoAccount.setPhoneEnumId(null);
        user.setAmoAccount(amoAccount);

        try {
            HibernateDao.update(user);
            return new Message(Message.Status.Success, "Amo account setted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка изменения амо аккаунта: ", e);
            return new Message(Message.Status.Error, "Internal error");
        } finally {
            if (safeMode){

            }
        }
    }

    @PostMapping("/test")
    public Object check(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        if (user.getAmoAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected amo account");
        }

        try {
            AmoDAO.checkAllAccess(user.getAmoAccount());
            return new Message(Message.Status.Success, "Check complete. It works!");
        } catch (AmoAccountNotFoundException e) {
            return new Message(Message.Status.Error, "Amo account not found");
        } catch (AmoWrongLoginOrApiKeyExeption e) {
            return new Message(Message.Status.Error, "Wrong login or api key");
        } catch (AmoCantCreateDealException e) {
            return new Message(Message.Status.Error, "User have not enough rights for create contacts and deals");
        } catch (UnirestException e) {
            LOGGER.error("Ошибка соединения или парсинга для проверки аккаунта амо. Аккаунт Nextel " + user.getLogin(), e);
            return new Message(Message.Status.Error, "Internal error.");
        } catch (AmoUnknownException e) {
            LOGGER.error("Неизвестный код ответа от АМО", e);
            return new Message(Message.Status.Error, "Internal error.");
        } catch (Exception e) {
            LOGGER.error("Неизвестная ошибка при проверке аккаунта амо. Аккаунт nextel: " + user.getLogin(), e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }

    @PostMapping("/remove")
    public Object remove(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        if (user.getAmoAccount() == null) {
            return new Message(Message.Status.Error, "User have not connected Amo account");
        }

        user.setAmoAccount(null);

        try {
            HibernateDao.update(user);
            return new Message(Message.Status.Success, "Amo account removed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления амо аккаунта. Возвращаем амо обратно.", e);
            return new Message(Message.Status.Error, "Internal error");
        } finally {
            if (safeMode){

            }
        }
    }


    @PostMapping("/getBindings")
    public Object getBindings(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return new Message(Message.Status.Error, "User have not connected Amo account");
        }

        HashMap<String, String> workerIdAndPhones;

        // получаем текущий список привязок и конвертируем его в мапу id работника <-> номер телефона
        Set<AmoOperatorLocation> operatorLocations = user.getAmoOperatorLocations();
        if (operatorLocations.isEmpty()) {
            workerIdAndPhones = new HashMap<>();
        } else {
            workerIdAndPhones = operatorLocations.iterator().next().getAmoUserIdAndInnerNumber();
        }


        try {
            // Загружаем из Amo список работников пользователя
            JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
            HashMap<String, String> users = jsonAmoAccount.getUsersIdAndName();

            // Теперь комбинируем 2 мапы в третью, что бы пользователь видел имена работников и их номера телефонов.
            HashMap<String, String> completeMap = new HashMap<>();
            for (Map.Entry<String, String> entry : users.entrySet()) {
                String id = entry.getKey();
                String name = entry.getValue();
                completeMap.put(name, workerIdAndPhones.get(id));
            }

            return completeMap;
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": Ошибка при создании списка привязок", e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }

    @PostMapping("/setBindings")
    public Object setBindings(HttpServletRequest request, @RequestBody HashMap<String, String> newBindings) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return new Message(Message.Status.Error, "User have not connected Amo account");
        }

        try {
            JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
            HashMap<String, String> usersNameAndId = jsonAmoAccount.getUsersNameAndId();

            HashMap<String, String> newHashAmoBindings = new HashMap<>();

            for (Map.Entry<String, String> entry : newBindings.entrySet()) {
                String worker = entry.getKey();
                String phone = entry.getValue();

                if (StringUtils.isAnyBlank(worker, phone)) {
                    continue;
                }

                String workerId = usersNameAndId.get(worker);
                if (workerId == null) {
                    return new Message(Message.Status.Error, "User " + worker + " not found in amo account.");
                }

                newHashAmoBindings.put(worker, phone);
            }

            Set<AmoOperatorLocation> operatorLocations = user.getAmoOperatorLocations();
            if (!operatorLocations.isEmpty()) {
                operatorLocations.clear();
            }

            AmoOperatorLocation location = new AmoOperatorLocation();
            location.setLogin(user.getLogin());
            location.setName("location");
            location.setAmoUserIdAndInnerNumber(newHashAmoBindings);
            operatorLocations.add(location);

            HibernateDao.update(user);
            return new Message(Message.Status.Success, "Bindings saved");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": Ошибка при создании списка привязок", e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }
}
