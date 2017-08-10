package ua.adeptius.asterisk;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.model.User;

import java.util.List;

public class TestClass {

    public static void main(String[] args) throws Exception {

        ApplicationContext context = new ClassPathXmlApplicationContext("appconfig-root.xml");
        HibernateDao bean = context.getBean(HibernateDao.class);
        List<User> allUsers = bean.getAllUsers();
        allUsers.forEach(System.out::println);

//        ApplicationContext context = new AnnotationConfigWebApplicationContext();
//        context.re
//        HibernateDao bean = context.getBean(HibernateDao.class);
//        List<User> allUsers = bean.getAllUsers();
//        allUsers.forEach(System.out::println);



    }
}