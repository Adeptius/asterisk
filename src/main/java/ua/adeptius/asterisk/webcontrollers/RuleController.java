package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.MySqlCalltrackDao;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

//@Controller
//@RequestMapping("/rules")
public class RuleController {
//
//    private static Logger LOGGER = LoggerFactory.getLogger(RuleController.class.getSimpleName());
//
//
//    @RequestMapping(value = "/getAvailableNumbers", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
//    @ResponseBody
//    public String getAvailableNumbers(HttpServletRequest request) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//
//        try {
//            return new ObjectMapper().writeValueAsString(user.getAvailableNumbers());
//        } catch (Exception e) {
//            LOGGER.error(user.getLogin() + ": ошибка получения доступных номеров для правил", e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        }
//    }
//
//    private static List<String> melodies;
//    private static long melodiesTimeCache;
//
//    private static void loadMelodies() throws Exception {
//        melodies = MySqlCalltrackDao.getMelodies();
//        melodiesTimeCache = new Date().getTime();
//    }
//
//
//    @RequestMapping(value = "/getMelodies", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
//    @ResponseBody
//    public String getHistory() {
//        try {
//
//            if (melodies == null) {
//                loadMelodies();
//            }
//
//            long currentTime = new Date().getTime();
//            long past = currentTime - melodiesTimeCache;
//            long updateEvery = 3600000; // обновление каждый час
//
//            if (past > updateEvery){
//                loadMelodies();
//            }
//
//            return new Gson().toJson(melodies);
//        } catch (Exception e) {
//            LOGGER.error("Ошибка получения списка мелодий", e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        }
//    }
}