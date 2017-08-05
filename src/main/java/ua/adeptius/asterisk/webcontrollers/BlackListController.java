package ua.adeptius.asterisk.webcontrollers;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/blacklist")
public class BlackListController {

    private static Logger LOGGER =  LoggerFactory.getLogger(BlackListController.class.getSimpleName());

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String addToBlackList(@RequestParam String ip, String sitename, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Message.Status.Error, "User have no tracking sites").toString();
        }
        try {
            Matcher regexMatcher = Pattern.compile("(\\d{1,3}[.]){3}\\d{1,3}").matcher(ip.trim());
            regexMatcher.find();
            regexMatcher.group();
        }catch (Exception e){
            return new Message(Message.Status.Error, "Wrong ip").toString();
        }
        Site site = user.getSiteByName(sitename);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site").toString();
        }

        site.addIpToBlackList(ip);
        try {
            LOGGER.debug("{}: сайт {} добавление IP {} в черный список",user.getLogin(), site, ip);
            HibernateDao.update(user);
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка добавления IP "+ip+" в черный список", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
        return new Message(Message.Status.Success, "Added").toString();
    }


    @RequestMapping(value = "/remove", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String removeFromBlackList(@RequestParam String ip, String sitename, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }

        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Message.Status.Error, "User have no tracking sites").toString();
        }
        Site site = user.getSiteByName(sitename);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site").toString();
        }

        site.removeIpFromBlackList(ip);

        try {
            LOGGER.debug("{}: сайт {} удаление IP {} из черного списка",user.getLogin(), site, ip);
            HibernateDao.update(user);
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка удаления IP "+ip+" из черного списка", e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
        return new Message(Message.Status.Success, "Removed").toString();
    }


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    @ResponseBody
    public String getBlackList(HttpServletRequest request, String sitename) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Message.Status.Error, "User have no tracking sites").toString();
        }
        Site site = user.getSiteByName(sitename);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site").toString();
        }

        try {
            LinkedList<String> list = site.getBlackList();
            return new Gson().toJson(list);
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка получения черного списка для сайта" + sitename, e);
            return new Message(Message.Status.Error, "Internal error").toString();
        }
    }
}
