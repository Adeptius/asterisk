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
}
