package ua.adeptius.asterisk.webcontrollers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.DaoHelper;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.model.Customer;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

@SuppressWarnings("Duplicates")
@Controller
@RequestMapping("/converter")
public class WebConverter {


    @RequestMapping(value = "/siteinfo", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getSiteByName(@RequestParam String name, @RequestParam String password) {
        if (!MainController.isSiteLogin(name, password)) {
            return "Wrong password";
        }

        Site site = MainController.getSiteByName(name);
        StringBuilder builder = new StringBuilder();
        builder.append(
                "    <tr>\n" +
                        "        <th>Телефон</th>\n" +
                        "        <th>Google ID</th>\n" +
                        "        <th>IP</th>\n" +
                        "        <th>Время</th>\n" +
                        "    </tr>\n" +
                        "    \n");
        for (Phone phone : site.getPhones()) {
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
    public String getLogs(@RequestParam String site) {
        LinkedList<String> list = MyLogger.logs;
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            if (s.startsWith(site)) {
                sb.append("*").append(s.substring(s.indexOf(" "))).append("\n");
            }
        }
        return sb.toString();
    }

    @RequestMapping(value = "/history", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getSiteByName(@RequestParam String name,
                                @RequestParam String dateFrom,
                                @RequestParam String dateTo,
                                @RequestParam String direction,
                                @RequestParam String password) {
        if (!MainController.isLogin(name, password)) {
            return "Error: wrong password";
        }
        if (!direction.equals("IN") && !direction.equals("OUT")) {
            return "Error: wrong direction";
        }

        Customer customer;
        try {
            customer = MainController.getCustomerByName(name);
        } catch (NoSuchElementException e) {
            return "Error: no such user";
        }

        try {

            List<Call> list = MySqlStatisticDao.getStatisticOfRange(customer.getName(), dateFrom, dateTo, direction);
            Collections.reverse(list);
            StringBuilder builder = new StringBuilder();
            builder.append(
                    "    <tr>\n" +
                            "        <th>Дата</th>\n" +
                            "        <th>Направление</th>\n" +
                            "        <th>От</th>\n" +
                            "        <th>Звонок на</th>\n" +
                            "        <th>Состояние</th>\n" +
                            "        <th>До ответа (с)</th>\n" +
                            "        <th>Время (с)</th>\n" +
                            "        <th>Google id</th>\n" +
                            "        <th>UTM</th>\n" +
                            "        <th>Запись</th>\n" +
                            "    </tr>\n" +
                            "    \n");
            for (Call call : list) {
                builder.append("<tr>\n            ");
                builder.append("<td>" + call.getCalled() + "</td>");
                builder.append("<td>" + call.getDirection() + "</td>");
                builder.append("<td>" + call.getFrom() + "</td>");
                builder.append("<td>" + call.getTo() + "</td>");
                builder.append("<td>" + call.getCallState() + "</td>");
                builder.append("<td>" + call.getAnswered() + "</td>");
                builder.append("<td>" + call.getEnded() + "</td>");
                builder.append("<td>" + call.getGoogleId() + "</td>");
                builder.append("<td>" + call.getUtm().replaceAll("&", " ") + "</td>");
                if (call.getCallState() == Call.CallState.ANSWERED) {
                    builder.append("<td>" + getButton(call.getCalled(), call.getId()) + "</td>");
                } else {
                    builder.append("<td>" + "</td>");
                }
                builder.append("</tr>\n");
            }
            return builder.toString();
        } catch (Exception e) {
            return "Error: internal error";
        }
    }


    private static String getButton(String date, String id) {
        String form = "<form name=\"forma1\" action=\"URL\">\n" +
                "<button type=\"submit\">Скачать</button>\n" +
                "</form>";

        String url = "/tracking/status/record/" + id + "/" + date;

        form = form.replaceAll("URL", url);
        return form;
    }

    @RequestMapping(value = "/getblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getLogs(@RequestParam String name,
                          @RequestParam String password) {
        if (!MainController.isSiteLogin(name, password)) {
            return "Error: wrong password";
        }
        Site site;
        try {
            site = MainController.getSiteByName(name);
        } catch (NoSuchElementException e) {
            return "Error: no such user";
        }
        List<String> list = site.getBlackIps();
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
}
