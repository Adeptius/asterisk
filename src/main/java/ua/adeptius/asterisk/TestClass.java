package ua.adeptius.asterisk;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonPipeline;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.senders.EmailSender;

import java.util.List;

@SuppressWarnings("Duplicates")
@Component
@EnableWebMvc
public class TestClass {

    private static Logger LOGGER = LoggerFactory.getLogger(EmailSender.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        TestClass testClass = new TestClass();
        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");

        testClass.testAmo();
        testClass.testGroups();
        testClass.testMail();
    }

    private void testAmo() throws Exception {

        User user = new User("WID_TEST", "", "");
        AmoAccount amoAccount = new AmoAccount();
        amoAccount.setAmoLogin("adeptius@wid.ua");
        amoAccount.setApiKey("a99ead2f473e150091360d25aecc2878");
        amoAccount.setDomain("adeptiustest6");
        amoAccount.setUser(user);
//        JsonAmoAccount amoAccount1 = AmoDAO.getAmoAccount(amoAccount);
//        AmoDAO.getDealById(amoAccount, Collections.singletonList("9219307"));
//        JsonAmoContact contactIdByPhoneNumber = AmoDAO.getContactIdByPhoneNumber(amoAccount, "0938095833");
//        System.out.println(amoAccount);

        JsonAmoAccount jsonAmoAccount = AmoDAO.getAmoAccount(amoAccount);
        List<JsonPipeline> pipelines = jsonAmoAccount.getPipelines();
        System.out.println();

        AmoDAO.addNewDealAndGetBackIdAndTime(amoAccount, "Nextel", "0");

//        List<JsonAmoDeal> dealById = AmoDAO.getDealById(amoAccount, Collections.singletonList("4021703"));
    }









    private void testMail() {
//        for (int i = 0; i < 10; i++) {
//            Email email = new Email(NO_OUTER_PHONES_LEFT, "omix@i.ua", "rutracker" + (i+1), "e404");
//            Main.emailSender.send(email);
//            Thread.sleep(15000);
//        }
    }

    private void testGroups() {
//        List<String> phones = Arrays.asList("111", "222", "333", "444");
//        User user = HibernateController.getUserByLogin("e404");
//        PhoneGroup phoneGroup = new PhoneGroup();
//        phoneGroup.setUser(user);
//        phoneGroup.setPhones(phones);
//        phoneGroup.setName("Менеджеры");
//        user.addOrReplacePhoneGroup(phoneGroup);

//        PhoneGroup next = user.getPhoneGroups().iterator().next();
//        user.removePhoneGroup(next);


//        HibernateController.update(user);
//        user = HibernateController.getUserByLogin("e404");
//        Set<PhoneGroup> phoneGroups = user.getPhoneGroups();
//        System.out.println("--------groups--------");
//        for (PhoneGroup group : phoneGroups) {
//            System.out.println(group);
//        }
//        System.out.println("------end groups------");
    }
}