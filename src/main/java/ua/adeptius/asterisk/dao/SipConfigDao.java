package ua.adeptius.asterisk.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.telephony.SipConfig;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;

public class SipConfigDao {

    private static Logger LOGGER =  LoggerFactory.getLogger(SipConfigDao.class.getSimpleName());

    private static String folder = Settings.getSetting("___sipConfigsFolder");

    public static void writeToFile(SipConfig sipConfig) throws Exception {
        LOGGER.trace("Запись SIP конфига в файл {}", sipConfig.getNumber());
        BufferedWriter writer = new BufferedWriter(new FileWriter(folder + sipConfig.getNumber() + ".conf"));
            writer.write(sipConfig.getConfig());
        writer.close();
    }


    public static void synchronizeFilesAndDb() throws Exception{
        LOGGER.debug("Синхронизация SIP конфигов с БД");
        HashMap<String, String> dbSips = PhonesDao.getAllSipsAndPass();
        Path path = Paths.get(folder);

        List<File> list = Files.walk(path).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());

        List<String> files = new ArrayList<>(); // Текущий список имён файлов

        for (File file : list) {  //Суём имена всех файлов в список files
            files.add(file.getName().substring(0,file.getName().indexOf(".")));
        }

        for (Map.Entry<String, String> entry : dbSips.entrySet()) { // создаём недостающие
            if(!files.contains(entry.getKey())){
                SipConfig sip = new SipConfig(entry.getKey(), entry.getValue());
                writeToFile(sip);
            }
        }
        int deleted = 0;

        for (String file : files) {
            if (!dbSips.containsKey(file)){
                deleted++;
                removeFile(file);
            }
        }
        MyLogger.log(DB_OPERATIONS, "Синхронизация БД и файлов конфигов sip номеров. Всего конфигов: " + dbSips.size() + ", удалено " + deleted);
    }

    public static void removeFile(String number) throws Exception {
        LOGGER.trace("Удаление SIP конфига {}", number);
        Files.deleteIfExists(Paths.get(folder + number + ".conf"));
    }

    public static void removeTelephonyConfigFiles(List<String> numbers) {
        for (String s : numbers) {
            try {
                removeFile(s);
            }catch (Exception e){
                e.printStackTrace();
                LOGGER.error("Ошибка удаления файла конфига "+s, e);
                MyLogger.log(DB_OPERATIONS, "Ошибка удаления "+s+".conf");
            }
        }
    }
}
