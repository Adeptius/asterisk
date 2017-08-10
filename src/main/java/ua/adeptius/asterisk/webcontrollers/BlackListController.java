package ua.adeptius.asterisk.webcontrollers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "/blacklist", produces = "application/json; charset=UTF-8")
@ResponseBody
public class BlackListController {

    private static Logger LOGGER = LoggerFactory.getLogger(BlackListController.class.getSimpleName());

    @PostMapping("/add")
    public Object addToBlackList(String ip, String siteName, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Message.Status.Error, "User have no tracking sites");
        }
        try {
            Matcher regexMatcher = Pattern.compile("(\\d{1,3}[.]){3}\\d{1,3}").matcher(ip.trim());
            regexMatcher.find();
            regexMatcher.group();
        } catch (Exception e) {
            return new Message(Message.Status.Error, "Wrong ip");
        }
        Site site = user.getSiteByName(siteName);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site");
        }

        site.addIpToBlackList(ip);
        try {
            LOGGER.debug("{}: сайт {} добавление IP {} в черный список", user.getLogin(), site, ip);



            HibernateDao.update(user); //  Оптимизация: это затратно по ресурсам ---------------------------------------




        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка добавления IP " + ip + " в черный список", e);
            return new Message(Message.Status.Error, "Internal error");
        }
        return new Message(Message.Status.Success, "Added");
    }


    @PostMapping("/remove")
    public Object removeFromBlackList(String ip, String siteName, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Message.Status.Error, "User have no such site");
        }
        Site site = user.getSiteByName(siteName);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site");
        }

        site.removeIpFromBlackList(ip);

        try {
            LOGGER.debug("{}: сайт {} удаление IP {} из черного списка", user.getLogin(), site, ip);
            HibernateDao.update(user);
        } catch (Exception e) {
            LOGGER.error(user.getLogin() + ": ошибка удаления IP " + ip + " из черного списка", e);
            return new Message(Message.Status.Error, "Internal error");
        }
        return new Message(Message.Status.Success, "Removed");
    }


    @PostMapping("/get")
    public Object getBlackList(HttpServletRequest request, String siteName) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }
        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Message.Status.Error, "User have no such site");
        }
        Site site = user.getSiteByName(siteName);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site");
        }

        return site.getBlackList();
    }
}
