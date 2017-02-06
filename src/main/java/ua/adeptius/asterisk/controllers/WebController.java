package ua.adeptius.asterisk.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.model.Site;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class WebController {

    @RequestMapping(value = "/{sitename}/getnumber/{googleid}/{ip}/{pagerequest}", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext(@PathVariable String sitename,
                                          @PathVariable String googleid,
                                          @PathVariable String ip,
                                          @PathVariable String pagerequest,
                                          HttpServletResponse response,
                                          HttpServletRequest request) {
//                                          ,

        Site site = MainController.getSiteByName(sitename);
        String phone = MainController.getFreeNumberFromSite(site, googleid, ip, pagerequest);

        String accessControlAllowOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
        return phone;
    }


    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String test() {
        return "It works!!";
    }



}
