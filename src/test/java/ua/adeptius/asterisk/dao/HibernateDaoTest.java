package ua.adeptius.asterisk.dao;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.model.telephony.*;
import ua.adeptius.asterisk.spring_config.WebConfig;

import java.util.*;

import static org.junit.Assert.*;
import static ua.adeptius.asterisk.TestUtil.*;
import static ua.adeptius.asterisk.model.telephony.DestinationType.SIP;
import static ua.adeptius.asterisk.model.telephony.ForwardType.TO_ALL;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class})
@WebAppConfiguration
public class HibernateDaoTest {

    private static Logger LOGGER = LoggerFactory.getLogger("-=TESTING=-");

    
    private boolean initialized;

    
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception{
        if(!initialized){
            MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
            initialized = true;
            HibernateController.getUserByLogin(TEST_USERNAME);
        }
    }

    private static void createTestUser() throws Exception {
        User user = new User();
        user.setLogin(TEST_USERNAME);
        user.setEmail("hibernate@mail.com");
        user.setPassword(TEST_PASSWORD);
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
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        if (user == null) {
            createTestUser();
            user = HibernateController.getUserByLogin(TEST_USERNAME);
        }
        assertNotNull("User is null!", user);

        if (user.getSiteByName("hiber") == null) {
            addSiteToUser(user);
        }
        user = HibernateController.getUserByLogin(TEST_USERNAME);
        assertTrue(user.getSiteByName("hiber") != null);

        HibernateController.removeAllTestPhones();
        List<OuterPhone> allTestPhones = HibernateController.getAllTestPhones();
        assertEquals(0, allTestPhones.size());

        HibernateController.createTestPhonesForUser(TEST_USERNAME, "hiber");
        user = HibernateController.getUserByLogin(TEST_USERNAME);
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
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        Site site = user.getSiteByName("hiber");
        user.removeSite(site);
        HibernateController.update(user);

        user = HibernateController.getUserByLogin(TEST_USERNAME);
        Set<OuterPhone> outerPhones = user.getOuterPhones();
        assertEquals("Внешние номера не должны удалятся", 3, outerPhones.size());
        for (OuterPhone outerPhone : outerPhones) {
            assertNull("Номера не отвязались от сайта", outerPhone.getSitename());
        }
    }

    @Test
    public void testDeleteInnerPhones() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);

        SipConfig sipConfig = new SipConfig("2222222");
        InnerPhone innerPhone = HibernateController.saveSipBySipConfig(sipConfig, user.getLogin());
        user.addInnerPhones(Collections.singletonList(innerPhone));
        HibernateController.update(user);

        user = HibernateController.getUserByLogin(TEST_USERNAME);
        InnerPhone innerPhoneByNumber = user.getInnerPhoneByNumber("2222222");
        assertNotNull(innerPhoneByNumber);
        user.removeInnerPhones(Collections.singletonList(innerPhoneByNumber));
        HibernateController.update(user);

        user = HibernateController.getUserByLogin(TEST_USERNAME);
        innerPhoneByNumber = user.getInnerPhoneByNumber("2222222");
        assertNull(innerPhoneByNumber);
    }


    /**
     * При удалении пользователя внешние номера должны освобождатся, но не удалятся
     */
    @Test
    public void testRemoveUser() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        HibernateController.delete(user);
        user = HibernateController.getUserByLogin(TEST_USERNAME);
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
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        user.setPassword("newPass");
        user.setTrackingId("newTrackId");
        user.setEmail("newMail");
        HibernateController.update(user);

        user = HibernateController.getUserByLogin(TEST_USERNAME);
        assertEquals("Пароль пользователя не сохраняется", "newPass", user.getPassword());
        assertEquals("Tracking id пользователя не сохраняется", "newTrackId", user.getTrackingId());
        assertEquals("Email пользователя не сохраняется", "newMail", user.getEmail());
    }

    @Test
    public void amoAccountTest() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        assertNull(user.getAmoAccount());

        AmoAccount amoAccount = new AmoAccount();
        amoAccount.setAmoLogin("amoLogin");
        amoAccount.setDomain("amoDomain");
        amoAccount.setPhoneEnumId("phoneEnumId");
        amoAccount.setApiKey("apiKey");
        amoAccount.setPhoneId("phoneId");
        user.setAmoAccount(amoAccount);
        HibernateController.update(user);

        user = HibernateController.getUserByLogin(TEST_USERNAME);
        amoAccount = user.getAmoAccount();
        assertNotNull("Амо аккаунт не сохраняется", amoAccount);
        assertEquals("amoLogin", amoAccount.getAmoLogin());
        assertEquals("amoDomain", amoAccount.getDomain());
        assertEquals("phoneEnumId", amoAccount.getPhoneEnumId());
        assertEquals("apiKey", amoAccount.getApiKey());
        assertEquals("phoneId", amoAccount.getPhoneId());

        user.setAmoAccount(null);
        HibernateController.update(user);

        amoAccount = HibernateController.getAmoAccountByUser(TEST_USERNAME);
        assertNull(amoAccount);
    }


    @Test
    public void roistatAccountTest() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        assertNull(user.getRoistatAccount());

        RoistatAccount roistatAccount = new RoistatAccount();
        roistatAccount.setApiKey("apiKey");
        roistatAccount.setProjectNumber("projectNumber");
        user.setRoistatAccount(roistatAccount);
        HibernateController.update(user);

        user = HibernateController.getUserByLogin(TEST_USERNAME);
        roistatAccount = user.getRoistatAccount();
        assertNotNull("Roistat аккаунт не сохраняется", roistatAccount);
        assertEquals("apiKey", roistatAccount.getApiKey());
        assertEquals("projectNumber", roistatAccount.getProjectNumber());

        user.setRoistatAccount(null);
        HibernateController.update(user);

        roistatAccount = HibernateController.getRoistatAccountByUser(TEST_USERNAME);
        assertNull(roistatAccount);
    }

    @Test
    public void operatorLocationsTest() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        assertNull(user.getOperatorLocation());

        AmoOperatorLocation operatorLocation = new AmoOperatorLocation();
        operatorLocation.setName("hiberLocations");
        HashMap<String, String> map = new HashMap<>();
        map.put("111", "222");
        operatorLocation.setAmoUserIdAndInnerNumber(map);
        user.setAmoOperatorLocations(operatorLocation);
        HibernateController.update(user);


        user = HibernateController.getUserByLogin(TEST_USERNAME);
        operatorLocation = user.getOperatorLocation();
        assertNotNull("OperatorLocation не сохраняется", operatorLocation);
        assertEquals("hiberLocations", operatorLocation.getName());
        assertTrue(operatorLocation.getAmoUserIdAndInnerNumber().size() == 1);

        user.setAmoOperatorLocations(null);
//        user.setRoistatAccount(null);
        HibernateController.update(user);

        user = HibernateController.getUserByLogin(TEST_USERNAME);

        assertNull(user.getOperatorLocation());

        operatorLocation = HibernateController.getAmoOperatorLocationByUser(TEST_USERNAME);

        assertNull(operatorLocation);

    }


    @Test
    public void siteTest() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        assertEquals(1, user.getSites().size());

        Site site = user.getSiteByName("hiber");
        assertNotNull(site);

        site.addIpToBlackList("78.159.55.63");

        HibernateController.update(user);
        user = HibernateController.getUserByLogin(TEST_USERNAME);
        site = user.getSiteByName("hiber");
        assertEquals(1, site.getBlackList().size());
    }

    @Test
    public void userAudioTest() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        assertEquals(0, user.getUserAudio().size());
        UserAudio userAudio = new UserAudio();
        userAudio.setFilename("123.mp3");
        userAudio.setName("123");
        user.addUserAudio(userAudio);

        HibernateController.update(user);
        user = HibernateController.getUserByLogin(TEST_USERNAME);
        assertEquals(1, user.getUserAudio().size());

        UserAudio next = user.getUserAudio().iterator().next();
        user.removeUserAudio(next);

        HibernateController.update(user);
        user = HibernateController.getUserByLogin(TEST_USERNAME);

        assertEquals(0, user.getUserAudio().size());
    }

    @Test
    public void rulesScenariosAndChainsTest() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);

        Scenario scenario = addNewScenario(user, "hiberScenario");

        Rule firstRule = addNewRule(scenario, "hiberRule");
        Rule secondRule = addNewRule(scenario, "hiberRule2");
        ChainElement firstChainElement = addNewChainElement(firstRule, 0);

        HibernateController.update(user);
        user = HibernateController.getUserByLogin(TEST_USERNAME);
        scenario = user.getScenarios().iterator().next();



        Set<Scenario> scenarios = user.getScenarios();
        List<Rule> rules = scenario.getRules();
        assertTrue(rules.contains(firstRule));
        assertTrue(rules.contains(secondRule));

        assertEquals(2, rules.size());


        firstRule = rules.stream().filter(rule -> rule.getName().equals("hiberRule")).findFirst().get();
        firstChainElement = firstRule.getChain().get(0);
        assertNotNull(firstChainElement);

//        HibernateController.update(user);
//        user = HibernateController.getUserByLogin(TEST_USERNAME);

        user.removeScenario(scenario);
        HibernateController.update(user);
        user = HibernateController.getUserByLogin(TEST_USERNAME);

        List<Rule> ruleList = HibernateController.getRuleByUser(TEST_USERNAME);
        assertTrue("При удалении сценария не удаляются правила", ruleList.isEmpty());
//
        List<ChainElement> hibernate = HibernateController.getChainsByUser(TEST_USERNAME);
        assertTrue("при удалении правила не удаляются цепочки", hibernate.isEmpty());
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

    @Test
    public void registerQueryTest() throws Exception {
        RegisterQuery registerQuery = new RegisterQuery(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_PHONE);
        String hash = registerQuery.getHash();
        HibernateController.saveOrUpdate(registerQuery);
        RegisterQuery registerQueryByKey = HibernateController.getRegisterQueryByKey(hash);
        assertNotNull(registerQueryByKey);
        assertEquals(registerQuery.getLogin(), TEST_USERNAME);
        assertEquals(registerQuery.getPassword(), TEST_PASSWORD);
        assertEquals(registerQuery.getEmail(), TEST_EMAIL);
        assertEquals(registerQuery.getUserPhoneNumber(), TEST_PHONE);


        HibernateController.removeRegisterQuery(registerQueryByKey);
        registerQueryByKey = HibernateController.getRegisterQueryByKey(hash);
        assertNull(registerQueryByKey);
    }


    @AfterClass
    public static void cleaningDb() throws Exception {
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        if (user != null) {
            HibernateController.delete(user);
        }

        AmoAccount amoAccount = HibernateController.getAmoAccountByUser(TEST_USERNAME);
        if (amoAccount != null) {
            HibernateController.delete(amoAccount);
        }

        RoistatAccount roistatAccount = HibernateController.getRoistatAccountByUser(TEST_USERNAME);
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