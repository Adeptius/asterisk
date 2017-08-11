package ua.adeptius.asterisk.dao;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.*;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class HibernateDaoTest {

    private static Logger LOGGER = LoggerFactory.getLogger("-=TESTING=-");

    private static HibernateController hibernateController;

    @BeforeClass
    public static void preparingDb() throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");
        hibernateController = context.getBean(HibernateController.class);

    }

    private static void createTestUser() throws Exception {
        User user = new User();
        user.setLogin("hibernate");
        user.setEmail("hibernate@mail.com");
        user.setPassword("password");
        user.setTrackingId("hibernateId");
        hibernateController.saveUser(user);
    }

    private static void addSiteToUser(User user) {
        Site site = new Site();
        site.setName("hiber");
        site.setTimeToBlock(120);
        site.setStandardNumber("5551112");
        site.setUser(user);
        user.getSites().add(site);
        hibernateController.update(user);
    }

    @Before
    public void prepareUser() throws Exception {
        LOGGER.info("Подготавливаем тестового пользователя для нового теста");
        User user = hibernateController.getUserByLogin("hibernate");
        if (user == null) {
            createTestUser();
            user = hibernateController.getUserByLogin("hibernate");
        }
        assertNotNull("User is null!", user);

        if (user.getSiteByName("hiber") == null) {
            addSiteToUser(user);
        }
        user = hibernateController.getUserByLogin("hibernate");
        assertTrue(user.getSiteByName("hiber") != null);

        hibernateController.removeAllTestPhones();
        List<OuterPhone> allTestPhones = hibernateController.getAllTestPhones();
        assertEquals(0, allTestPhones.size());

        hibernateController.createTestPhonesForUser("hibernate", "hiber");
        user = hibernateController.getUserByLogin("hibernate");
        Site hiberSite = user.getSiteByName("hiber");
        assertEquals(3, user.getOuterPhones().size());
        assertEquals(2, hiberSite.getOuterPhones().size());

        // теперь у нас юзер с тремя телефонами, сайтом, и привязанному к нему двумя номерами

        user.setRoistatAccount(null);
        user.setAmoAccount(null);
        hibernateController.update(user);




    }

    /**
     * Тест на удаление сайта. Номера должны отвязатся от сайта, но остаться у пользователя
     */
    @Test
    public void testDeleteSite() throws Exception {
        User user = hibernateController.getUserByLogin("hibernate");
        Site site = user.getSiteByName("hiber");
        user.removeSite(site);
        hibernateController.update(user);

        user = hibernateController.getUserByLogin("hibernate");
        Set<OuterPhone> outerPhones = user.getOuterPhones();
        assertEquals("Внешние номера не должны удалятся", 3, outerPhones.size());
        for (OuterPhone outerPhone : outerPhones) {
            assertNull("Номера не отвязались от сайта", outerPhone.getSitename());
        }
    }

    /**
     * При удалении пользователя внешние номера должны освобождатся, но не удалятся
     */
    @Test
    public void testRemoveUser() throws Exception {
        User user = hibernateController.getUserByLogin("hibernate");
        hibernateController.delete(user);
        user = hibernateController.getUserByLogin("hibernate");
        assertNull("Пользователь не удалился", user);
        List<OuterPhone> testPhones = hibernateController.getAllTestPhones();
        assertEquals("Внешние номера удалились при удалении пользователя", 3, testPhones.size());
        for (OuterPhone phone : testPhones) {
            assertNull(phone.getSitename());
            assertNull(phone.getBusy());
        }
    }

    @Test
    public void testUsersProperties() throws Exception {
        User user = hibernateController.getUserByLogin("hibernate");
        user.setPassword("newPass");
        user.setTrackingId("newTrackId");
        user.setEmail("newMail");
        hibernateController.update(user);

        user = hibernateController.getUserByLogin("hibernate");
        assertEquals("Пароль пользователя не сохраняется", "newPass", user.getPassword());
        assertEquals("Tracking id пользователя не сохраняется", "newTrackId", user.getTrackingId());
        assertEquals("Email пользователя не сохраняется", "newMail", user.getEmail());
    }

    @Test
    public void amoAccountTest() throws Exception {
        User user = hibernateController.getUserByLogin("hibernate");
        assertNull(user.getAmoAccount());

        AmoAccount amoAccount = new AmoAccount();
        amoAccount.setAmoLogin("amoLogin");
        amoAccount.setDomain("amoDomain");
        amoAccount.setPhoneEnumId("phoneEnumId");
        amoAccount.setApiKey("apiKey");
        amoAccount.setPhoneId("phoneId");
        user.setAmoAccount(amoAccount);
        hibernateController.update(user);

        user = hibernateController.getUserByLogin("hibernate");
        amoAccount = user.getAmoAccount();
        assertNotNull("Амо аккаунт не сохраняется", amoAccount);
        assertEquals("amoLogin", amoAccount.getAmoLogin());
        assertEquals("amoDomain", amoAccount.getDomain());
        assertEquals("phoneEnumId", amoAccount.getPhoneEnumId());
        assertEquals("apiKey", amoAccount.getApiKey());
        assertEquals("phoneId", amoAccount.getPhoneId());

        user.setAmoAccount(null);
        hibernateController.update(user);

        amoAccount = hibernateController.getAmoAccountByUser("hibernate");
        assertNull(amoAccount);
    }


    @Test
    public void roistatAccountTest() throws Exception {
        User user = hibernateController.getUserByLogin("hibernate");
        assertNull(user.getRoistatAccount());

        RoistatAccount roistatAccount = new RoistatAccount();
        roistatAccount.setApiKey("apiKey");
        roistatAccount.setProjectNumber("projectNumber");
        user.setRoistatAccount(roistatAccount);
        hibernateController.update(user);

        user = hibernateController.getUserByLogin("hibernate");
        roistatAccount = user.getRoistatAccount();
        assertNotNull("Roistat аккаунт не сохраняется", roistatAccount);
        assertEquals("apiKey", roistatAccount.getApiKey());
        assertEquals("projectNumber", roistatAccount.getProjectNumber());

        user.setRoistatAccount(null);
        hibernateController.update(user);

        roistatAccount = hibernateController.getRoistatAccountByUser("hibernate");
        assertNull(roistatAccount);
    }


    @Test
    public void siteTest() throws Exception {
        User user = hibernateController.getUserByLogin("hibernate");
        assertEquals(1,user.getSites().size());

        Site site = user.getSiteByName("hiber");
        assertNotNull(site);

        site.addIpToBlackList("78.159.55.63");

        hibernateController.update(user);
        user = hibernateController.getUserByLogin("hibernate");
        site = user.getSiteByName("hiber");
        assertEquals(1, site.getBlackList().size());




    }


    @AfterClass
    public static void cleaningDb() throws Exception {
        User user = hibernateController.getUserByLogin("hibernate");
        if (user != null){
            hibernateController.delete(user);
        }

        AmoAccount amoAccount = hibernateController.getAmoAccountByUser("hibernate");
        if (amoAccount != null){
            hibernateController.delete(amoAccount);
        }

        RoistatAccount roistatAccount = hibernateController.getRoistatAccountByUser("hibernate");
        if (roistatAccount != null) {
            hibernateController.delete(roistatAccount);
        }

        hibernateController.removeAllTestPhones();

    }
}