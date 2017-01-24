package com.luxoft.webapplication.controllers;


import com.luxoft.webapplication.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class WebController {


    private DBController dbController = Main.dbController;

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext() {
        return "TEXT";
    }


    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext2() {
        return "TEXT";
    }
}
