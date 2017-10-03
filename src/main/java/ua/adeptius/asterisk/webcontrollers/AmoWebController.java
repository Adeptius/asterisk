package ua.adeptius.asterisk.webcontrollers;


import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.resources.Messages_sv;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.exceptions.*;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonPipeline;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.exceptions.UkrainianNumberParseException;
import ua.adeptius.asterisk.json.JsonAmoForController;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.senders.ErrorsMailSender;
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static ua.adeptius.asterisk.json.Message.Status.Error;


@Controller
@RequestMapping(value = "/amo", produces = "application/json; charset=UTF-8")
@ResponseBody
public class AmoWebController {

    private static boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(AmoWebController.class.getSimpleName());


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

        try{
            JsonAmoForController jsonAmoForController = new JsonAmoForController(amoAccount);
            JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);

            // список пользователей
            HashMap<String, String> usersIdAndName = jsonAmoAccount.getUsersIdAndName();
            jsonAmoForController.setUsersIdAndName(usersIdAndName);

            // расположение операторов
            HashMap<String, String> workerIdAndPhones;
            AmoOperatorLocation operatorLocation = user.getOperatorLocation();
            if (operatorLocation != null) {
                workerIdAndPhones = operatorLocation.getAmoUserIdAndInnerNumber();
            } else {
                workerIdAndPhones = new HashMap<>();
            }
            jsonAmoForController.setOperatorLocation(workerIdAndPhones);

            // Воронки
            List<JsonPipeline> pipelines = jsonAmoAccount.getPipelines();
            jsonAmoForController.setPipelines(pipelines);

            return jsonAmoForController;
        }catch (Exception e){
            LOGGER.error(user.getLogin() + ": ошибка выдачи AmoAccount", e);
            return new Message(Error, "Internal error");
        }
    }

    @PostMapping("/set")
    public Message set(@RequestBody JsonAmoForController jsonAmoAccount, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        // Проверка домена, логина, ключа
        String domain = jsonAmoAccount.getDomain();
        String amoLogin = jsonAmoAccount.getAmoLogin();
        String apiKey = jsonAmoAccount.getApiKey();
        if (StringUtils.isAnyBlank(domain, amoLogin, apiKey)) {
            return new Message(Error, "Some params are blank!");
        }

        // Проверка расположения операторов
        HashMap<String, String> newOperatorLocation = jsonAmoAccount.getOperatorLocation();
        if (newOperatorLocation == null) {
            newOperatorLocation = new HashMap<>();
        }

        //Создаю контрольный Set что бы понять не введены ли дубли номеров телефонов.
        Set<String> nameCheck = new HashSet<>();

        for (Map.Entry<String, String> entry : newOperatorLocation.entrySet()) {
//            String workerId = entry.getKey();
            String phone = entry.getValue();

            if (user.getInnerPhoneByNumber(phone) == null) {
                try {
                    phone = MyStringUtils.cleanAndValidateUkrainianPhoneNumber(phone);
                } catch (UkrainianNumberParseException e) {
                    return new Message(Error, "Phone " + phone + " is not user's SIP or Ukrainian GSM");
                }
            }

            if (!nameCheck.add(phone)) {
                return new Message(Error, "Found duplicates of number " + phone);
            }
        }

        //Эти данные проверять не нужно
        boolean cling = jsonAmoAccount.isCling();
        int pipelineId = jsonAmoAccount.getPipelineId();
        int stageId = jsonAmoAccount.getStageId();
        String[] responsibleUserSchedule = jsonAmoAccount.getResponsibleUserSchedule();


        // Проверка завершена. Далее просто всё сохраняем

        AmoOperatorLocation location = new AmoOperatorLocation();
        location.setName("location");
        location.setAmoUserIdAndInnerNumber(newOperatorLocation);
        user.setAmoOperatorLocations(location);

        // Если амо не был подключен - создаём.
        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            amoAccount = new AmoAccount();
        }

        // сбрасываем данные
        amoAccount.setPhoneId(null);
        amoAccount.setPhoneEnumId(null);
        amoAccount.setApiUserId(null);

        // назначаем новые
        amoAccount.setDomain(domain);
        amoAccount.setAmoLogin(amoLogin);
        amoAccount.setApiKey(apiKey);
        amoAccount.setCling(cling);
        amoAccount.setPipelineId(pipelineId);
        amoAccount.setStageId(stageId);
        amoAccount.setResponsibleUserSchedule(responsibleUserSchedule);
        user.setAmoAccount(amoAccount);

        try {
            HibernateController.update(user);
            return new Message(Message.Status.Success, "Amo account setted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка изменения амо аккаунта: ", e);
            return new Message(Error, "Internal error");
        } finally {
            if (safeMode) {

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
            if (safeMode) {

            }
        }
    }

    @PostMapping(value = "/widDovovorAndEmail", produces = "text/html; charset=UTF-8")
    public Object widDovovorAndEmail(String dogovor, String email) {

        if (StringUtils.isAnyBlank(dogovor, email)){
            return "Договор или email не указан";
        }

        try {
            User user = new User();
            user.setLogin("WID_AMO_TEST");
            AmoAccount amoAccount = new AmoAccount();
            amoAccount.setUser(user);
            amoAccount.setDomain("wid");
            amoAccount.setApiKey("a99ead2f473e150091360d25aecc2878");
            amoAccount.setAmoLogin("adeptius@wid.ua");
            return AmoDAO.doChangeMailForWidOnly(amoAccount, dogovor, email);
        }catch (Exception e){
            LOGGER.error("WID-MAIL: ошибка. Договор "+dogovor+" email " + email, e);
            ErrorsMailSender.send("WID-MAIL: ошибка. Договор "+dogovor+" email " + email, e);
            return "Спасибо!";
        }
    }
}
