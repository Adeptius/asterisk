package ua.adeptius.asterisk.webcontrollers;


import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.json.JsonTracking;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/tracking")
public class TrackingController {

    private static Logger LOGGER =  LoggerFactory.getLogger(TrackingController.class.getSimpleName());

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String addTracking(@RequestBody JsonTracking jsonTracking, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() != null) {
            return new Message(Message.Status.Error, "User already have tracking").toString();
        }

        if (jsonTracking.getStandartNumber() == null || jsonTracking.getStandartNumber().equals("")) {
            return new Message(Message.Status.Error, "Wrong standart number").toString();
        }

        if (jsonTracking.getTimeToBlock() == null || jsonTracking.getTimeToBlock() == 0) {
            jsonTracking.setTimeToBlock(120);
        }

        Tracking newTracking = new Tracking();
        newTracking.setUser(user);
        newTracking.setStandartNumber(jsonTracking.getStandartNumber());
        newTracking.setTimeToBlock(jsonTracking.getTimeToBlock());
        newTracking.setSiteNumbersCount(0);

        user.setTracking(newTracking);
        try {
            HibernateController.updateUser(user);
            return new Message(Message.Status.Success, "Tracking added").toString();
        } catch (Exception e) {
            user.setTracking(null);
            LOGGER.error(user.getLogin()+": ошибка добавление трекинга: "+jsonTracking, e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/setNumberCount", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String setNumberCount(@RequestBody JsonTracking jsonTracking, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have no tracking").toString();
        }

        Tracking tracking = user.getTracking();
        // делаем бекап количества номеров на случай ошибки
        int currentNumberCount = tracking.getSiteNumbersCount();
        int neededNumberCount = jsonTracking.getSiteNumbersCount();

        try {
            tracking.setSiteNumbersCount(neededNumberCount);
            tracking.updateNumbers();
            HibernateController.updateUser(user);
            CallProcessor.updatePhonesHashMap();
            RulesConfigDAO.removeFileIfNeeded(user);
            return new Message(Message.Status.Success, "Number count set").toString();
        } catch (NotEnoughNumbers e){
            tracking.setSiteNumbersCount(currentNumberCount);
            return new Message(Message.Status.Success, "Not enough free numbers").toString();
        } catch (Exception e) {
            tracking.setSiteNumbersCount(currentNumberCount);
            LOGGER.error(user.getLogin()+": ошибка задания количества номеров: "+jsonTracking, e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/set", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getUserByName(@RequestBody JsonTracking jsonTracking, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have not tracking").toString();
        }

        if (jsonTracking.getStandartNumber() == null || jsonTracking.getStandartNumber().equals("")) {
            return new Message(Message.Status.Error, "Wrong standart number").toString();
        }

        if (jsonTracking.getTimeToBlock() == null || jsonTracking.getTimeToBlock() == 0) {
            jsonTracking.setTimeToBlock(120);
        }

        Tracking tracking = user.getTracking();
        tracking.setTimeToBlock(jsonTracking.getTimeToBlock());
        tracking.setStandartNumber(jsonTracking.getStandartNumber());

        try {
            HibernateController.updateUser(user);
            return new Message(Message.Status.Success, "Tracking updated").toString();
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка задания трекинга: "+jsonTracking, e);
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
            LOGGER.error(user.getLogin()+": ошибка удаления трекинга", e);
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
            LOGGER.error(user.getLogin()+": ошибка получения пользователя", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
