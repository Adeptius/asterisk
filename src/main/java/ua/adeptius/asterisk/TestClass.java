package ua.adeptius.asterisk;

import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.model.*;

import java.util.*;

public class TestClass {

    public static void main(String[] args) throws Exception {

        User user = HibernateDao.getUserByLogin("e404");
        System.out.println(user);
//        AmoOperatorLocation operatorLocation = new AmoOperatorLocation();
//        operatorLocation.setName("операторы");
//        operatorLocation.setBindingString("111=222");
//        operatorLocation.setLogin(user.getLogin());
//        user.getOperatorLocations().remove(user.getOperatorLocations().iterator().next());
//        user.getAmoAccount().getOperatorLocations().add(operatorLocation);
//        user.getAmoAccount().getOperatorLocations().remove(user.getAmoAccount().getOperatorLocations().iterator().next());
//        List<OuterPhone> freeOuterPhones = HibernateDao.getAllFreeOuterPhones();
//        OuterPhone phone = freeOuterPhones.get(0);
//        user.getOuterPhones().add(phone);
//        HibernateDao.update(user);

//        TestClass testClass = new TestClass();

//        System.out.println(testClass.getAmoUserIdAndInnerNumber());

    }


}