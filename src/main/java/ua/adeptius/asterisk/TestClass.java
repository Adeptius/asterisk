package ua.adeptius.asterisk;

import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class TestClass {

    public static void main(String[] args) throws Exception {

        User user = HibernateDao.getUserByLogin("e404");
        System.out.println(user);

        OuterPhone needRemove = null;

        Set<OuterPhone> outerPhones = user.getOuterPhones();
//        for (OuterPhone outerPhone : outerPhones) {
//            if ("0443211128".equals(outerPhone.getNumber())){
//                needRemove = outerPhone;
////                outerPhone.setNumber("28");
////                outerPhone.setSitename(null);
//            }
//        }
//
//        if (needRemove != null){
//            user.getOuterPhones().remove(needRemove);
//        }
//
//        HibernateDao.update(user);

        List<OuterPhone> lastElementsFromList = getLastElementsFromList(outerPhones, 5);
        lastElementsFromList.forEach(System.out::println);

    }


    private static List<OuterPhone> getLastElementsFromList(Set<OuterPhone> setFrom, int count) {
        return setFrom.stream()
                .sorted(Comparator.comparing(OuterPhone::getNumber))
                .limit(count)
                .collect(Collectors.toList());
    }


    private static List<String> getLastElementsFromList(List<String> listFrom, int count) {
        ArrayList<String> lastElements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lastElements.add(listFrom.remove(listFrom.size() - 1));
        }
        return lastElements;
    }
}