package ua.adeptius.asterisk.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.model.Site;

import javax.servlet.http.HttpServletResponse;


@Controller
public class WebController {

    @RequestMapping(value = "/{sitename}/getnumber/{googleid}/{ip}", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext(@PathVariable String sitename,
                                          @PathVariable String googleid,
                                          @PathVariable String ip,
                                          HttpServletResponse response) {

        Site site = MainController.getSiteByName(sitename);
        String phone = MainController.getFreeNumberFromSite(site, googleid, ip);
        String accessControlAllowOrigin = site.getAccessControlAllowOrigin();

        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
        return phone;
    }
}
