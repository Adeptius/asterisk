package ua.adeptius.asterisk.webcontrollers;


import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.*;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.JsonAmoForController;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ua.adeptius.asterisk.json.Message.Status.Error;


@Controller
@RequestMapping(value = "/amo", produces = "application/json; charset=UTF-8")
@ResponseBody
public class AmoWebController {

    private static boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(AmoWebController.class.getSimpleName());
    private static ObjectMapper mapper = new ObjectMapper();


    @PostMapping("/get")
    public Object get(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return new Message(Error, "User have not connected amo account");
        }

        return amoAccount;
    }

    @PostMapping("/set")
    public Message set(@RequestBody JsonAmoForController jsonAmoAccount, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        String domain = jsonAmoAccount.getDomain();
        String amoLogin = jsonAmoAccount.getAmoLogin();
        String apiKey = jsonAmoAccount.getApiKey();
        boolean cling = jsonAmoAccount.isCling();

        if (StringUtils.isAnyBlank(domain, amoLogin, apiKey)) {
            return new Message(Error, "Some params are blank!");
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
        amoAccount.setCling(cling);
        user.setAmoAccount(amoAccount);

        try {
            HibernateController.update(user);
            return new Message(Message.Status.Success, "Amo account setted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка изменения амо аккаунта: ", e);
            return new Message(Error, "Internal error");
        } finally {
            if (safeMode){

            }
        }
    }

    @PostMapping("/test")
    public Message check(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        if (user.getAmoAccount() == null) {
            return new Message(Error, "User have not connected amo account");
        }

        try {
            AmoDAO.checkAllAccess(user.getAmoAccount());
            return new Message(Message.Status.Success, "Check complete. It works!");
        } catch (AmoAccountNotFoundException e) {
            return new Message(Error, "Amo account not found");
        } catch (AmoWrongLoginOrApiKeyException e) {
            return new Message(Error, "Wrong login or api key");
        } catch (AmoCantCreateDealException e) {
            return new Message(Error, "User have not enough rights for create contacts and deals");
        } catch (AmoAccountNotPaidException e) {
            return new Message(Error, "User have not paid account");
        } catch (UnirestException e) {
            LOGGER.error("Ошибка соединения или парсинга для проверки аккаунта амо. Аккаунт Nextel " + user.getLogin(), e);
            return new Message(Error, "Internal error.");
        } catch (AmoUnknownException e) {
            LOGGER.error("Неизвестный код ответа от АМО", e);
            return new Message(Error, "Internal error.");
        } catch (Exception e) {
            LOGGER.error("Неизвестная ошибка при проверке аккаунта амо. Аккаунт nextel: " + user.getLogin(), e);
            return new Message(Error, "Internal error");
        }
    }

    @PostMapping("/remove")
    public Message remove(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        if (user.getAmoAccount() == null) {
            return new Message(Error, "User have not connected Amo account");
        }

        user.setAmoAccount(null);

        try {
            HibernateController.update(user);
            return new Message(Message.Status.Success, "Amo account removed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления амо аккаунта. Возвращаем амо обратно.", e);
            return new Message(Error, "Internal error");
        } finally {
            if (safeMode){

            }
        }
    }


    @PostMapping("/getUsers")
    public Object getUsers(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return new Message(Error, "User have not connected Amo account");
        }

        try {
            // Загружаем из Amo список работников пользователя
            JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
            return jsonAmoAccount.getUsersNameAndId();
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": Ошибка при отправке списка сотрудников", e);
            return new Message(Error, "Internal error");
        }
    }

    @PostMapping("/getBindings")
    public Object getBindings(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return new Message(Error, "User have not connected Amo account");
        }

        HashMap<String, String> workerIdAndPhones; // получаем текуший список
        AmoOperatorLocation operatorLocation = user.getOperatorLocation();
        if (operatorLocation != null){
            workerIdAndPhones = operatorLocation.getAmoUserIdAndInnerNumber();
        }else {
            workerIdAndPhones = new HashMap<>();
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
            return new Message(Error, "Internal error");
        }
    }

    @PostMapping("/setBindings")
    public Message setBindings(HttpServletRequest request, @RequestBody HashMap<String, String> newBindings) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            return new Message(Error, "User have not connected Amo account");
        }

        LOGGER.debug("{}: запрос сохранения привязок {}",user.getLogin(), newBindings);

        try {
            JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
            HashMap<String, String> usersNameAndId = jsonAmoAccount.getUsersNameAndId();

//            Формируем окончательную мапу userID <-> phone number
            HashMap<String, String> newHashAmoBindings = new HashMap<>();

            //Создаю контрольный Set что бы понять не введены ли дубли номеров телефонов.
            Set<String> nameCheck = new HashSet<>();

            for (Map.Entry<String, String> entry : newBindings.entrySet()) {
                String worker = entry.getKey();
                String phone = entry.getValue();

                if (StringUtils.isAnyBlank(worker, phone)) {
                    continue;
                }

                String workerId = usersNameAndId.get(worker);
                if (workerId == null) {
                    return new Message(Error, "User '" + worker + "' not found in amo account.");
                }

                if (user.getInnerPhoneByNumber(phone) == null){
                    try {
                        phone = MyStringUtils.cleanAndValidateUkrainianPhoneNumber(phone);
                    } catch (IllegalArgumentException e) {
                        return new Message(Error, "Phone " + phone + " is not user's SIP or Ukrainian GSM");
                    }
                }

                if (nameCheck.add(phone)){
                    newHashAmoBindings.put(workerId, phone);
                }else {
                    return new Message(Error, "Found duplicates of number " + phone);
                }
            }

            AmoOperatorLocation location = new AmoOperatorLocation();
            location.setLogin(user.getLogin());
            location.setName("location");
            location.setAmoUserIdAndInnerNumber(newHashAmoBindings);
            user.setAmoOperatorLocations(location);

            HibernateController.update(user);
            return new Message(Message.Status.Success, "Bindings saved");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": Ошибка при создании списка привязок " + newBindings, e);
            return new Message(Error, "Internal error");
        }
    }
}
