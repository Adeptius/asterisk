package ua.adeptius.asterisk.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.InnerPhone;
import ua.adeptius.asterisk.telephony.SipConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class SipConfigDao {

    private static Logger LOGGER =  LoggerFactory.getLogger(SipConfigDao.class.getSimpleName());

    private static String folder = Settings.getSetting("sipConfigsFolder");

    public static void writeToFile(SipConfig sipConfig) throws Exception {
        LOGGER.trace("Запись SIP конфига в файл {}", sipConfig.getNumber());
        BufferedWriter writer = new BufferedWriter(new FileWriter(folder + sipConfig.getNumber() + ".conf"));
            writer.write(sipConfig.getConfig());
        writer.close();
    }


    public static void synchronizeSipFilesAndInnerDb() throws Exception{
        LOGGER.debug("Синхронизация SIP конфигов с БД");
        List<InnerPhone> allInnerPhones = HibernateDao.getAllInnerPhones();
        HashMap<String, String> dbSips = new HashMap<>();
        for (InnerPhone phone : allInnerPhones) {
            dbSips.put(phone.getNumber(), phone.getPass());
        }
//        HashMap<String, String> dbSips = PhonesDao.getAllSipsAndPass();

        // почему-то не работает в папке  /etc/asterisk/sip_clients/
//        Path path = Paths.get(folder);
//        List<File> list = Files.walk(path).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
//        List<String> files = new ArrayList<>(); // Текущий список имён файлов
//        for (File file : list) {  //Суём имена всех файлов в список files
//            files.add(file.getName().substring(0,file.getName().indexOf(".")));
//        }

        List<String> fileList = new ArrayList<>(); //Суём имена всех файлов
        File configsDir = new File(folder);
        File[] files = configsDir.listFiles();
        for (File f : files) {
            String fil = f.getName();
            if (!fil.startsWith("test")) {
                fileList.add(fil.substring(0, fil.indexOf(".")));
            }
        }

        for (Map.Entry<String, String> entry : dbSips.entrySet()) { // создаём недостающие
            if(!fileList.contains(entry.getKey())){
                SipConfig sip = new SipConfig(entry.getKey(), entry.getValue());
                writeToFile(sip);
            }
        }
        int deleted = 0;

        for (String file : fileList) { // удаляем лишние
            if (!dbSips.containsKey(file)){
                deleted++;
                removeFile(file);
            }
        }
        LOGGER.info("Синхронизация БД и файлов конфигов sip номеров. Всего конфигов: {}, удалено {}", dbSips.size(), deleted);
    }



    public static void removeFiles(List<String> numbers){
        LOGGER.trace("Удаление файлов SIP конфигов {}", numbers);
        for (String number : numbers) {
            try {
                removeFile(number);
            }catch (IOException e){
                LOGGER.error("Не удалось удалить конфиг "+number, e);
            }
        }
    }

    private static void removeFile(String number) throws IOException {
        Files.deleteIfExists(Paths.get(folder + number + ".conf"));
    }
}
