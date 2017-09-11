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
import ua.adeptius.asterisk.telephony.ForwardType;
import ua.adeptius.asterisk.telephony.SipConfig;

import java.util.*;

import static org.junit.Assert.*;
import static ua.adeptius.asterisk.telephony.DestinationType.SIP;
import static ua.adeptius.asterisk.telephony.ForwardType.TO_ALL;

public class HibernateDaoTest {

    private static Logger LOGGER = LoggerFactory.getLogger("-=TESTING=-");

//    private static HibernateController HibernateController;

    @BeforeClass
    public static void preparingDb() throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");
//        HibernateController = context.getBean(HibernateController.class);
        User user = HibernateController.getUserByLogin("hibernate");
    }

    private static void createTestUser() throws Exception {
        User user = new User();
        user.setLogin("hibernate");
        user.setEmail("hibernate@mail.com");
        user.setPassword("password");
        user.setTrackingId("hibernateId");
        HibernateController.saveUser(user);
    }

    private static void addSiteToUser(User user) {
        Site site = new Site();
        site.setName("hiber");
        site.setTimeToBlock(120);
        site.setStandardNumber("5551112");
        site.setUser(user);
        user.addSite(site);
        HibernateController.update(user);
    }

    @Before
    public void prepareUser() throws Exception {
        LOGGER.info("Подготавливаем тестового пользователя для нового теста");
        User user = HibernateController.getUserByLogin("hibernate");
        if (user == null) {
            createTestUser();
            user = HibernateController.getUserByLogin("hibernate");
        }
        assertNotNull("User is null!", user);

        if (user.getSiteByName("hiber") == null) {
            addSiteToUser(user);
        }
        user = HibernateController.getUserByLogin("hibernate");
        assertTrue(user.getSiteByName("hiber") != null);

        HibernateController.removeAllTestPhones();
        List<OuterPhone> allTestPhones = HibernateController.getAllTestPhones();
        assertEquals(0, allTestPhones.size());

        HibernateController.createTestPhonesForUser("hibernate", "hiber");
        user = HibernateController.getUserByLogin("hibernate");
        Site hiberSite = user.getSiteByName("hiber");
        assertEquals(3, user.getOuterPhones().size());
        assertEquals(2, hiberSite.getOuterPhones().size());

        // теперь у нас юзер с тремя телефонами, сайтом, и привязанному к нему двумя номерами

        user.setRoistatAccount(null);
        user.setAmoAccount(null);
        HibernateController.update(user);


    }

    /**
     * Тест на удаление сайта. Номера должны отвязатся от сайта, но остаться у пользователя
     */
    @Test
    public void testDeleteSite() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        Site site = user.getSiteByName("hiber");
        user.removeSite(site);
        HibernateController.update(user);

        user = HibernateController.getUserByLogin("hibernate");
        Set<OuterPhone> outerPhones = user.getOuterPhones();
        assertEquals("Внешние номера не должны удалятся", 3, outerPhones.size());
        for (OuterPhone outerPhone : outerPhones) {
            assertNull("Номера не отвязались от сайта", outerPhone.getSitename());
        }
    }

    @Test
    public void testDeleteInnerPhones() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");

        SipConfig sipConfig = new SipConfig("2222222");
        InnerPhone innerPhone = HibernateController.saveSipBySipConfig(sipConfig, user.getLogin());
        user.addInnerPhones(Collections.singletonList(innerPhone));
        HibernateController.update(user);

        user = HibernateController.getUserByLogin("hibernate");
        InnerPhone innerPhoneByNumber = user.getInnerPhoneByNumber("2222222");
        assertNotNull(innerPhoneByNumber);
        user.removeInnerPhones(Collections.singletonList(innerPhoneByNumber));
        HibernateController.update(user);

        user = HibernateController.getUserByLogin("hibernate");
        innerPhoneByNumber = user.getInnerPhoneByNumber("2222222");
        assertNull(innerPhoneByNumber);
    }


    /**
     * При удалении пользователя внешние номера должны освобождатся, но не удалятся
     */
    @Test
    public void testRemoveUser() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        HibernateController.delete(user);
        user = HibernateController.getUserByLogin("hibernate");
        assertNull("Пользователь не удалился", user);
        List<OuterPhone> testPhones = HibernateController.getAllTestPhones();
        assertEquals("Внешние номера удалились при удалении пользователя", 3, testPhones.size());
        for (OuterPhone phone : testPhones) {
            assertNull(phone.getSitename());
            assertNull(phone.getBusy());
        }
    }

    @Test
    public void testUsersProperties() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        user.setPassword("newPass");
        user.setTrackingId("newTrackId");
        user.setEmail("newMail");
        HibernateController.update(user);

        user = HibernateController.getUserByLogin("hibernate");
        assertEquals("Пароль пользователя не сохраняется", "newPass", user.getPassword());
        assertEquals("Tracking id пользователя не сохраняется", "newTrackId", user.getTrackingId());
        assertEquals("Email пользователя не сохраняется", "newMail", user.getEmail());
    }

    @Test
    public void amoAccountTest() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        assertNull(user.getAmoAccount());

        AmoAccount amoAccount = new AmoAccount();
        amoAccount.setAmoLogin("amoLogin");
        amoAccount.setDomain("amoDomain");
        amoAccount.setPhoneEnumId("phoneEnumId");
        amoAccount.setApiKey("apiKey");
        amoAccount.setPhoneId("phoneId");
        user.setAmoAccount(amoAccount);
        HibernateController.update(user);

        user = HibernateController.getUserByLogin("hibernate");
        amoAccount = user.getAmoAccount();
        assertNotNull("Амо аккаунт не сохраняется", amoAccount);
        assertEquals("amoLogin", amoAccount.getAmoLogin());
        assertEquals("amoDomain", amoAccount.getDomain());
        assertEquals("phoneEnumId", amoAccount.getPhoneEnumId());
        assertEquals("apiKey", amoAccount.getApiKey());
        assertEquals("phoneId", amoAccount.getPhoneId());

        user.setAmoAccount(null);
        HibernateController.update(user);

        amoAccount = HibernateController.getAmoAccountByUser("hibernate");
        assertNull(amoAccount);
    }


    @Test
    public void roistatAccountTest() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        assertNull(user.getRoistatAccount());

        RoistatAccount roistatAccount = new RoistatAccount();
        roistatAccount.setApiKey("apiKey");
        roistatAccount.setProjectNumber("projectNumber");
        user.setRoistatAccount(roistatAccount);
        HibernateController.update(user);

        user = HibernateController.getUserByLogin("hibernate");
        roistatAccount = user.getRoistatAccount();
        assertNotNull("Roistat аккаунт не сохраняется", roistatAccount);
        assertEquals("apiKey", roistatAccount.getApiKey());
        assertEquals("projectNumber", roistatAccount.getProjectNumber());

        user.setRoistatAccount(null);
        HibernateController.update(user);

        roistatAccount = HibernateController.getRoistatAccountByUser("hibernate");
        assertNull(roistatAccount);
    }

    @Test
    public void operatorLocationsTest() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        assertNull(user.getOperatorLocation());

        AmoOperatorLocation operatorLocation = new AmoOperatorLocation();
        operatorLocation.setName("hiberLocations");
        HashMap<String, String> map = new HashMap<>();
        map.put("111", "222");
        operatorLocation.setAmoUserIdAndInnerNumber(map);
        user.setAmoOperatorLocations(operatorLocation);
        HibernateController.update(user);


        user = HibernateController.getUserByLogin("hibernate");
        operatorLocation = user.getOperatorLocation();
        assertNotNull("OperatorLocation не сохраняется", operatorLocation);
        assertEquals("hiberLocations", operatorLocation.getName());
        assertTrue(operatorLocation.getAmoUserIdAndInnerNumber().size() == 1);

        user.setAmoOperatorLocations(null);
//        user.setRoistatAccount(null);
        HibernateController.update(user);

        user = HibernateController.getUserByLogin("hibernate");

        assertNull(user.getOperatorLocation());

        operatorLocation = HibernateController.getAmoOperatorLocationByUser("hibernate");

        assertNull(operatorLocation);

    }


    @Test
    public void siteTest() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        assertEquals(1, user.getSites().size());

        Site site = user.getSiteByName("hiber");
        assertNotNull(site);

        site.addIpToBlackList("78.159.55.63");

        HibernateController.update(user);
        user = HibernateController.getUserByLogin("hibernate");
        site = user.getSiteByName("hiber");
        assertEquals(1, site.getBlackList().size());
    }

    @Test
    public void userAudioTest() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        assertEquals(0, user.getUserAudio().size());
        UserAudio userAudio = new UserAudio();
        userAudio.setFilename("123.mp3");
        userAudio.setName("123");
        user.addUserAudio(userAudio);

        HibernateController.update(user);
        user = HibernateController.getUserByLogin("hibernate");
        assertEquals(1, user.getUserAudio().size());

        UserAudio next = user.getUserAudio().iterator().next();
        user.removeUserAudio(next);

        HibernateController.update(user);
        user = HibernateController.getUserByLogin("hibernate");

        assertEquals(0, user.getUserAudio().size());
    }

    @Test
    public void rulesScenariosAndChainsTest() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");

        Scenario scenario = addNewScenario(user, "hiberScenario");

        Rule firstRule = addNewRule(scenario, "hiberRule");
        Rule secondRule = addNewRule(scenario, "hiberRule2");
        ChainElement firstChainElement = addNewChainElement(firstRule, 0);

        HibernateController.update(user);
        user = HibernateController.getUserByLogin("hibernate");

        Set<Scenario> scenarios = user.getScenarios();
        List<Rule> rules = scenario.getRules();
        assertEquals(2, rules.size());

        firstRule = rules.stream().filter(rule -> rule.getName().equals("hiberRule")).findFirst().get();
        firstChainElement = firstRule.getChain().get(0);
        assertNotNull(firstChainElement);

//        HibernateController.update(user);
//        user = HibernateController.getUserByLogin("hibernate");

        user.removeScenario(scenario);
        HibernateController.update(user);
        user = HibernateController.getUserByLogin("hibernate");

        //todo дописать тесты что бы убедится что на этом моменте и правила и цепочки удалены

    }


    private Scenario addNewScenario(User user, String name) {
        Scenario scenario = new Scenario();
        scenario.setName(name);
        user.addScenario(scenario);
        return scenario;
    }

    private Rule addNewRule(Scenario scenario, String name) {
        Rule rule = new Rule();
        rule.setName(name);
        rule.setDays(new boolean[]{true, true, true, true, true, true, true});
        rule.setStartHour(0);
        rule.setEndHour(24);
        rule.setType(RuleType.DEFAULT);
        rule.setMelody("none");
        scenario.addRule(rule);
        return rule;
    }

    private ChainElement addNewChainElement(Rule rule, int position) {
        ChainElement chainElement = new ChainElement();
        chainElement.setAwaitingTime(10);
        chainElement.setDestinationType(SIP);
        chainElement.setForwardType(TO_ALL);
        chainElement.setPosition(position);
        chainElement.setToList(Arrays.asList("111", "222"));
        rule.addChainElement(chainElement);
        return chainElement;
    }

    @AfterClass
    public static void cleaningDb() throws Exception {
        User user = HibernateController.getUserByLogin("hibernate");
        if (user != null) {
            HibernateController.delete(user);
        }

        AmoAccount amoAccount = HibernateController.getAmoAccountByUser("hibernate");
        if (amoAccount != null) {
            HibernateController.delete(amoAccount);
        }

        RoistatAccount roistatAccount = HibernateController.getRoistatAccountByUser("hibernate");
        if (roistatAccount != null) {
            HibernateController.delete(roistatAccount);
        }

        HibernateController.removeAllTestPhones();

//        List<Long> time = HibernateController.time;
//        long summary = 0;
//        for (Long aLong : time) {
//            summary+=aLong;
//        }
//        summary = summary / time.size();

//        System.out.println("Operation count: " + HibernateController.operationsCount + " AVG time: " + summary);
    }
}