package ua.adeptius.asterisk.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.tracking.TrackingController;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.dao.Settings;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/script")
public class ScriptController {

    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = {"text/html; charset=UTF-8"})
    @ResponseBody
    public String getScript(@PathVariable String name) {
        Site site = null;
        try {
            site = TrackingController.getSiteByName(name);
//            String script = "function loadScript(url,callback){var head=document.getElementsByTagName('head')[0];var script=document.createElement('script');script.type='text/javascript';script.src=url;script.onreadystatechange=callback;script.onload=callback;head.appendChild(script)}var runMyCodeAfterJQueryLoaded=function(){(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');ga('create','GOOGLETRACKINGID','auto');ga('send','pageview');$(document).ready(function(){$.getJSON(\"http://jsonip.com/?callback=?\",function(data){var ip=''+data.ip;if (ip.search(',')>0){ip = ip.substring(0, ip.indexOf(','));}var match=document.cookie.match('(?:^|;)\\\\s*_ga=([^;]*)');var raw=(match)?decodeURIComponent(match[1]):null;if(raw){match=raw.match(/(\\d+\\.\\d+)$/)}var gacid=(match)?match[1]:null;var sPageURL=decodeURIComponent(window.location.search.substring(1));if(sPageURL==''){sPageURL='null'}someRequest();function someRequest(){var url='http://SERVERADDRESS/tracking/SITENAME/getnumber/'+gacid+'/'+ip+'/'+sPageURL+'/';$.get(url,function(phone){$('.ct-phone').html(phone)});setTimeout(someRequest,TIMETOUPDATE000)}})})};loadScript(\"http://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js\",runMyCodeAfterJQueryLoaded);";
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

            script = script.replaceAll("SERVERADDRESS",Settings.getSetting("SERVER_ADDRESS_FOR_SCRIPT"));
            script = script.replaceAll("SITENAME", site.getName());
            script = script.replaceAll("GOOGLETRACKINGID",site.getGoogleAnalyticsTrackingId());
            script = script.replaceAll("TIMETOUPDATE",Settings.getSetting("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE"));
            return script;
        } catch (NoSuchElementException e) {
            MyLogger.log(LogCategory.ELSE, name + " не найден в БД");
            return "Not found in db";
        }
    }
}
