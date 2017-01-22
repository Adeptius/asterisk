package com.luxoft.webapplication.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class WebController {

    @Autowired
    private DBController dbController;

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext() {
        return "TEXT";
    }
}
