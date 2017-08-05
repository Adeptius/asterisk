package ua.adeptius.asterisk.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.InnerPhone;
import ua.adeptius.asterisk.model.OuterPhone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PhonesDao {

    private static Logger LOGGER = LoggerFactory.getLogger(PhonesDao.class.getSimpleName());


    public static int getMaxSipNumber() throws Exception {
        return HibernateDao.getSipMaxNumber();
    }

    public static HashMap<String, String> getSipPasswords(String name) throws Exception {
        LOGGER.trace("{}: запрос паролей к сип номерам", name);
        List<InnerPhone> innerPhones = HibernateDao.getAllInnerUserPhones(name);
        HashMap<String, String> map = new HashMap<>();
        for (InnerPhone phone : innerPhones) {
            map.put(phone.getNumber(), phone.getPass());
        }
        return map;
    }

    public static HashMap<String, String> getAllSipsAndPass() throws Exception {
        LOGGER.trace("Запрос всех сип из базы.");
        List<InnerPhone> allInnerPhones = HibernateDao.getAllInnerPhones();
        HashMap<String, String> map = new HashMap<>();
        for (InnerPhone phone : allInnerPhones) {
            map.put(phone.getNumber(), phone.getPass());
        }
        return map;
    }

    public static ArrayList<String> getCustomersInnerNumbers(String name) throws Exception {
        LOGGER.trace("{}: запрос всех номеров пользователя из внутренней таблицы", name);
        List<InnerPhone> innerPhones = HibernateDao.getAllInnerUserPhones(name);
        ArrayList<String> list = new ArrayList<>();
        for (InnerPhone innerPhone : innerPhones) {
            list.add(innerPhone.getNumber());
        }
        return list;
    }

    public static ArrayList<String> getCustomersOuterNumbers(String name) throws Exception {
        LOGGER.trace("{}: запрос всех номеров пользователя из внешней таблицы", name);
        List<OuterPhone> outerPhones = HibernateDao.getAllOuterUsersPhones(name);
        ArrayList<String> list = new ArrayList<>();
        for (OuterPhone outerPhone : outerPhones) {
            list.add(outerPhone.getNumber());
        }
        return list;
    }


    public static HashMap<String, String> getBusyOuterPhones() throws Exception {
        LOGGER.trace("Запрос занятых внешних номеров из БД");
        List<OuterPhone> allBusyOuterPhones = HibernateDao.getAllBusyOuterPhones();
        HashMap<String, String> phones = new HashMap<>();
        for (OuterPhone outerPhone : allBusyOuterPhones) {
            phones.put(outerPhone.getNumber(), outerPhone.getBusy());
        }
        return phones;
    }

    @Deprecated // внутренние номера всегда заняты. И Не работает
    public static HashMap<String, String> getBusyInnerPhones() throws Exception {
        List<InnerPhone> allInnerPhones = HibernateDao.getAllInnerPhones();
        HashMap<String, String> map = new HashMap<>();
        for (InnerPhone phone : allInnerPhones) {
            map.put(phone.getNumber(), phone.getBusy());
        }
        return map;
    }

    public static ArrayList<String> getFreeOuterPhones() throws Exception {
        List<OuterPhone> allFreeOuterPhones = HibernateDao.getAllFreeOuterPhones();
        ArrayList<String> list = new ArrayList<>();
        for (OuterPhone phone : allFreeOuterPhones) {
            list.add(phone.getNumber());
        }
        return list;
    }
}
