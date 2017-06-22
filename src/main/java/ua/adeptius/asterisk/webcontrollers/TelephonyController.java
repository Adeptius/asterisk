package ua.adeptius.asterisk.webcontrollers;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.json.JsonTelephony;
import ua.adeptius.asterisk.json.JsonTracking;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.Telephony;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/telephony")
public class TelephonyController {

    private static Logger LOGGER =  LoggerFactory.getLogger(TelephonyController.class.getSimpleName());


    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String addTracking(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTelephony() != null) {
            return new Message(Message.Status.Error, "User already have telephony").toString();
        }

        Telephony telephony = new Telephony();
        telephony.setUser(user);
        telephony.setInnerCount(0);
        telephony.setOuterCount(0);

        user.setTelephony(telephony);
        try {
            HibernateController.updateUser(user);
            return new Message(Message.Status.Success, "Telephony added").toString();
        } catch (Exception e) {
            user.setTelephony(null);
            LOGGER.error(user.getLogin()+": ошибка добавления телефонии",e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/setNumberCount", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String setNumberCount(@RequestBody JsonTelephony jsonTelephony, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTelephony() == null) {
            return new Message(Message.Status.Error, "User have no telephony").toString();
        }

        Telephony telephony = user.getTelephony();
        // делаем бекап количества номеров на случай ошибки
        int currentOuterNumberCount = telephony.getOuterCount();
        int currentInnerNumberCount = telephony.getInnerCount();
        int neededOuterNumberCount = jsonTelephony.getOuterCount();
        int neededInnerNumberCount = jsonTelephony.getInnerCount();

        try {
            telephony.setOuterCount(neededOuterNumberCount);
            telephony.setInnerCount(neededInnerNumberCount);
            telephony.updateNumbers();
            HibernateController.updateUser(user);
            CallProcessor.updatePhonesHashMap();
            RulesConfigDAO.removeFileIfNeeded(user);
            return new Message(Message.Status.Success, "Number count set").toString();
        } catch (NotEnoughNumbers e){
            telephony.setOuterCount(currentOuterNumberCount); // возвращаем бэкап
            telephony.setInnerCount(currentInnerNumberCount);
            return new Message(Message.Status.Success, "Not enough free numbers").toString();
        } catch (Exception e) {
            telephony.setOuterCount(currentOuterNumberCount); // возвращаем бэкап
            telephony.setInnerCount(currentInnerNumberCount);
            LOGGER.error(user.getLogin()+": ошибка изменения количества номеров телефонии: "+jsonTelephony, e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String removeTelephony(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTelephony() == null) {
            return new Message(Message.Status.Error, "User have not telephony").toString();
        }
        try {
            HibernateController.removeTelephony(user);
            RulesConfigDAO.removeFileIfNeeded(user);
            return new Message(Message.Status.Success, "Telephony removed").toString();
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка удаления телефонии",e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getBlackList(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTelephony() == null) {
            return new Message(Message.Status.Error, "User have no telephony").toString();
        }

        try {
            return new ObjectMapper().writeValueAsString(user.getTelephony());
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка получения телефонии",e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/getSipPasswords", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getPasswords(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTelephony() == null){
            return new Message(Message.Status.Error, "User have not telephony").toString();
        }

        try {
            Map<String, String> map = PhonesDao.getSipPasswords(user.getLogin());
            return new ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка получения паролей к SIP номерам");
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
