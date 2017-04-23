package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.json.JsonHistoryQuery;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.newmodel.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping(value = "/getUser", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getUser(HttpServletRequest request) {

        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        try {
            return new ObjectMapper().writeValueAsString(user);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/setTracking", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getUserByName(@RequestBody Tracking incomeTracking, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        //TODO черный список

        if (incomeTracking.getStandartNumber() == null || incomeTracking.getStandartNumber().equals("")) {
            return new Message(Message.Status.Error, "Wrong standart number").toString();
        }

        if (incomeTracking.getTimeToBlock() == null || incomeTracking.getTimeToBlock() == 0) {
            incomeTracking.setTimeToBlock(60);
        }

        if (incomeTracking.getSiteNumbersCount() == null || incomeTracking.getSiteNumbersCount() < 0) {
            incomeTracking.setSiteNumbersCount(0);
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

    @RequestMapping(value = "/setTelephony", method = RequestMethod.POST, produces = "application/json")
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


    @RequestMapping(value = "/removeTracking", method = RequestMethod.POST, produces = "application/json")
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

    @RequestMapping(value = "/removeTelephony", method = RequestMethod.POST, produces = "application/json")
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


    @RequestMapping(value = "/getHistory", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getHistory(@RequestBody JsonHistoryQuery query, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        String dateFrom = query.getDateFrom();
        String dateTo = query.getDateTo();
        String direction = query.getDirection();

        //TODO валидация данных

        try {
            List<Call> calls = MySqlStatisticDao.getStatisticOfRange(user.getLogin(), dateFrom, dateTo, direction.toString());
            return new ObjectMapper().writeValueAsString(calls);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "DB error").toString();
        }
    }

    @RequestMapping(value = "/sipPasswords", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getPasswords(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTelephony() == null){
            return new Message(Message.Status.Error, "User have not tracking").toString();
        }

        try {
            Map<String, String> map = PhonesDao.getSipPasswords(user.getLogin());
            return new ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }

    @RequestMapping(value = "/record/{id}/{date}", method = RequestMethod.GET)
    public void getFile(@PathVariable String id, @PathVariable String date, HttpServletResponse response) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);

        try {
            File file = findFile(year, month, day, id);
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("File not found");
        }
    }


    private static File findFile(String year, String month, String day, String id) throws Exception {
        Path path = Paths.get("/var/spool/asterisk/monitor/" + year + "/" + month + "/" + day);

        List<File> list = Files.walk(path)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (File file : list) {
            if (file.getName().contains(id)) {
                return file;
            }
        }
        throw new FileNotFoundException();
    }
}
