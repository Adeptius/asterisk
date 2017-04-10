package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.exceptions.NotEnogthNumbers;

import java.util.*;

public class PhonesController {

    public static void increaseOrDecrease(int needCount, ArrayList<String> currentList, String name, boolean innerTable) throws Exception{
        if (needCount > currentList.size()){ // Если номеров недостаточно
            int needMoreNumbers = needCount - currentList.size();
            ArrayList<String> availableNumbers = PhonesDao.getFreePhones(innerTable); // взяли все свободные номера
            ArrayList<String> preparedNumbers = new ArrayList<>();
            if (availableNumbers.size()<needMoreNumbers){ // Убедились, что их достаточно
                throw new NotEnogthNumbers();
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
            PhonesDao.markNumberFree(numbersToRelease, innerTable);
        }
    }
}
