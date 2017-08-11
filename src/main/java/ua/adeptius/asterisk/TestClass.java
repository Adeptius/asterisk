package ua.adeptius.asterisk;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.model.User;

import java.util.List;

public class TestClass {



    public static void main(String[] args) throws Exception {

        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");

//        String[] beanDefinitionNames = context.getBeanDefinitionNames();
//        for (String beanDefinitionName : beanDefinitionNames) {
//            System.out.println(beanDefinitionName);
//        }
//        SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
//        System.out.println(sessionFactory);


//        Session session = sessionFactory.openSession();
//        User e404 = session.get(User.class, "e404");
//        session.close();
//        System.out.println(e404);

//        TestClass testClass = new TestClass();
//        testClass.test();
    }


    private void test(){
        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");
//        System.out.println(sessionFactory);
    }
}