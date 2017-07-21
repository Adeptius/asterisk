package ua.adeptius.asterisk.webcontrollers;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.json.JsonHistoryQuery;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.NewCall;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/history")
public class HistoryController {

    private static Logger LOGGER =  LoggerFactory.getLogger(HistoryController.class.getSimpleName());


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getHistory(@RequestBody JsonHistoryQuery query, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        String dateFrom = query.getDateFrom();
        String dateTo = query.getDateTo();
        String direction = query.getDirection();

        if (!direction.equals("IN") && !direction.equals("OUT")) {
            return new Message(Message.Status.Error, "Wrong direction").toString();
        }

        if (!Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(dateFrom).find()) {
            return new Message(Message.Status.Error, "Wrong FROM date").toString();
        }

        if (!Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(dateTo).find()) {
            return new Message(Message.Status.Error, "Wrong TO date").toString();
        }

        String login = user.getLogin();
        try {
            LOGGER.debug("{}: запрос истории c {} по {}, направление {}", login, dateFrom, dateTo, direction);
            List<NewCall> calls = MySqlStatisticDao.getStatisticOfRange(login, dateFrom, dateTo, direction);
            return new ObjectMapper().writeValueAsString(calls);
        } catch (Exception e) {
            LOGGER.error(login +": ошибка запроса истории c "+dateFrom+" по "+dateTo+", направление "+direction, e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }


    @RequestMapping(value = "/record/{id}/{date}", method = RequestMethod.GET)
    public void getFile(@PathVariable String id, @PathVariable String date, HttpServletResponse response) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);

        try {
            LOGGER.trace("Запрос записи звонка ID {}, Date {}",id, date);
            File file = findFile(year, month, day, id);
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            LOGGER.error("Ошибка получения записи ID "+id+" Date "+date, e);
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
