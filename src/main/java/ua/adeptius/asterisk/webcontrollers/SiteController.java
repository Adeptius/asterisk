package ua.adeptius.asterisk.webcontrollers;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.json.JsonSite;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;

@Controller
@ResponseBody
@RequestMapping(value = "/sites", produces = "application/json; charset=UTF-8")
public class SiteController {
    //    private static boolean safeMode = true;
    private static Logger LOGGER =  LoggerFactory.getLogger(SiteController.class.getSimpleName());

    @PostMapping("/add")
    public Object add(@RequestBody JsonSite jsonSite, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String newName = jsonSite.getName();
        String newStandardNumber = jsonSite.getStandardNumber();
        Integer newtimeToBlock = jsonSite.getTimeToBlock();

        if (!MyStringUtils.validateThatContainsOnlyEngLettersAndNumbers(newName)){
            return new Message(Message.Status.Error, "Invalid name, must contains only english letter or numbers");
        }


        if (StringUtils.isBlank(newStandardNumber)) {
            return new Message(Message.Status.Error, "Wrong standart number");
        }

        if (newtimeToBlock == null || newtimeToBlock == 0) {
            newtimeToBlock = 120;
        }


        Site site = new Site();
        site.setUser(user);
        site.setName(newName);
        site.setStandardNumber(newStandardNumber);
        site.setTimeToBlock(newtimeToBlock);

        try {
            user.getSites().add(site);
            HibernateDao.update(user);
            return new Message(Message.Status.Success, "Site added");
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка добавление сайта: "+jsonSite, e);
            return new Message(Message.Status.Error, "Internal error");
        }
//        finally {
//            if (safeMode)
//                user.reloadTrackingFromDb();
//        }
    }

    @PostMapping("/edit")
    public Object edit(HttpServletRequest request, @RequestBody JsonSite jsonSite) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String jName = jsonSite.getName();
        String jStandardNumber = jsonSite.getStandardNumber();
        Integer jTimeToBlock = jsonSite.getTimeToBlock();

        Site site = user.getSiteByName(jName);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site");
        }

        if (!StringUtils.isBlank(jStandardNumber)) {
            site.setStandardNumber(jStandardNumber);
        }

        if (jTimeToBlock != null && jTimeToBlock != 0) {
            site.setTimeToBlock(jTimeToBlock);
        }

        try {
            HibernateDao.update(user);
            return new Message(Message.Status.Success, "Site updated");
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка обновления сайта " + jsonSite, e);
            return new Message(Message.Status.Error, "Internal error");
        }
//        finally {todo
//            if (safeMode)
//                user.reloadTrackingFromDb();
//        }
    }

    @PostMapping("/remove")
    public Object removeSite(HttpServletRequest request, @RequestParam String siteName) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }
        Site site = user.getSiteByName(siteName);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site");
        }

        try {
            site.releaseAllPhones();
            user.getSites().remove(site);
            HibernateDao.update(user);
            return new Message(Message.Status.Success, "Site removed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка удаления трекинга", e);
            return new Message(Message.Status.Error, "Internal error");
        }
//        finally {todo
//            if (safeMode)
//                user.reloadTrackingFromDb();
//        }
    }
//
//
//    @PostMapping("/get")
//    public String getBlackList(HttpServletRequest request) {
//        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
//        if (user == null) {
//            return new Message(Message.Status.Error, "Authorization invalid").toString();
//        }
//        if (user.getTracking() == null) {
//            return new Message(Message.Status.Error, "User have no tracking").toString();
//        }
//
//        try {
//            return new ObjectMapper().writeValueAsString(user.getTracking());
//        } catch (Exception e) {
//            LOGGER.error(user.getLogin()+": ошибка получения пользователя", e);
//            return new Message(Message.Status.Error, "Internal error").toString();
//        }
//    }
}
