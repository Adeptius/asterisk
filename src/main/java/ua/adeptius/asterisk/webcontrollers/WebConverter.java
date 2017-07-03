package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.NewCall;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/converter")
public class WebConverter {

    private static Logger LOGGER =  LoggerFactory.getLogger(WebConverter.class.getSimpleName());

    @RequestMapping(value = "/siteinfo", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getSiteByName(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have not tracking").toString();
        }

        Tracking tracking = user.getTracking();

        StringBuilder builder = new StringBuilder();
        builder.append(
                "    <tr>\n" +
                        "        <th>Телефон</th>\n" +
                        "        <th>Google ID</th>\n" +
                        "        <th>IP</th>\n" +
                        "        <th>Время</th>\n" +
                        "    </tr>\n" +
                        "    \n");
        for (Phone phone : tracking.getPhones()) {
            builder.append("<tr>\n            ");
            builder.append("<td class='statusNumber'>" + phone.getNumber() + "</td>");
            builder.append("<td>" + phone.getGoogleId() + "</td>");
            builder.append("<td>" + phone.getIp() + "</td>");
            builder.append("<td>" + phone.getBusyTimeText() + "</td>");
            builder.append("</tr>\n");
        }
        return builder.toString();
    }

    @RequestMapping(value = "/logs", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getLogs2(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        LinkedList<String> list = MyLogger.logs;
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (s.startsWith(user.getLogin())) {
                sb.append("*").append(s.substring(s.indexOf(" "))).append("\n");
            }
        }
        return sb.toString();
    }

    @RequestMapping(value = "/history", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getSiteByName(@RequestParam String dateFrom,
                                @RequestParam String dateTo,
                                @RequestParam String direction,
                                HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        if (!direction.equals("IN") && !direction.equals("OUT")) {
            return "Error: wrong direction";
        }

        try {
            List<NewCall> calls = MySqlStatisticDao.getStatisticOfRange(user.getLogin(), dateFrom, dateTo, direction);

            Collections.reverse(calls);
            StringBuilder builder = new StringBuilder();
            builder.append(
                    "    <tr>\n" +
                            "        <th width=\"15%\">Дата</th>\n" +
                            "        <th width=\"8%\">От</th>\n" +
                            "        <th width=\"8%\">Кому</th>\n" +
                            "        <th width=\"8%\">Состояние</th>\n" +
                            "        <th width=\"8%\">До ответа (с)</th>\n" +
                            "        <th width=\"6%\">Время (с)</th>\n" +
                            "        <th width=\"12%\">Google id</th>\n" +
                            "        <th >UTM</th>\n" +
                            "        <th width=\"8%\">Запись</th>\n" +
                            "    </tr>\n" +
                            "    \n");
//            for (Call call : calls) {
//                builder.append("<tr>\n            ");
//                builder.append("<td>" + call.getCalled() + "</td>");
//                builder.append("<td>" + call.getFrom() + "</td>");
//                builder.append("<td>" + call.getTo() + "</td>");
//                builder.append("<td>" + call.getCallState() + "</td>");
//                builder.append("<td>" + call.getAnswered() + "</td>");
//                builder.append("<td>" + call.getEnded() + "</td>");
//                builder.append("<td>" + call.getGoogleId() + "</td>");
//                builder.append("<td>" + call.getUtm().replaceAll("&", " ") + "</td>");
//                if (call.getCallState() == Call.CallState.ANSWERED) {
//                    builder.append("<td>" + getButton(call.getCalled(), call.getId()) + "</td>");
//                } else {
//                    builder.append("<td>" + "</td>");
//                }
//                builder.append("</tr>\n");
//            }
            return builder.toString();
        } catch (Exception e) {
            return "Error: internal Error";
        }
    }


    private static String getButton(String date, String id) {
        String form = "<form name=\"forma1\" action=\"URL\">\n" +
                "<button type=\"submit\">Скачать</button>\n" +
                "</form>";

        String url = "/tracking/history/record/" + id + "/" + date;

        form = form.replaceAll("URL", url);
        return form;
    }


    @RequestMapping(value = "/getblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getLogs(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() == null) {
            return new Message(Message.Status.Error, "User have not tracking").toString();
        }
        List<String> list = user.getTracking().getBlackList();
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
}
