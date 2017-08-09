package ua.adeptius.asterisk.dao;


import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateSessionFactory {

    private static Logger LOGGER =  LoggerFactory.getLogger(HibernateSessionFactory.class.getSimpleName());

    private static SessionFactory sessionFactory = buildSessionFactory();

    protected static SessionFactory buildSessionFactory() { // todo переделать на аннотации

        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            StandardServiceRegistryBuilder.destroy( registry );
            LOGGER.error("Hibernate: фабрика cессий вылетела", e);
            throw new ExceptionInInitializerError("Initial SessionFactory failed" + e);
        }
        return sessionFactory;
    }


    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
//         Close caches and connection pools
        LOGGER.debug("Hibernate: фабрика сессий закрывается");
        getSessionFactory().close();
    }
}
