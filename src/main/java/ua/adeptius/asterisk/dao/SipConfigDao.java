package ua.adeptius.asterisk.dao;


import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.telephony.SipConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SipConfigDao {

    private static String folder = Settings.getSetting("___sipConfigsFolder");

    public static void writeToFile(SipConfig sipConfig) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(folder + sipConfig.getNumber() + ".conf"));
            writer.write(sipConfig.getConfig());
        writer.close();
    }


    public static void synchronizeFilesAndDb() throws Exception{
        HashMap<String, String> dbSips = PhonesDao.getAllSipsAndPass();
        Path path = Paths.get(folder);

        List<File> list = Files.walk(path)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        List<String> files = new ArrayList<>();

        for (File file : list) {
            files.add(file.getName().substring(0,file.getName().indexOf(".")));
        }

        for (Map.Entry<String, String> entry : dbSips.entrySet()) { // создаём недостающие
            if(!files.contains(entry.getKey())){
                SipConfig sip = new SipConfig(entry.getKey(), entry.getValue());
                writeToFile(sip);
            }
        }

        for (String file : files) {
            if (!dbSips.containsKey(file)){
                removeFile(file);
            }
        }
    }

    public static void removeFile(String number) throws Exception {
        Files.deleteIfExists(Paths.get(folder + number + ".conf"));
    }
}
