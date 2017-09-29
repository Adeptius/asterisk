package ua.adeptius.asterisk.webcontrollers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.JsonSite;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.OuterPhone;
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

@Controller
@ResponseBody
@RequestMapping(value = "/sites", produces = "application/json; charset=UTF-8")
public class SiteWebController {
    //    private static boolean safeMode = true;
    private static Logger LOGGER = LoggerFactory.getLogger(SiteWebController.class.getSimpleName());
    private boolean safeMode = true;


    @PostMapping("/set")
    public Message set(@RequestBody JsonSite jsonSite, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String siteName = jsonSite.getName();
        String newStandardNumber = jsonSite.getStandardNumber();
        Integer newtimeToBlock = jsonSite.getTimeToBlock();
        ArrayList<String> blackList = jsonSite.getBlackList();

        if (blackList != null && blackList.size() > 100) {
            LOGGER.debug("{}: черный список содержит более 100 записей ", user.getLogin());
            return new Message(Message.Status.Error, "BlackList size limit");
        }

        if (StringUtils.isBlank(siteName) || !MyStringUtils.isLoginValid(siteName)) {
            LOGGER.debug("{}: неправильное название сайта {}", user.getLogin(), jsonSite.getName());
            return new Message(Message.Status.Error, "Invalid name");
        }

        if (StringUtils.isBlank(newStandardNumber)) {
            LOGGER.debug("{}: не указан стандартный номер.", user.getLogin());
            return new Message(Message.Status.Error, "Wrong standart number");
        }
        newStandardNumber = newStandardNumber.replaceAll("\\D+", "");

        if (newtimeToBlock == null || newtimeToBlock == 0) {
            newtimeToBlock = 120;
        }

        // Проверяем свободны ли номера, которые будем привязывать.
        ArrayList<String> futureConnectedPhones = jsonSite.getConnectedPhones();
        if (futureConnectedPhones == null) {
            futureConnectedPhones = new ArrayList<>();
        }


        for (String futureConnectedPhone : futureConnectedPhones) { // проверяем каждый желаемый номер по очереди
            // находим телефон по этому номеру.
            OuterPhone outerPhoneByNumber = user.getOuterPhoneByNumber(futureConnectedPhone);
            if (outerPhoneByNumber == null) { // такого номера у пользователя нет - ошибка на сайте
                return new Message(Message.Status.Error, "Invalid number " + futureConnectedPhone);
            }
            String phoneConnectedSite = outerPhoneByNumber.getSitename();
            if (phoneConnectedSite == null) {// этот телефон свободен - можно будет его привязывать. Продолжаем.
                continue;
            } else { // этот телефон занят. Если этим же сайтом - то все хорошо, а если другим - сообщаем.
                if (!phoneConnectedSite.equals(siteName)) {
                    return new Message(Message.Status.Error, "Number is busy " + futureConnectedPhone);
                }
            }
        }


        Site site = user.getSiteByName(siteName);
        if (site == null) {
            LOGGER.info("{}: запрос добавления сайта {}", user.getLogin(), jsonSite);
            site = new Site();
            site.setName(siteName);
            user.addSite(site);
        } else {
            LOGGER.info("{}: запрос изменения сайта {}", user.getLogin(), jsonSite);
        }

        site.setStandardNumber(convertPhone(newStandardNumber));
        site.setTimeToBlock(newtimeToBlock);

        if (blackList != null) {
            site.setBlackLinkedList(blackList);
        }

        // назначаем номера на сайт. Надо освободить номера, если их число уменьшилось.
        Set<OuterPhone> outerPhones = user.getOuterPhones();
        for (OuterPhone outerPhone : outerPhones) {
            String number = outerPhone.getNumber();
            if (futureConnectedPhones.contains(number)){ // если это тот номер, который мы привязываем
                outerPhone.setSitename(siteName);

            }else { // остальные номера надо проверить. Возможно надо от этого сайта их отвязать.
                String currentSiteName = outerPhone.getSitename();
                if (currentSiteName != null && currentSiteName.equals(siteName)){
                    // если телефон сейчас привязан к этому сайту - отвязываем, так как его нет в списке будущих привязок.
                    outerPhone.setSitename(null);
                }
            }
        }


        for (String futureConnectedPhone : futureConnectedPhones) {
            user.getOuterPhoneByNumber(futureConnectedPhone).setSitename(siteName);
        }

        try {
            HibernateController.update(user);
            LOGGER.debug("{}: сайт добавлен или изменён", user.getLogin());
            return new Message(Message.Status.Success, "Site setted");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка добавление сайта: " + jsonSite, e);
            return new Message(Message.Status.Error, "Internal error");
        }
    }


    @PostMapping("/remove")
    public Message removeSite(HttpServletRequest request, @RequestParam String siteName) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        LOGGER.info("{}: запрос удаления сайта {}", user.getLogin(), siteName);

        Site site = user.getSiteByName(siteName);
        if (site == null) {
            LOGGER.debug("{}: такого сайта у пользователя нет", user.getLogin());
            return new Message(Message.Status.Error, "User have no such site");
        }

        try {
            site.releaseAllPhones();
            user.removeSite(site);
            HibernateController.update(user);
            LOGGER.debug("{}: сайт {} удалён", user.getLogin(), siteName);
            return new Message(Message.Status.Success, "Site removed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления трекинга", e);
            return new Message(Message.Status.Error, "Internal error");
        } finally {
            if (safeMode) {

            }
        }
    }

    @PostMapping("/get")
    public Object getBlackList(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }
        Set<Site> sites = user.getSites();
        if (sites == null || sites.size() == 0) {
            return new Message(Message.Status.Error, "User have no sites");
        }
        return sites;
    }


    @PostMapping("/sendScript")
    public Object sendScriptInstruction(HttpServletRequest request, String email, String comment, String siteName){
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        Site site = user.getSiteByName(siteName);
        if (site == null) {
            LOGGER.debug("{}: такого сайта у пользователя нет", user.getLogin());
            return new Message(Message.Status.Error, "User have no such site");
        }


        String error = "Wrong email";
        String error2 = "Internal error";

        return new Message(Message.Status.Success, "Mail sended");
    }


    public static String convertPhone(String source) {
        if (source.length() > 8) {
            int len = source.length();
            String s4 = source.substring(len - 2, len);
            String s3 = source.substring(len - 4, len - 2);
            String s2 = source.substring(len - 7, len - 4);
            String s1 = source.substring(0, len - 7);
            return String.format("(%s) %s-%s-%s", s1, s2, s3, s4);
        }
        return source;
    }
}
