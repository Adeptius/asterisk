package ua.adeptius.asterisk.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.Statistic;
import ua.adeptius.asterisk.tracking.MainController;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("Duplicates")
@Controller
@RequestMapping("/converter")
public class WebConverter {


    @RequestMapping(value = "/siteinfo", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getSiteByName(@RequestParam String name, @RequestParam String password) {
        if (isPasswordWrong(name,password)){
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
            if (s.startsWith(site)){
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
        if (isPasswordWrong(name,password)){
            return "Wrong password";
        }
        if (!direction.equals("IN") && !direction.equals("OUT")){
            return "Wrong direction";
        }
        try {
            if (MainController.sites.stream().map(Site::getName).anyMatch(s -> s.equals(name))) {
                List<Statistic> list = Main.sitesDao.getStatisticOfRange(name, dateFrom, dateTo, direction);
                Collections.reverse(list);
                StringBuilder builder = new StringBuilder();
                builder.append(
                        "    <tr>\n" +
                                "        <th>Дата</th>\n" +
                                "        <th>Звонок на</th>\n" +
                                "        <th>От</th>\n" +
                                "        <th>До ответа (с)</th>\n" +
                                "        <th>Время (с)</th>\n" +
                                "        <th>Google id</th>\n" +
                                "        <th>UTM</th>\n" +
                                "        <th>Запись</th>\n" +
                                "    </tr>\n" +
                                "    \n");
                for (Statistic statistic : list) {
                    builder.append("<tr>\n            ");
                    builder.append("<td>" + statistic.getDate() + "</td>");
                    builder.append("<td>" + statistic.getTo() + "</td>");
                    builder.append("<td>" + statistic.getFrom() + "</td>");
                    builder.append("<td>" + statistic.getTimeToAnswerForWebInSeconds() + "</td>");
                    builder.append("<td>" + statistic.getTalkingTime() + "</td>");
                    builder.append("<td>" + statistic.getGoogleId() + "</td>");
                    builder.append("<td>" + statistic.getRequest().replaceAll("&", " ") + "</td>");
                    if (!statistic.getTimeToAnswerForWebInSeconds().equals("Недозвон")){
                        builder.append("<td>" + getButton(statistic.getDate(), statistic.getCallUniqueId()) + "</td>");
                    }else {
                        builder.append("<td>" + "</td>");
                    }
                    builder.append("</tr>\n");
                }
                return builder.toString();
            }
        } catch (Exception e) {
            return "Ошибка";
        }
        return "Ошибка";
    }


    private static String getButton(String date, String id){
        String form = "<form name=\"forma1\" action=\"URL\">\n" +
                "<button type=\"submit\">Скачать</button>\n" +
                "</form>";

        String url = "/tracking/status/record/"+id+"/"+date;

        form = form.replaceAll("URL",url);
        return form;
    }

    @RequestMapping(value = "/getblacklist", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getLogs(@RequestParam String name,
                          @RequestParam String password) {
        if (isPasswordWrong(name, password)){
            return "Wrong password";
        }
        Site site = MainController.getSiteByName(name);
        List<String> list = site.getBlackIps();
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }



    private static boolean isPasswordWrong(String sitename, String password){
        String currentSitePass = MainController.getSiteByName(sitename).getPassword();
        if (password.equals(currentSitePass)){
            return false;
        }
        if (password.equals(AdminController.ADMIN_PASS)){
            return false;
        }
        return true;
    }
}
