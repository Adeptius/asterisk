package ua.adeptius.asterisk;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.interceptors.AccessControlOriginInterceptor;
import ua.adeptius.asterisk.model.Email;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.senders.EmailSender;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static ua.adeptius.asterisk.model.Email.EmailType.NO_OUTER_PHONES_LEFT;


@SuppressWarnings("Duplicates")
@Component
@EnableWebMvc
public class TestClass {

    private static Logger LOGGER = LoggerFactory.getLogger(EmailSender.class.getSimpleName());
    private static Logger LOGGER2 = LoggerFactory.getLogger(AccessControlOriginInterceptor.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        TestClass testClass = new TestClass();
        testClass.test();
    }

    private static Random random = new Random();

    private void test() throws Exception {
//        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");



//        String s = "AmoCallSender";
//        System.out.println(makeNameForLogger(s));


        LOGGER.error("Error log");
        LOGGER.warn("Warn log");
        LOGGER.info("Info log");
        LOGGER.debug("Debug log");
        LOGGER.trace("Trace log");

        LOGGER2.error("Error log");
        LOGGER2.warn("Warn log");
        LOGGER2.info("Info log");
        LOGGER2.debug("Debug log");
        LOGGER2.trace("Trace log");



    }


    private String makeNameForLogger(String s){

        int needMoreSpaces = 20-s.length();
        String moreSpaces = "";
        for (int i = 0; i < needMoreSpaces; i++) {
            moreSpaces+= " " ;
        }
        return moreSpaces + s;
    }
}