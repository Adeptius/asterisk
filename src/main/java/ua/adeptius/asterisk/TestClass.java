package ua.adeptius.asterisk;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;
import java.util.Random;


@SuppressWarnings("Duplicates")
@Component
@EnableWebMvc
public class TestClass {



    public static void main(String[] args) throws Exception {
        TestClass testClass = new TestClass();
        testClass.test();
    }

    private static Random random = new Random();

    private void test() throws Exception {
//        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");

    }
}