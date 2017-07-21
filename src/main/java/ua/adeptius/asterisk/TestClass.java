package ua.adeptius.asterisk;

import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.telephony.ForwardType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ua.adeptius.asterisk.telephony.DestinationType.SIP;

public class TestClass {

    public static void main(String[] args) throws Exception {
        User user = HibernateDao.getUserByLogin("e404");
        Telephony telephony = HibernateDao.getTelephonyByUser(user);
        System.out.println(telephony);
    }
}
