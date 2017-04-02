package ua.adeptius.asterisk.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.tracking.TrackingController;


@Controller
public class WebController {

    @RequestMapping(value = "/{sitename}/getnumber/{googleid}/{ip}/{pagerequest}", method = RequestMethod.GET, produces = {"text/html; charset=UTF-8"})
    public
    @ResponseBody
    String plaintext(@PathVariable String sitename,
                     @PathVariable String googleid,
                     @PathVariable String ip,
                     @PathVariable String pagerequest) {
        Site site = TrackingController.getSiteByName(sitename);
        String phone = TrackingController.getFreeNumberFromSite(site, googleid, ip, pagerequest);
        return convertPhone(phone);
    }


    @RequestMapping(value = "/", method = RequestMethod.GET, produces = {"text/html; charset=UTF-8"})
    public String main() {
        return "main";
    }


    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = {"text/html; charset=UTF-8"})
    public String login() {
        return "login";
    }


    public static String convertPhone(String source){
        if (source.length()>8){
            int len = source.length();
            String s4 = source.substring(len-2, len);
            String s3 = source.substring(len-4, len-2);
            String s2 = source.substring(len-7, len-4);
            String s1 = source.substring(0, len-7);
            return String.format("(%s) %s-%s-%s",s1,s2,s3,s4);
        }
        return source;
    }
}
