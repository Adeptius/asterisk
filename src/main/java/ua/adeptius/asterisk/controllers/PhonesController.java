package ua.adeptius.asterisk.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.dao.SipConfigDao;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.telephony.SipConfig;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class PhonesController {

    private static Logger LOGGER = LoggerFactory.getLogger(PhonesController.class.getSimpleName());
    private static HibernateController hibernateController;

    @Autowired
    public void setHibernateController(HibernateController controller) {
        hibernateController = controller;
    }

    public static List<InnerPhone> createMoreSipNumbers(int number, String user) throws Exception {
        LOGGER.debug("Создание дополнительно {} sip номеров", number);
        int max = PhonesDao.getMaxSipNumber();
        List<InnerPhone> createdNumbers = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            String newSipNumber = ++max + "";
            SipConfig sipConfig = new SipConfig(newSipNumber);
            createdNumbers.add(hibernateController.saveSipBySipConfig(sipConfig, user));
            SipConfigDao.writeToFile(sipConfig);
        }
        return createdNumbers;
    }

    public static void removeAllInnerNumbersConfigFiles(User user) throws Exception {
        SipConfigDao.removeFiles(
                user.getInnerPhones().stream()
                        .map(InnerPhone::getNumber)
                        .collect(Collectors.toList())
        );
    }

    public static void removeSipNumbersConfigs(List<InnerPhone> redutrantNumbers) {
        SipConfigDao.removeFiles(redutrantNumbers.stream().map(InnerPhone::getNumber).collect(Collectors.toList()));
    }


//    public static void increaseOrDecreaseInnerList(int needCount, List<String> currentList, String name) throws Exception {
//        if (needCount > currentList.size()) { // Если номеров недостаточно
//            LOGGER.debug("{}: Внутренних номеров недостаточно. Нужно {}, есть {}", name, needCount, currentList.size());
//            int needMoreNumbers = needCount - currentList.size();
//            List<String> newSipNumbers = createMoreSipNumbers(needMoreNumbers, name);
//            currentList.addAll(newSipNumbers);
//
//        } else if (needCount < currentList.size()) { // Если номеров больше чем нужно
//            LOGGER.debug("Внутренних Номеров больше чем нужно. Должно быть {}, сейчас {}", needCount, currentList.size());
//            int redundantNumbers = currentList.size() - needCount;
//            List<String> numbersToRelease = getLastElementsFromList(currentList, redundantNumbers);
//            HibernateDao.removeInnerPhone(numbersToRelease);// удаляем их с базы и конфиг файлы тоже
//            SipConfigDao.removeFiles(numbersToRelease);
//
//        } else {
//            LOGGER.debug("{}: Внутренних номеров нужное количество. Список не меняется", name);
//        }
//    }

//    public static void increaseOrDecreaseOuterList(int needCount, List<String> currentList, String name) throws Exception {
//        if (needCount > currentList.size()) { // Если номеров недостаточно
//            LOGGER.debug("{}: внешних номеров недостаточно. Нужно {}, есть {}", name, needCount, currentList.size());
//            int needMoreNumbers = needCount - currentList.size();
//            ArrayList<String> availableNumbers = PhonesDao.getFreeOuterPhones(); // взяли все свободные номера
//            ArrayList<String> preparedNumbers = new ArrayList<>();
//            if (availableNumbers.size() < needMoreNumbers) { // Если, их не достаточно
//                LOGGER.debug("Внешних номеров недостаточно! Есть {}, нужно {}", availableNumbers.size(), needMoreNumbers);
//                throw new NotEnoughNumbers();
//            }
//            for (int i = 0; i < needMoreNumbers; i++) { // берём нужное количество
//                preparedNumbers.add(availableNumbers.get(i));
//            }
//            HibernateDao.markOuterPhoneBusy(name, preparedNumbers); // помечаем как занятые
//            currentList.addAll(preparedNumbers);
//
//        } else if (needCount < currentList.size()) { // Если номеров больше чем нужно
//            LOGGER.debug("Внешних номеров больше чем нужно. Должно быть {}, сейчас {}", needCount, currentList.size());
//            int redundantNumbers = currentList.size() - needCount;
//            List<String> numbersToRelease = getLastElementsFromList(currentList, redundantNumbers);
//            HibernateDao.markOuterPhoneFree(numbersToRelease);
//
//        } else {
//            LOGGER.debug("{}: внешних номеров нужное количество. Список не меняется", name);
//        }
//    }

//    private static List<String> getLastElementsFromList(List<String> listFrom, int count) {
//        ArrayList<String> lastElements = new ArrayList<>();
//        for (int i = 0; i < count; i++) {
//            lastElements.add(listFrom.remove(listFrom.size() - 1));
//        }
//        return lastElements;
//    }

//    public static void releaseAllCustomerNumbers(User user) throws Exception {
//        LOGGER.debug("{}: Освобождаем все номера пользователя", user);
//
//        if (user.getTracking() != null) {
//            releaseAllTrackingNumbers(user.getTracking());
//        }
//        if (user.getTelephony() != null) {
//            releaseAllTelephonyNumbers(user.getTelephony());
//        }
//    }

//    public static void releaseAllTrackingNumbers(Tracking tracking) throws Exception {
//        HibernateDao.markOuterPhoneFree(tracking.getOldPhones().stream().map(OldPhone::getNumber).collect(Collectors.toList()));
//    }

    // Это не нужно так как теперь при удалении обьекта - номера освобождаются автоматически хибернейтом.
//    public static void scanAndClean() throws Exception {
//        LOGGER.debug("Начало очистки БД на предмет занятых номеров несуществующими пользователями");
//        HashMap<String, String> innerMap = PhonesDao.getBusyInnerPhones();
//        HashMap<String, String> outerMap = PhonesDao.getBusyOuterPhones();
//        List<String> users = UserContainer.getUsers().stream().map(User::getLogin).collect(Collectors.toList());
//        List<String> innerToClean = new ArrayList<>();
//        List<String> outerToClean = new ArrayList<>();
//        for (Map.Entry<String, String> entry : innerMap.entrySet()) {
//            if (!users.contains(entry.getValue())) {
//                innerToClean.add(entry.getKey());
//            }
//        }
//        for (Map.Entry<String, String> entry : outerMap.entrySet()) {
//            // busyBy - получаем значение после прочерка (trac_newUser или tele_newUser)
//            String busyBy = entry.getValue().substring(entry.getValue().indexOf("_") + 1);
//            if (!users.contains(busyBy)) {
//                outerToClean.add(entry.getKey());
//            }
//        }
//
//        HibernateDao.removeInnerPhone(innerToClean);
//        HibernateDao.markOuterPhoneFree(outerToClean);
//        SipConfigDao.removeFiles(innerToClean);
//        LOGGER.debug("\"Синхронизация БД освобождено номеров внешних {}, внутренних {}", outerToClean.size(), innerToClean.size());
//    }
}
