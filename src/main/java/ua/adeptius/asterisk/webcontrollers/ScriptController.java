package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.dao.Settings;

import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/script")
public class ScriptController {

    private static Logger LOGGER =  LoggerFactory.getLogger(ScriptController.class.getSimpleName());


    @RequestMapping(value = "/get", method = RequestMethod.POST, produces = {"application/json; charset=UTF-8"})
    @ResponseBody
    public String getScript(HttpServletRequest request) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid").toString();
        }
        if (user.getTracking() == null){
            return new Message(Message.Status.Error, "User have not tracking").toString();
        }
        String name = user.getLogin();
        String script = "<script src=\\\"https://"
                + Settings.getSetting("SERVER_ADDRESS_FOR_SCRIPT")
                + "/tracking/script/"
                + name
                + "\\\"></script>";

        // Локальный хост
//        String script = "<script src=\\\"http://localhost:8080/tracking/script/" + name + "\\\"></script>";

        return new Message(Message.Status.Success, script).toString();
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getScript2(@PathVariable String name) {
        Tracking tracking = null;
        try {
            tracking = UserContainer.getSiteByName(name);
            String script = "function loadScript(url,callback){var head=document.getElementsByTagName('head')[0];var script=document.createElement('script');" +
                    "script.type='text/javascript';script.src=url;script.onreadystatechange=callback;" +
                    "script.onload=callback;head.appendChild(script)}var runMyCodeAfterJQueryLoaded=function(){(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;" +
                    "i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];" +
                    "a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');" +
                    "ga('create','GOOGLETRACKINGID','auto');ga('send','pageview');$(document)" +
                    ".ready(function(){$.getJSON(\"https://api.ipify.org?format=json\",function(data){var ip=''+data.ip;if (ip.search(',')>0){ip = ip" +
                    ".substring(0, ip.indexOf(','));}var match=document.cookie.match('(?:^|;)\\\\s*_ga=([^;]*)');" +
                    "var raw=(match)?decodeURIComponent(match[1]):null;if(raw){match=raw.match(/(\\d+\\.\\d+)$/)}var gacid=(match)?match[1]:null;" +
                    "var sPageURL=decodeURIComponent(window.location.search.substring(1));if(sPageURL==''){sPageURL='null'}someRequest();" +
                    "function someRequest(){var url='https://SERVERADDRESS/tracking/SITENAME/getnumber/'+gacid+'/'+ip+'/'+sPageURL+'/';" +
                    "$.get(url,function(phone){$('.ct-phone').html(phone)});setTimeout(someRequest,TIMETOUPDATE000)}})})};" +
                    "loadScript(\"https://code.jquery.com/jquery-1.12.4.min.js\",runMyCodeAfterJQueryLoaded);";



//             Локальный хост
//  String script = "function loadScript(url,callback){var head=document.getElementsByTagName('head')[0];var script=document.createElement('script');" +
//                    "script.type='text/javascript';script.src=url;script.onreadystatechange=callback;" +
//                    "script.onload=callback;head.appendChild(script)}var runMyCodeAfterJQueryLoaded=function(){(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;" +
//                    "i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];" +
//                    "a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');" +
//                    "ga('create','GOOGLETRACKINGID','auto');ga('send','pageview');$(document)" +
//                    ".ready(function(){$.getJSON(\"https://api.ipify.org?format=json\",function(data){var ip=''+data.ip;if (ip.search(',')>0){ip = ip" +
//                    ".substring(0, ip.indexOf(','));}var match=document.cookie.match('(?:^|;)\\\\s*_ga=([^;]*)');" +
//                    "var raw=(match)?decodeURIComponent(match[1]):null;if(raw){match=raw.match(/(\\d+\\.\\d+)$/)}var gacid=(match)?match[1]:null;" +
//                    "var sPageURL=decodeURIComponent(window.location.search.substring(1));if(sPageURL==''){sPageURL='null'}someRequest();" +
//                    "function someRequest(){var url='http://localhost:8080/tracking/SITENAME/getnumber/'+gacid+'/'+ip+'/'+sPageURL+'/';" +
//                    "$.get(url,function(phone){$('.ct-phone').html(phone)});setTimeout(someRequest,TIMETOUPDATE000)}})})};" +
//                    "loadScript(\"https://code.jquery.com/jquery-1.12.4.min.js\",runMyCodeAfterJQueryLoaded);";

            script = script.replaceAll("SERVERADDRESS",Settings.getSetting("SERVER_ADDRESS_FOR_SCRIPT"));
            script = script.replaceAll("SITENAME", tracking.getLogin());
            script = script.replaceAll("GOOGLETRACKINGID", tracking.getUser().getTrackingId());
            script = script.replaceAll("TIMETOUPDATE",Settings.getSetting("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE"));
            return script;
        } catch (NoSuchElementException e) {
            MyLogger.log(LogCategory.DB_OPERATIONS, name + " не найден в БД");
            return "Not found in db";
        }
    }
}
