package ua.adeptius.asterisk.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.dao.SipConfigDao;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Telephony;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.telephony.SipConfig;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.*;
import java.util.stream.Collectors;

public class PhonesController {

    private static Logger LOGGER =  LoggerFactory.getLogger(PhonesController.class.getSimpleName());


    public static void createMoreSipNumbers(int number) throws Exception{
        LOGGER.debug("Создание дополнительно {} sip номеров", number);
        int max = PhonesDao.getMaxSipNumber();

        for (int i = 0; i < number; i++) {
            SipConfig sipConfig = new SipConfig(++max + "");
            PhonesDao.saveSipToDB(sipConfig);
            SipConfigDao.writeToFile(sipConfig);
        }
    }

    public static void increaseOrDecrease(int needCount, List<String> currentList, String name, boolean innerTable) throws Exception{
        if (needCount > currentList.size()){ // Если номеров недостаточно
            LOGGER.debug("{}: номеров недостаточно. Нужно {}, есть {}", name, needCount, currentList.size());
            int needMoreNumbers = needCount - currentList.size();
            ArrayList<String> availableNumbers = PhonesDao.getFreePhones(innerTable); // взяли все свободные номера
            ArrayList<String> preparedNumbers = new ArrayList<>();
            if (availableNumbers.size()<needMoreNumbers && !innerTable){ // Убедились, что их достаточно
                LOGGER.debug("Номеров недостаточно! Есть {}, нужно {}", availableNumbers.size(), needMoreNumbers);
                throw new NotEnoughNumbers();
            }else if (availableNumbers.size()<needMoreNumbers && innerTable){
                createMoreSipNumbers(needMoreNumbers);
                availableNumbers = PhonesDao.getFreePhones(innerTable);
            }
            for (int i = 0; i < needMoreNumbers; i++) { // берём нужное количество
                preparedNumbers.add(availableNumbers.get(i));
            }
            PhonesDao.markNumbersBusy(preparedNumbers, name, innerTable); // помечаем как занятые
            currentList.addAll(preparedNumbers);
        }else if (needCount < currentList.size()){ // Если номеров больше чем нужно
            LOGGER.debug("Номеров больше чем нужно. Должно быть {}, сейчас {}", needCount, currentList.size());
            int redundantNumbers = currentList.size() - needCount;
            ArrayList<String> numbersToRelease = new ArrayList<>();
            for (int i = 0; i < redundantNumbers; i++) {
                numbersToRelease.add(currentList.remove(currentList.size()-1));
            }
            if (innerTable){// если это сип, то мы их удаляем с базы и конфиг файлы тоже
                PhonesDao.deleteNumbersFromDb(numbersToRelease, true);
                for (String s : numbersToRelease) {
                    SipConfigDao.removeFile(s);
                }
            }else {
                PhonesDao.markNumberFree(numbersToRelease, innerTable);
            }
        }else {
            LOGGER.debug("{}: номеров нужное количество. Список не меняется", name);
        }
    }

    public static void releaseAllCustomerNumbers(User user) throws Exception{
        LOGGER.debug("{}: Освобождаем все номера пользователя", user);
        if (user.getTracking() !=null){
            releaseAllTrackingNumbers(user.getTracking());
        }
        if (user.getTelephony() != null){
            releaseAllTelephonyNumbers(user.getTelephony());
        }
    }

    public static void releaseAllTrackingNumbers(Tracking tracking) throws Exception{
        PhonesDao.markNumberFree(tracking.getPhones().stream().map(Phone::getNumber)
                .collect(Collectors.toList()),false);
    }

    public static void releaseAllTelephonyNumbers(Telephony telephony) throws Exception{
        PhonesDao.markNumberFree(telephony.getOuterPhonesList(),false);
        PhonesDao.deleteNumbersFromDb(telephony.getInnerPhonesList(), true);
        SipConfigDao.removeTelephonyConfigFiles(telephony.getInnerPhonesList());
    }

    public static void scanAndClean() throws Exception{
        LOGGER.debug("Начало очистки БД на предмет занятых номеров несуществующими пользователями");
        HashMap<String, String> innerMap = PhonesDao.getBusyInnerPhones();
        HashMap<String, String> outerMap = PhonesDao.getBusyOuterPhones();
        List<String> users = UserContainer.getUsers().stream().map(User::getLogin).collect(Collectors.toList());
        List<String> innerToClean = new ArrayList<>();
        List<String> outerToClean = new ArrayList<>();
        for (Map.Entry<String, String> entry : innerMap.entrySet()) {
            if (!users.contains(entry.getValue())){
                innerToClean.add(entry.getKey());
            }
        }
        for (Map.Entry<String, String> entry : outerMap.entrySet()) {
            // busyBy - получаем значение после прочерка (trac_newUser или tele_newUser)
            String busyBy = entry.getValue().substring(entry.getValue().indexOf("_")+1);
            if (!users.contains(busyBy)){
                outerToClean.add(entry.getKey());
            }
        }
        PhonesDao.markNumberFree(innerToClean,true);
        PhonesDao.markNumberFree(outerToClean,false);
        SipConfigDao.removeTelephonyConfigFiles(innerToClean);
        LOGGER.debug("\"Синхронизация БД освобождено номеров внешних {}, внутренних {}", outerToClean.size() ,innerToClean.size());
        MyLogger.log(LogCategory.DB_OPERATIONS, "Синхронизация БД освобождено номеров внешних " + outerToClean.size() + ", внутренних " + innerToClean.size());
    }
}
