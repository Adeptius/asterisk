package ua.adeptius.asterisk;

import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.model.Scenario;
import ua.adeptius.asterisk.model.User;

import java.util.List;

public class TestClass {

    public static void main(String[] args) throws Exception {
        User user = HibernateDao.getUserByLogin("e404");
//        System.out.println(user);

//        UserContainer.setUsers(HibernateDao.getAllUsers());
//        RulesConfigDAO.writeAllNeededScenarios();
//        Scenario scenarioE404 = HibernateDao.getScenarioById(1);


        Scenario scenarioTele3 = HibernateDao.getScenarioById(2);

//        user.addScenario(scenarioTele3);
        user.activateScenario(1);


    }


    // сначала проверю на конфликт по времени, а потом добавлю логику сверки со всеми номерами пользователя

}
