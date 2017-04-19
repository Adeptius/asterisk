package ua.adeptius.asterisk.newmodel;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class HibernateDao {

    private static SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();

    public static List<User> getAllUsers() {
        Session session = sessionFactory.openSession();
        List<User> list = session.createQuery("select e from User e").list();
        session.close();
        return list;
    }

    @Transactional
    private void createEntity() {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();

        User user = new User();
        user.setLogin("e404");
        user.setEmail("adeptius@gmail.com");
        user.setPassword("123");
        user.setTrackingId("someId");

        Site site = new Site();
        site.setLogin("e404");
        site.setSiteNumbersCount(2);
        site.setTimeToBlock(60);
        site.setUser(user);

        user.setSite(site);

        session.save(user);
        session.getTransaction().commit();
        session.close();
    }

}
