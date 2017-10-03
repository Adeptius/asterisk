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


    @GetMapping(value = "/{login}/{site}", produces = "text/html; charset=UTF-8")
    public String getFullScript(@PathVariable String login, @PathVariable String site) {
        User user = UserContainer.getUserByName(login);
        if (user == null) {
            return "Error";
        }
        Site siteObject = user.getSiteByName(site);
        if (siteObject == null) {
            return "Error";
        }


        String serverAddress = settings.getServerAddress();
        String trackingId = siteObject.getGoogleTrackingId();
        if (trackingId == null){
            trackingId = "";
        }
        int secondsToUpdatePhoneOnWebPage = settings.getSecondsToUpdatePhoneOnWebPage();

//        String script = "eval(function(p,a,c,k,e,d){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--){d[e(c)]=k[c]||e(c)}k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c])}}return p}('6 D(G,h){3 e=8.B(\\'e\\')[0];3 5=8.C(\\'5\\');5.O=\\'R/N\\';5.v=G;5.T=h;5.10=h;e.11(5)}3 w=6(){(6(i,s,o,g,r,a,m){i[\\'14\\']=r;i[r]=i[r]||6(){(i[r].q=i[r].q||[]).Z(Y)},i[r].l=1*V X();a=s.C(o),m=s.B(o)[0];a.W=1;a.v=g;m.U.13(a,m)})(z,8,\\'5\\',\\'b://P.J-A.E/A.I\\',\\'p\\');p(\\'Q\\',\\'S\\',\\'M\\');p(\\'K\\',\\'L\\');$(8).15(6(){$.1c(\"b://1t.1o.1p?1v=1q\",6(u){3 7=\\'\\'+u.7;f(7.x(\\',\\')>0){7=7.t(0,7.16(\\',\\'))}3 2=8.1n.2(\\'(?:^|;)\\\\\\\\s*1r=([^;]*)\\');3 n=(2)?y(2[1]):j;f(n){2=n.2(/(\\\\d+\\\\.\\\\d+)$/)}3 H=(2)?2[1]:j;3 9=y(z.1u.x.t(1));f(9==\\'\\'){9=\\'j\\'}k();6 k(){$.1s(\\'b://1l/1b/1m\\',{1a:\\'19\\',17:\\'18\\',1d:H,7:7,1e:9},6(c){$(\\'.1j-c\\').1k(c)});1i(k,1h)}})})};D(\"b://1f.F.E/F-1.12.4.1g.I\",w);',62,94,'||match|var||script|function|ip|document|sPageURL||https|phone||head|if||callback||null|someRequest|||raw||ga||||substring|data|src|runMyCodeAfterJQueryLoaded|search|decodeURIComponent|window|analytics|getElementsByTagName|createElement|loadScript|com|jquery|url|gacid|js|google|send|pageview|auto|javascript|type|www|create|text|GOOGLETRACKINGID|onreadystatechange|parentNode|new|async|Date|arguments|push|onload|appendChild||insertBefore|GoogleAnalyticsObject|ready|indexOf|site|SITENAME|LOGIN|user|tracking|getJSON|googleId|pageRequest|code|min|TIMETOUPDATE000|setTimeout|ct|html|SERVERADDRESS|getNumber|cookie|ipify|org|json|_ga|post|api|location|format'.split('|'),0,{}))";
//        script = script.replaceAll("SERVERADDRESS", serverAddress);
//        script = script.replaceAll("LOGIN", login);
//        script = script.replaceAll("SITENAME", site);
//        script = script.replaceAll("GOOGLETRACKINGID", trackingId);
//        script = script.replaceAll("TIMETOUPDATE", ""+ secondsToUpdatePhoneOnWebPage);
//        return script;

        return "eval(function(p,a,c,k,e,d){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--){d[e(c)]=k[c]||e(c)}k=[function(e){return d[e]}];e=function(){return'\\\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\\\b'+e(c)+'\\\\b','g'),k[c])}}return p}('6 D(G,h){3 e=8.B(\\'e\\')[0];3 5=8.C(\\'5\\');5.O=\\'R/N\\';5.v=G;5.T=h;5.10=h;e.11(5)}3 w=6(){(6(i,s,o,g,r,a,m){i[\\'14\\']=r;i[r]=i[r]||6(){(i[r].q=i[r].q||[]).Z(Y)},i[r].l=1*V X();a=s.C(o),m=s.B(o)[0];a.W=1;a.v=g;m.U.13(a,m)})(z,8,\\'5\\',\\'b://P.J-A.E/A.I\\',\\'p\\');p(\\'Q\\',\\'S\\',\\'M\\');p(\\'K\\',\\'L\\');$(8).15(6(){$.1c(\"b://1t.1o.1p?1v=1q\",6(u){3 7=\\'\\'+u.7;f(7.x(\\',\\')>0){7=7.t(0,7.16(\\',\\'))}3 2=8.1n.2(\\'(?:^|;)\\\\\\\\s*1r=([^;]*)\\');3 n=(2)?y(2[1]):j;f(n){2=n.2(/(\\\\d+\\\\.\\\\d+)$/)}3 H=(2)?2[1]:j;3 9=y(z.1u.x.t(1));f(9==\\'\\'){9=\\'j\\'}k();6 k(){$.1s(\\'b://1l/1b/1m\\',{1a:\\'19\\',17:\\'18\\',1d:H,7:7,1e:9},6(c){$(\\'.1j-c\\').1k(c)});1i(k,1h)}})})};D(\"b://1f.F.E/F-1.12.4.1g.I\",w);',62,94,'||match|var||script|function|ip|document|sPageURL||https|phone||head|if||callback||null|someRequest|||raw||ga||||substring|data|src|runMyCodeAfterJQueryLoaded|search|decodeURIComponent|window|analytics|getElementsByTagName|createElement|loadScript|com|jquery|url|gacid|js|google|send|pageview|auto|javascript|type|www|create|text|"+trackingId+"|onreadystatechange|parentNode|new|async|Date|arguments|push|onload|appendChild||insertBefore|GoogleAnalyticsObject|ready|indexOf|site|"+site+"|"+login+"|user|tracking|getJSON|googleId|pageRequest|code|min|"+secondsToUpdatePhoneOnWebPage+"000|setTimeout|ct|html|"+serverAddress+"|getNumber|cookie|ipify|org|json|_ga|post|api|location|format'.split('|'),0,{}))";
    }
}
