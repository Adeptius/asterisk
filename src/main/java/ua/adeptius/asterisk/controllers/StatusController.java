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

    @RequestMapping(value = "/site/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Site getSiteByName(@PathVariable String name,
                              HttpServletResponse response,
                              HttpServletRequest request) {
        String accessControlAllowOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
        Site site = MainController.getSiteByName(name);
        return site;
    }

    @RequestMapping(value = "/site/getall", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String[] getAllNameOfSites(HttpServletResponse response, HttpServletRequest request) {
        String accessControlAllowOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
        List<String> list = MainController.sites.stream().map(Site::getName).collect(Collectors.toList());
        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    @RequestMapping(value = "/logs", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public LinkedList<String> getLogs(HttpServletResponse response, HttpServletRequest request) {
        String accessControlAllowOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
        return MyLogger.logs;
    }

    @RequestMapping(value = "/history", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<Statistic> getSiteByName(@RequestParam String name,
                                         @RequestParam String dateFrom,
                                         @RequestParam String dateTo,
                                         HttpServletResponse response,
                                         HttpServletRequest request) {
        String accessControlAllowOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);

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

    @RequestMapping(value = "/record/{id}/{year}/{month}/{day}", method = RequestMethod.GET)
    public void getFile(
            @PathVariable String id,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            HttpServletResponse response) {

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
}
