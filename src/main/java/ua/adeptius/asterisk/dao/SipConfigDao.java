package ua.adeptius.asterisk.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.InnerPhone;
import ua.adeptius.asterisk.model.telephony.SipConfig;
import ua.adeptius.asterisk.monitor.Scheduler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class SipConfigDao {

    private static Logger LOGGER =  LoggerFactory.getLogger(SipConfigDao.class.getSimpleName());

    private static String folder = Main.settings.getFolderSips();

    public static void synchronizeSipFilesAndInnerDb() throws Exception{
        LOGGER.debug("Синхронизация SIP конфигов с БД");
        List<InnerPhone> allInnerPhones = HibernateController.getAllInnerPhones();
        HashMap<String, String> dbSips = new HashMap<>();
        for (InnerPhone phone : allInnerPhones) {
            dbSips.put(phone.getNumber(), phone.getPass());
        }

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
                writeFile(sip);
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

    public static List<InnerPhone> createMoreSipNumbers(int number, String user) throws Exception {
        LOGGER.debug("Создание дополнительно {} sip номеров", number);
        int max = PhonesDao.getMaxSipNumber();
        List<InnerPhone> createdNumbers = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            String newSipNumber = ++max + "";
            SipConfig sipConfig = new SipConfig(newSipNumber);
            createdNumbers.add(HibernateController.saveSipBySipConfig(sipConfig, user));
            SipConfigDao.writeFile(sipConfig);
        }
        return createdNumbers;
    }

    public static void removeSipNumbersConfigs(List<InnerPhone> redutrantNumbers) {
        removeFiles(redutrantNumbers.stream().map(InnerPhone::getNumber).collect(Collectors.toList()));
    }

    public static void removeAllInnerNumbersConfigFiles(User user) throws Exception {
        removeFiles(
                user.getInnerPhones().stream()
                        .map(InnerPhone::getNumber)
                        .collect(Collectors.toList())
        );
    }

    private static void removeFiles(List<String> numbers){
        LOGGER.trace("Удаление файлов SIP конфигов {}", numbers);
        for (String number : numbers) {
            try {
                removeFile(number);
            }catch (IOException e){
                LOGGER.error("Не удалось удалить конфиг "+number, e);
            }
        }
    }

    private static void writeFile(SipConfig sipConfig) throws Exception {
        LOGGER.trace("Запись SIP конфига в файл {}", sipConfig.getNumber());
        BufferedWriter writer = new BufferedWriter(new FileWriter(folder + sipConfig.getNumber() + ".conf"));
        writer.write(sipConfig.getConfig());
        writer.close();

        Scheduler.reloadSipOnNextScheduler();
    }

    public static void removeFile(String number) throws IOException {
        Files.deleteIfExists(Paths.get(folder + number + ".conf"));
        Scheduler.reloadSipOnNextScheduler();
    }
}
