package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.model.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {


    public static List<String> findTablesThatNeedToDelete(List<Site> sites, List<String> tables){

        List<String> tablesToDelete = new ArrayList<>();
        List<String> sitesAlreadyHave = sites.stream().map(Site::getName).collect(Collectors.toList());

        for (String table : tables) {
            String siteNameFromTable = table.substring(10);

            if (!sitesAlreadyHave.contains(siteNameFromTable)){
                tablesToDelete.add(table);
            }
        }
        return tablesToDelete;
    }


    public static List<String> findTablesThatNeedToCreate(List<Site> sites, List<String> tables) {
        List<String> sitesAlreadyHave = sites.stream().map(Site::getName).collect(Collectors.toList());
        List<String> sitesNeedToCreate = new ArrayList<>();

        for (String s : sitesAlreadyHave) {
            if (!tables.contains("statistic_"+s)){
                sitesNeedToCreate.add(s);
            }
        }
        return sitesNeedToCreate;
    }


    public static String getScriptForSite(Site site){
        String s = "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js\"></script>\n<script>(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');ga('create','GOOGLETRACKINGID','auto');ga('send','pageview');$(document).ready(function(){$.getJSON(\"http://jsonip.com/?callback=?\",function(data){var ip=''+data.ip;var match=document.cookie.match('(?:^|;)\\\\s*_ga=([^;]*)');var raw=(match)?decodeURIComponent(match[1]):null;if(raw){match=raw.match(/(\\d+\\.\\d+)$/)}var gacid=(match)?match[1]:null;someRequest();function someRequest(){var url='http://SERVERADDRESS/SITENAME/getnumber/'+gacid+'/'+ip+'/';$.get(url,function(phone){$('#phone').html(phone)});setTimeout(someRequest,TIMETOUPDATE000)}})});</script>";

        s = s.replaceAll("SERVERADDRESS",Settings.getSetting("SERVER_ADDRESS_FOR_SCRIPT"));
        s = s.replaceAll("SITENAME", site.getName());
        s = s.replaceAll("GOOGLETRACKINGID",site.getGoogleAnalyticsTrackingId());
        s = s.replaceAll("TIMETOUPDATE",Settings.getSetting("SECONDS_TO_UPDATE_PHONE_ON_WEB_PAGE"));

        return s;
    }
}
