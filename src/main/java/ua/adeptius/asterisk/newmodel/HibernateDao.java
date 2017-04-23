package ua.adeptius.asterisk.newmodel;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class HibernateDao {

    private static SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
    private static Session session;


    public static List<User> getAllUsers() throws Exception {
        session = sessionFactory.openSession();
        List<User> list = session.createQuery("select e from User e").list();
        session.close();
        return list;
    }


    public static void saveUser(User user) throws Exception {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(user);

        session.getTransaction().commit();
        session.close();
    }


    public static void update(User user) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.update(user);

        session.getTransaction().commit();
        session.close();
    }

    public static void removeTelephony(User user) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(user.getTelephony());
        user.setTelephony(null);
        session.update(user);

        session.getTransaction().commit();
        session.close();
    }

    public static void removeTracking(User user) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(user.getTracking());
        user.setTracking(null);
        session.update(user);

        session.getTransaction().commit();
        session.close();
    }

    public static void deleteUser(String username) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        User user = session.get(User.class, username);
        user.setTelephony(null);
        user.setTracking(null);
        session.delete(user);

        session.getTransaction().commit();
        session.close();
    }
}
