package ua.adeptius.asterisk.webcontrollers;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.Telephony;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/telephony")
public class TelephonyController {


    //TODO переделать
    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String setTelephony(@RequestBody Telephony incomeTelephony, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        if (incomeTelephony.getInnerCount() == null || incomeTelephony.getInnerCount() < 0) {
            incomeTelephony.setInnerCount(0);
        }

        if (incomeTelephony.getOuterCount() == null || incomeTelephony.getOuterCount() < 0) {
            incomeTelephony.setOuterCount(0);
        }

        incomeTelephony.setUser(user);

        try {
            incomeTelephony.updateNumbers();
        } catch (NotEnoughNumbers e) {
            return new Message(Message.Status.Error, "Not enough free numbers").toString();
        } catch (Exception e) {
            return new Message(Message.Status.Error, "Internal error").toString();
        }
        Telephony backupTelephony = user.getTelephony();
        user.setTelephony(incomeTelephony);
        try {
            HibernateController.updateUser(user);
            CallProcessor.updatePhonesHashMap();
            RulesConfigDAO.removeFileIfNeeded(user);
            return new Message(Message.Status.Success, "Telephony updated").toString();
        } catch (Exception e) {
            e.printStackTrace();
            user.setTelephony(backupTelephony);
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
