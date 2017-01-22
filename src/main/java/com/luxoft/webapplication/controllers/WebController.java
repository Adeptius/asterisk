package com.luxoft.webapplication.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { "text/html; charset=UTF-8" })
    public @ResponseBody String plaintext() {
        return "TEXT";
    }
}
