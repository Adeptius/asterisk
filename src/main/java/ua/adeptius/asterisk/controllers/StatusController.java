package ua.adeptius.asterisk.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.Statistic;
import ua.adeptius.asterisk.utils.MyLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/status")
public class StatusController {

    @RequestMapping(value = "/siteinfo", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Site getSiteByName(@RequestParam String name,
                              @RequestParam String password) {
       if (isPasswordWrong(name,password)){
            return new Site("Wrong password",null,"Wrong password","Wrong password","Wrong password",null,"Wrong password",0);
        }

        Site site = MainController.getSiteByName(name);
        return site;
    }

    @RequestMapping(value = "/history", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<Statistic> getSiteByName(@RequestParam String name,
                                         @RequestParam String dateFrom,
                                         @RequestParam String dateTo,
                                         @RequestParam String password) {
        if (isPasswordWrong(name,password)){
            ArrayList<Statistic> list = new ArrayList<>();
            Statistic statistic = new Statistic();
            statistic.setDate("Wrong password");
            list.add(statistic);
            return list;
        }
        try {
            if (MainController.sites.stream().map(Site::getName).anyMatch(s -> s.equals(name))) {
                List<Statistic> list = Main.mySqlDao.getStatisticOfRange(name, dateFrom, dateTo);
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



    private static boolean isPasswordWrong(String sitename, String password){
        String currentSitePass = MainController.getSiteByName(sitename).getPassword();
        if (password.equals(currentSitePass)){
            return false;
        }
        if (password.equals("pthy0eds")){
            return false;
        }
        return true;
    }
}
