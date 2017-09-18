package ua.adeptius.asterisk;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.interceptors.AccessControlOriginInterceptor;
import ua.adeptius.asterisk.model.Email;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.Rule;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.model.Email.EmailType.NO_OUTER_PHONES_LEFT;


@SuppressWarnings("Duplicates")
@Component
@EnableWebMvc
public class TestClass {

    private static Logger LOGGER = LoggerFactory.getLogger(EmailSender.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        TestClass testClass = new TestClass();
        testClass.test();
    }

    private void test() throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");


    }
}