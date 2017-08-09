package ua.adeptius.asterisk.dao;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.*;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class HibernateDaoTest {

    private static Logger LOGGER = LoggerFactory.getLogger("-=TESTING=-");

    private static void createTestUser() throws Exception {
        User user = new User();
        user.setLogin("hibernate");
        user.setEmail("hibernate@mail.com");
        user.setPassword("password");
        user.setTrackingId("hibernateId");
        HibernateDao.saveUser(user);
    }

    private static void addSiteToUser(User user) {
        Site site = new Site();
        site.setName("hiber");
        site.setTimeToBlock(120);
        site.setStandardNumber("5551112");
        site.setUser(user);
        user.getSites().add(site);
        HibernateDao.update(user);
    }

    @Before
    public void prepareUser() throws Exception {
        LOGGER.info("Подготавливаем тестового пользователя для нового теста");
        User user = HibernateDao.getUserByLogin("hibernate");
        if (user == null) {
            createTestUser();
            user = HibernateDao.getUserByLogin("hibernate");
        }
        assertNotNull("User is null!", user);

        if (user.getSiteByName("hiber") == null) {
            addSiteToUser(user);
        }
        user = HibernateDao.getUserByLogin("hibernate");
        assertTrue(user.getSiteByName("hiber") != null);

        HibernateDao.removeAllTestPhones();
        List<OuterPhone> allTestPhones = HibernateDao.getAllTestPhones();
        assertEquals(0, allTestPhones.size());

        HibernateDao.createTestPhonesForUser("hibernate", "hiber");
        user = HibernateDao.getUserByLogin("hibernate");
        Site hiberSite = user.getSiteByName("hiber");
        assertEquals(3, user.getOuterPhones().size());
        assertEquals(2, hiberSite.getOuterPhones().size());

        // теперь у нас юзер с тремя телефонами, сайтом, и привязанному к нему двумя номерами

        user.setRoistatAccount(null);
        user.setAmoAccount(null);
        HibernateDao.update(user);




    }

    /**
     * Тест на удаление сайта. Номера должны отвязатся от сайта, но остаться у пользователя
     */
    @Test
    public void testDeleteSite() throws Exception {
        User user = HibernateDao.getUserByLogin("hibernate");
        Site site = user.getSiteByName("hiber");
        user.removeSite(site);
        HibernateDao.update(user);

        user = HibernateDao.getUserByLogin("hibernate");
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
        User user = HibernateDao.getUserByLogin("hibernate");
        HibernateDao.delete(user);
        user = HibernateDao.getUserByLogin("hibernate");
        assertNull("Пользователь не удалился", user);
        List<OuterPhone> testPhones = HibernateDao.getAllTestPhones();
        assertEquals("Внешние номера удалились при удалении пользователя", 3, testPhones.size());
        for (OuterPhone phone : testPhones) {
            assertNull(phone.getSitename());
            assertNull(phone.getBusy());
        }
    }

    @Test
    public void testUsersProperties() throws Exception {
        User user = HibernateDao.getUserByLogin("hibernate");
        user.setPassword("newPass");
        user.setTrackingId("newTrackId");
        user.setEmail("newMail");
        HibernateDao.update(user);

        user = HibernateDao.getUserByLogin("hibernate");
        assertEquals("Пароль пользователя не сохраняется", "newPass", user.getPassword());
        assertEquals("Tracking id пользователя не сохраняется", "newTrackId", user.getTrackingId());
        assertEquals("Email пользователя не сохраняется", "newMail", user.getEmail());
    }

    @Test
    public void amoAccountTest() throws Exception {
        User user = HibernateDao.getUserByLogin("hibernate");
        assertNull(user.getAmoAccount());

        AmoAccount amoAccount = new AmoAccount();
        amoAccount.setAmoLogin("amoLogin");
        amoAccount.setDomain("amoDomain");
        amoAccount.setPhoneEnumId("phoneEnumId");
        amoAccount.setApiKey("apiKey");
        amoAccount.setPhoneId("phoneId");
        user.setAmoAccount(amoAccount);
        HibernateDao.update(user);

        user = HibernateDao.getUserByLogin("hibernate");
        amoAccount = user.getAmoAccount();
        assertNotNull("Амо аккаунт не сохраняется", amoAccount);
        assertEquals("amoLogin", amoAccount.getAmoLogin());
        assertEquals("amoDomain", amoAccount.getDomain());
        assertEquals("phoneEnumId", amoAccount.getPhoneEnumId());
        assertEquals("apiKey", amoAccount.getApiKey());
        assertEquals("phoneId", amoAccount.getPhoneId());

        user.setAmoAccount(null);
        HibernateDao.update(user);

        amoAccount = HibernateDao.getAmoAccountByUser("hibernate");
        assertNull(amoAccount);
    }


    @Test
    public void roistatAccountTest() throws Exception {
        User user = HibernateDao.getUserByLogin("hibernate");
        assertNull(user.getRoistatAccount());

        RoistatAccount roistatAccount = new RoistatAccount();
        roistatAccount.setApiKey("apiKey");
        roistatAccount.setProjectNumber("projectNumber");
        user.setRoistatAccount(roistatAccount);
        HibernateDao.update(user);

        user = HibernateDao.getUserByLogin("hibernate");
        roistatAccount = user.getRoistatAccount();
        assertNotNull("Roistat аккаунт не сохраняется", roistatAccount);
        assertEquals("apiKey", roistatAccount.getApiKey());
        assertEquals("projectNumber", roistatAccount.getProjectNumber());

        user.setRoistatAccount(null);
        HibernateDao.update(user);

        roistatAccount = HibernateDao.getRoistatAccountByUser("hibernate");
        assertNull(roistatAccount);
    }


    @Test
    public void siteTest() throws Exception {
        User user = HibernateDao.getUserByLogin("hibernate");
        assertEquals(1,user.getSites().size());

        Site site = user.getSiteByName("hiber");
        assertNotNull(site);

        site.addIpToBlackList("78.159.55.63");

        HibernateDao.update(user);
        user = HibernateDao.getUserByLogin("hibernate");
        site = user.getSiteByName("hiber");
        assertEquals(1, site.getBlackList().size());




    }


    @AfterClass
    public static void cleaningDb() throws Exception {
        User user = HibernateDao.getUserByLogin("hibernate");
        if (user != null){
            HibernateDao.delete(user);
        }

        AmoAccount amoAccount = HibernateDao.getAmoAccountByUser("hibernate");
        if (amoAccount != null){
            HibernateDao.delete(amoAccount);
        }

        RoistatAccount roistatAccount = HibernateDao.getRoistatAccountByUser("hibernate");
        if (roistatAccount != null) {
            HibernateDao.delete(roistatAccount);
        }

        HibernateDao.removeAllTestPhones();

    }
}