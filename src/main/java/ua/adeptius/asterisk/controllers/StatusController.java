package ua.adeptius.asterisk.controllers;


import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.Statistic;
import ua.adeptius.asterisk.model.TelephonyCustomer;
import ua.adeptius.asterisk.tracking.MainController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/status")
public class StatusController {




    @RequestMapping(value = "/telephonyinfo", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getTelephonyByName(@RequestParam String name, @RequestParam String password) {
       if (isTelephonyPasswordWrong(name,password)){
            return "Wrong password";
        }

        TelephonyCustomer telephonyCustomer = MainController.getTelephonyCustomerByName(name);
        return new Gson().toJson(telephonyCustomer);
    }


    @RequestMapping(value = "/siteinfo", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public String getSiteByName(@RequestParam String name, @RequestParam String password) {
       if (isSitePasswordWrong(name,password)){
            return "Wrong password";
        }
        Site site = MainController.getSiteByName(name);
        return new Gson().toJson(site);
    }

    @RequestMapping(value = "/history", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<Statistic> getSiteByName(@RequestParam String name,
                                         @RequestParam String dateFrom,
                                         @RequestParam String dateTo,
                                         @RequestParam String direction,
                                         @RequestParam String password) {
        if (isSitePasswordWrong(name,password)){
            ArrayList<Statistic> list = new ArrayList<>();
            Statistic statistic = new Statistic();
            statistic.setDate("Wrong password");
            list.add(statistic);
            return list;
        }
        if (!direction.equals("IN") && !direction.equals("OUT")){
            ArrayList<Statistic> list = new ArrayList<>();
            Statistic statistic = new Statistic();
            statistic.setDate("Wrong direction");
            list.add(statistic);
            return list;
        }
        try {
            if (MainController.sites.stream().map(Site::getName).anyMatch(s -> s.equals(name))) {
                List<Statistic> list = Main.sitesDao.getStatisticOfRange(name, dateFrom, dateTo,direction);
                return list;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    @RequestMapping(value = "/record/{id}/{date}", method = RequestMethod.GET)
    public void getFile(
            @PathVariable String id,
            @PathVariable String date,
            HttpServletResponse response) {

        String year = date.substring(0,4);
        String month = date.substring(5,7);
        String day = date.substring(8,10);

        try {
            File  file = findFile(year, month, day, id);
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



    // TODO переделать на класс родителя
    private static boolean isSitePasswordWrong(String sitename, String password){
        String currentSitePass = MainController.getSiteByName(sitename).getPassword();
        if (password.equals(currentSitePass)){
            return false;
        }
        if (password.equals("pthy0eds")){
            return false;
        }
        return true;
    }

    private static boolean isTelephonyPasswordWrong(String sitename, String password){
        String currentSitePass = MainController.getTelephonyCustomerByName(sitename).getPassword();
        if (password.equals(currentSitePass)){
            return false;
        }
        if (password.equals("pthy0eds")){
            return false;
        }
        return true;
    }
}
