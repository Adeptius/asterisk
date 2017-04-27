package ua.adeptius.asterisk.webcontrollers;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/tracking")
public class TrackingController {


    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getUserByName(@RequestBody Tracking incomeTracking, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        if (incomeTracking.getStandartNumber() == null || incomeTracking.getStandartNumber().equals("")) {
            return new Message(Message.Status.Error, "Wrong standart number").toString();
        }

        if (incomeTracking.getTimeToBlock() == null || incomeTracking.getTimeToBlock() == 0) {
            incomeTracking.setTimeToBlock(60);
        }

        if (incomeTracking.getSiteNumbersCount() == null || incomeTracking.getSiteNumbersCount() < 0) {
            incomeTracking.setSiteNumbersCount(0);
        }
        if (incomeTracking.getBlackIps() == null){
            incomeTracking.setBlackIps(user.getTracking().getBlackIps());
        }

        incomeTracking.setUser(user);

        try {
            incomeTracking.updateNumbers();
        } catch (NotEnoughNumbers e) {
            return new Message(Message.Status.Error, "Not enough free numbers").toString();
        } catch (Exception e) {
            return new Message(Message.Status.Error, "Internal error").toString();
        }
        Tracking backupTracking = user.getTracking();
        user.setTracking(incomeTracking);

        try {
            HibernateController.updateUser(user);
            CallProcessor.updatePhonesHashMap();
            RulesConfigDAO.removeFileIfNeeded(user);
            return new Message(Message.Status.Success, "Tracking updated").toString();
        } catch (Exception e) {
            e.printStackTrace();
            user.setTracking(backupTracking);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String removeTracking(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have not tracking").toString();
        }
        try {
            HibernateController.removeTracking(user);
            RulesConfigDAO.removeFileIfNeeded(user);
            return new Message(Message.Status.Success, "Tracking removed").toString();
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
        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have no tracking").toString();
        }

        try {
            return new ObjectMapper().writeValueAsString(user.getTracking());
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
