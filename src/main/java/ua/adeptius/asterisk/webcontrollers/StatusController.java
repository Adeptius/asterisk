package ua.adeptius.asterisk.webcontrollers;


import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.dao.MySqlCalltrackDao;
import ua.adeptius.asterisk.dao.MySqlStatisticDao;
import ua.adeptius.asterisk.model.Customer;
import ua.adeptius.asterisk.controllers.MainController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/status")
public class StatusController {

    @RequestMapping(value = "/telephonyinfo", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getTelephonyByName(@RequestParam String name, @RequestParam String password) {
        if (!MainController.isTelephonyLogin(name, password)) {
            return "Error: wrong password";
        }
        try {
            return new ObjectMapper().writeValueAsString(MainController.getTelephonyByName(name));
        } catch (Exception e) {
            return "Error: DB error";
        }
    }


    @RequestMapping(value = "/siteinfo", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getSiteByName(@RequestParam String name, @RequestParam String password) {
        if (!MainController.isSiteLogin(name, password)) {
            return "Error: wrong password";
        }
        try {
            return new ObjectMapper().writeValueAsString(MainController.getSiteByName(name));
        } catch (Exception e) {
            return "Error: DB error";
        }
    }

    @RequestMapping(value = "/history", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getHistory(@RequestParam String name, @RequestParam String dateFrom, @RequestParam String dateTo,
                             @RequestParam String direction, @RequestParam String password) {
        if (!MainController.isLogin(name, password)) {
            return "Error: wrong password";
        }
        direction = direction.toUpperCase();
        if (!direction.equals("IN") && !direction.equals("OUT")) {
            return "Error: wrong direction";
        }
        Customer customer;
        try {
            customer = MainController.getUserByName(name);
        } catch (NoSuchElementException e) {
            return "Error: no such User";
        }

        try {
            return new Gson().toJson(MySqlStatisticDao.getStatisticOfRange(name, dateFrom, dateTo, direction));
        } catch (Exception e) {
            return "Error: DB error";
        }
    }

    @RequestMapping(value = "/getMelodies", method = RequestMethod.GET, produces = "text/html; charset=UTF-8")
    @ResponseBody
    public String getHistory() {
        try {
            return new Gson().toJson(MySqlCalltrackDao.getMelodies());
        } catch (Exception e) {
            return "Error: DB Error";
        }
    }


    @RequestMapping(value = "/record/{id}/{date}", method = RequestMethod.GET)
    public void getFile(@PathVariable String id, @PathVariable String date, HttpServletResponse response) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);

        try {
            File file = findFile(year, month, day, id);
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("File not found");
        }
    }


    private static File findFile(String year, String month, String day, String id) throws Exception {
        Path path = Paths.get("/var/spool/asterisk/monitor/" + year + "/" + month + "/" + day);

        List<File> list = Files.walk(path)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (File file : list) {
            if (file.getName().contains(id)) {
                return file;
            }
        }
        throw new FileNotFoundException();
    }
}
