package ua.adeptius.asterisk.webcontrollers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.exceptions.JsonHistoryQueryValidationException;
import ua.adeptius.asterisk.json.JsonHistoryQuery;
import ua.adeptius.asterisk.json.JsonHistoryResponse;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.Call;

import javax.servlet.ServletOutputStream;
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

import static ua.adeptius.asterisk.json.Message.Status.Error;
import static ua.adeptius.asterisk.json.Message.Status.Success;

@Controller
@RequestMapping(value = "/history", produces = "application/json; charset=UTF-8")
@ResponseBody
public class HistoryWebController {


    private static Logger LOGGER = LoggerFactory.getLogger(HistoryWebController.class.getSimpleName());

    @PostMapping("/get")
    public Object getHistory(@RequestBody JsonHistoryQuery query, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        String login = user.getLogin();
        LOGGER.debug("{}: запрос истории: {}", login, query);

        if (query == null) {
            return new Message(Error, "Query null");
        }

        try {
            String sqlCount = query.buildSqlQueryCount(login);
            String sqlResult = query.buildSqlQueryResult(login);

            int count = MySqlStatisticDao.getCountStatisticOfRange(sqlCount);
            List<Call> calls = MySqlStatisticDao.getStatisticOfRange(sqlResult);

            return new JsonHistoryResponse(query.getLimit(), query.getOffset(), calls, count);
        } catch (JsonHistoryQueryValidationException valEx) {
            LOGGER.debug("{}: запрос истории неправильный: {}", login, valEx.getMessage());
            return new Message(Error, valEx.getMessage());

        } catch (Exception e) {
            LOGGER.error(login + ": ошибка запроса истории", e);
            return new Message(Error, "Internal error");
        }
    }

    @PostMapping("/setComment")
    public Object setComment(HttpServletRequest request, Integer id, String comment) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Error, "Authorization invalid");
        }

        String login = user.getLogin();
        LOGGER.info("{}: запрос установки коментария в историю. id {}: {}", login, id, comment);

        if (id == null){
            LOGGER.debug("{}: Id не указан", login);
            return new Message(Error, "Id is null");
        }

        if (StringUtils.isBlank(comment)){
            LOGGER.debug("{}: комент пуст", login);
            return new Message(Error, "Comment is blank");
        }

        if (comment.length()>100){
            LOGGER.debug("{}: комент слишком большой", login);
            return new Message(Error, "Comment limit");
        }

        try {
            MySqlStatisticDao.setComment(login, comment, id);
            return new Message(Success, "Setted");
        } catch (Exception e) {
            LOGGER.error(login + ": ошибка установки коментария", e);
            return new Message(Error, "Internal error");
        }
    }


    @GetMapping("/record/{id}/{date}")
    public void getFile(@PathVariable String id, @PathVariable String date, HttpServletResponse response) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);

        try {
            LOGGER.trace("Запрос записи звонка ID {}, Date {}", id, date);
            File file = findFile(year, month, day, id);
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            LOGGER.error("Ошибка получения записи ID " + id + " Date " + date, e);
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
