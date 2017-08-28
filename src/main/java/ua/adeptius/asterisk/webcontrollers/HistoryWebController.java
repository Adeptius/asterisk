package ua.adeptius.asterisk.webcontrollers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.json.JsonHistoryQuery;
import ua.adeptius.asterisk.json.JsonHistoryResponse;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.Call;

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
@RequestMapping(value = "/history", produces = "application/json; charset=UTF-8")
@ResponseBody
public class HistoryWebController {

    //TODO offset, count
    private static Logger LOGGER =  LoggerFactory.getLogger(HistoryWebController.class.getSimpleName());

    @PostMapping("/get")
    public Object getHistory(@RequestBody JsonHistoryQuery query, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String login = user.getLogin();

        String dateFrom = query.getDateFrom();
        String dateTo = query.getDateTo();
        String direction = query.getDirection();
        int limit = query.getLimit();
        int offset = query.getOffset();

        if ( 1 > limit || limit > 300){
            return new Message(Message.Status.Error, "Limit range is 1-300");
        }

        if (offset < 0){
            return new Message(Message.Status.Error, "Offset is less than 0");
        }

        if (!direction.equals("IN") && !direction.equals("OUT")) {
            return new Message(Message.Status.Error, "Wrong direction");
        }

        if (!Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(dateFrom).find()) {
            return new Message(Message.Status.Error, "Wrong FROM date");
        }

        if (!Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}").matcher(dateTo).find()) {
            return new Message(Message.Status.Error, "Wrong TO date");
        }

        try {

        // если offset 0 то с ответом передаём количество записей с помощью второго запроса
            int count = -1;
            if (offset == 0){
                count = MySqlStatisticDao.getCountStatisticOfRange(login, dateFrom, dateTo, direction);
            }

            LOGGER.debug("{}: запрос истории c {} по {}, направление {}", login, dateFrom, dateTo, direction);
            List<Call> calls = MySqlStatisticDao.getStatisticOfRange(login, dateFrom, dateTo, direction, limit, offset);

            return new JsonHistoryResponse(limit,offset, calls, count);
        } catch (Exception e) {
            LOGGER.error(login +": ошибка запроса истории c "+dateFrom+" по "+dateTo+", направление "+direction, e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }


    @GetMapping("/record/{id}/{date}")
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

        String lookingFile = id + ".wav";

        for (File file : list) {
            if (file.getName().endsWith(lookingFile)) {
                return file;
            }
        }
        throw new FileNotFoundException();
    }
}