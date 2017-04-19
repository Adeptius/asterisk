package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.dao.SipConfigDao;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.newmodel.User;
import ua.adeptius.asterisk.telephony.SipConfig;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.util.*;
import java.util.stream.Collectors;

public class PhonesController {

    public static void createMoreSipNumbers(int number) throws Exception{
        int max = PhonesDao.getMaxSipNumber();

        for (int i = 0; i < number; i++) {
            SipConfig sipConfig = new SipConfig(++max + "");
            PhonesDao.saveSipToDB(sipConfig);
            SipConfigDao.writeToFile(sipConfig);
        }
    }

    public static void increaseOrDecrease(int needCount, List<String> currentList, String name, boolean innerTable) throws Exception{
        if (needCount > currentList.size()){ // Если номеров недостаточно
            int needMoreNumbers = needCount - currentList.size();
            ArrayList<String> availableNumbers = PhonesDao.getFreePhones(innerTable); // взяли все свободные номера
            ArrayList<String> preparedNumbers = new ArrayList<>();
            if (availableNumbers.size()<needMoreNumbers && !innerTable){ // Убедились, что их достаточно
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
            int redundantNumbers = currentList.size() - needCount;
            ArrayList<String> numbersToRelease = new ArrayList<>();
            for (int i = 0; i < redundantNumbers; i++) {
                numbersToRelease.add(currentList.remove(currentList.size()-1));
            }
            if (innerTable){// если это сип, то мы их удаляем с базы и конфиг файлы тоже
                for (String s : numbersToRelease) {
                    SipConfigDao.removeFile(s);
                    PhonesDao.deleteNumbersFromDb(numbersToRelease, true);
                }
            }else {
                PhonesDao.markNumberFree(numbersToRelease, innerTable);
            }
        }
    }

    public static void releaseAllCustomerNumbers(User user) throws Exception{
        List<String> inner = new ArrayList<>();
        List<String> outer = new ArrayList<>();

        if (user.getSite() !=null){
            outer.addAll(user.getSite().getPhones().stream().map(Phone::getNumber).collect(Collectors.toList()));
        }
        if (user.getTelephony() != null){
            outer.addAll(user.getTelephony().getOuterPhonesList());
            inner.addAll(user.getTelephony().getInnerPhonesList());
        }

        PhonesDao.markNumberFree(outer,false);
        PhonesDao.markNumberFree(inner, true);
    }

    public static void scanAndClean() throws Exception{
        HashMap<String, String> innerMap = PhonesDao.getBusyInnerPhones();
        HashMap<String, String> outerMap = PhonesDao.getBusyOuterPhones();
        List<String> users = MainController.users.stream().map(User::getLogin).collect(Collectors.toList());
        List<String> innerToClean = new ArrayList<>();
        List<String> outerToClean = new ArrayList<>();
        for (Map.Entry<String, String> entry : innerMap.entrySet()) {
            if (!users.contains(entry.getValue())){
                innerToClean.add(entry.getKey());
            }
        }
        for (Map.Entry<String, String> entry : outerMap.entrySet()) {
            if (!users.contains(entry.getValue())){
                outerToClean.add(entry.getKey());
            }
        }
        PhonesDao.markNumberFree(innerToClean,true);
        PhonesDao.markNumberFree(outerToClean,false);
        MyLogger.log(LogCategory.DB_OPERATIONS, "Синхронизация БД освобождено номеров внешних " + outerToClean.size() + ", внутренних " + innerToClean.size());
    }
}
