package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.Main;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.utils.Settings;

import javax.servlet.http.HttpServletResponse;


@Controller
public class WebController {


    private DBController dbController = Main.dbController;

    @RequestMapping(value = "/asterisk/getnumber/{googleid}", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext(@PathVariable String googleid, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", Settings.accessControlAllowOrigin);
        return dbController.getFreePhone(googleid);
    }

    @RequestMapping(value = "/asterisk/getnumber", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext2(@RequestParam("id") String id) {
        return dbController.getFreePhone(id);
    }
}
