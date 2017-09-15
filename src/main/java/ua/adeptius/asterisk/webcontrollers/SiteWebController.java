package ua.adeptius.asterisk.webcontrollers;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

@Controller
@ResponseBody
@RequestMapping(value = "/sites", produces = "application/json; charset=UTF-8")
public class SiteWebController {
    //    private static boolean safeMode = true;
    private static Logger LOGGER =  LoggerFactory.getLogger(SiteWebController.class.getSimpleName());
    private boolean safeMode = true;


    @PostMapping("/add")
    public Message add(@RequestBody JsonSite jsonSite, HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String newName = jsonSite.getName();
        String newStandardNumber = jsonSite.getStandardNumber().replaceAll("\\D+", "");;
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

        if (user.getSiteByName(newName) != null){
            return new Message(Message.Status.Error, "Site already exists");
        }


        Site site = new Site();
        site.setName(newName);
        site.setStandardNumber(convertPhone(newStandardNumber));
        site.setTimeToBlock(newtimeToBlock);


        try {
            user.addSite(site);
            HibernateController.update(user);
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
    public Message edit(HttpServletRequest request, @RequestBody JsonSite jsonSite) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }

        String jName = jsonSite.getName();
        String jStandardNumber = jsonSite.getStandardNumber().replaceAll("\\D+", "");
        Integer jTimeToBlock = jsonSite.getTimeToBlock();

        Site site = user.getSiteByName(jName);
        if (site == null) {
            return new Message(Message.Status.Error, "User have no such site");
        }

        if (!StringUtils.isBlank(jStandardNumber)) {
            site.setStandardNumber(convertPhone(jStandardNumber));
        }

        if (jTimeToBlock != null && jTimeToBlock != 0) {
            site.setTimeToBlock(jTimeToBlock);
        }

        try {
            HibernateController.update(user);
            return new Message(Message.Status.Success, "Site updated");
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка обновления сайта " + jsonSite, e);
            return new Message(Message.Status.Error, "Internal error");
        }
        finally {
            if (safeMode){

            }
        }
    }

    @PostMapping("/remove")
    public Message removeSite(HttpServletRequest request, @RequestParam String siteName) {
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
            HibernateController.update(user);
            return new Message(Message.Status.Success, "Site removed");
        } catch (Exception e) {
            LOGGER.error(user.getLogin()+": ошибка удаления трекинга", e);
            return new Message(Message.Status.Error, "Internal error");
        }
        finally {
            if (safeMode){

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
//        try {
//            String s = new ObjectMapper().writeValueAsString(sites);
//            return s;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "error";
//        }
        return sites;
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
