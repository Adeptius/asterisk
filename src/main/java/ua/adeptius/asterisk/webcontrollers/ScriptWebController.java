package ua.adeptius.asterisk.webcontrollers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

import static ua.adeptius.asterisk.json.Message.Status.Error;


@Controller
@RequestMapping("/script")
@ResponseBody
public class ScriptWebController {

    private static Logger LOGGER = LoggerFactory.getLogger(ScriptWebController.class.getSimpleName());
    private static Settings settings = Main.settings;

    @PostMapping(value = "/get", produces = "application/json; charset=UTF-8")
    public Object getScript(HttpServletRequest request, @RequestParam String siteName) {
        User user = UserContainer.getUserByHash(request.getHeader("Authorization"));
        if (user == null) {
            return new Message(Message.Status.Error, "Authorization invalid");
        }
        Set<Site> sites = user.getSites();
        if (sites == null || sites.isEmpty()) {
            return new Message(Message.Status.Error, "User have no this site");
        }
        Site site = user.getSiteByName(siteName);
        if (site == null){
            return new Message(Message.Status.Error, "User have no this site");
        }

        String serverAddress = settings.getServerAddress();
        String login = user.getLogin();

        String script = "<script src=\"https://"+ serverAddress +"/tracking/script/"+login+"/"+siteName+"\"></script>";
        return new Message(Message.Status.Success, script);
    }

    @GetMapping(value = "/{login}/{site}", produces = "text/html; charset=UTF-8")
    public String getScript2(@PathVariable String login, @PathVariable String site) {
        User user = UserContainer.getUserByName(login);
        if (user == null) {
            return "{\"Status\":\"Error\",\"Message\":\"No such user\"}";
        }
        Site siteObject = user.getSiteByName(site);
        if (siteObject == null) {
            return "{\"Status\":\"Error\",\"Message\":\"No such site\"}";
        }

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
                "function someRequest(){var url='https://SERVERADDRESS/tracking/getnumber/LOGIN/SITENAME/'+gacid+'/'+ip+'/'+sPageURL+'/';" +
                "$.get(url,function(phone){$('.ct-phone').html(phone)});setTimeout(someRequest,TIMETOUPDATE000)}})})};" +
                "loadScript(\"https://code.jquery.com/jquery-1.12.4.min.js\",runMyCodeAfterJQueryLoaded);";

        script = script.replaceAll("SERVERADDRESS", settings.getServerAddress());
        script = script.replaceAll("LOGIN", user.getLogin());
        script = script.replaceAll("SITENAME", siteObject.getName());
        script = script.replaceAll("GOOGLETRACKINGID", user.getTrackingId());
        script = script.replaceAll("TIMETOUPDATE", ""+settings.getSecondsToUpdatePhoneOnWebPage());
        return script;
    }
}
