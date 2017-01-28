package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.Main;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.utils.Settings;

import javax.servlet.http.HttpServletResponse;


@Controller
public class WebController {


//    private DBController dbController = Main.dbController;

    @RequestMapping(value = "/{sitename}/getnumber/{googleid}", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext(@PathVariable String sitename, @PathVariable String googleid, HttpServletResponse response) {

        String phone = MainController.getFreeNumberFromSite(sitename);

        response.setHeader("Access-Control-Allow-Origin", MainController.getSiteByName(sitename).getAccessControlAllowOrigin());
        return phone;
    }
}
