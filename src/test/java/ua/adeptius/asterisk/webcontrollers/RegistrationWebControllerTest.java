package ua.adeptius.asterisk.webcontrollers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ua.adeptius.asterisk.TestUtil;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.RegisterQuery;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.spring_config.WebConfig;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static ua.adeptius.asterisk.TestUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class})
@WebAppConfiguration
public class RegistrationWebControllerTest {

    @Autowired
    private TestUtil testUtil;

    @Test
    public void register() throws Exception {
        testUtil.deleteTestUserIfExist();

        String jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", "sh", "password", TEST_PASSWORD,
                        "email", TEST_EMAIL, "phone", TEST_PHONE), null);
        Message message = new Message(jsonStringFromUrl);
        assertEquals("Login is short", message.getMessage());

        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", "ёжиг", "password", TEST_PASSWORD,
                        "email", TEST_EMAIL, "phone", TEST_PHONE), null);
        message = new Message(jsonStringFromUrl);
        assertEquals("Login contains wrong symbols", message.getMessage());

        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", "e404", "password", TEST_PASSWORD,
                        "email", TEST_EMAIL, "phone", TEST_PHONE), null);
        message = new Message(jsonStringFromUrl);
        assertEquals("Login is busy", message.getMessage());

        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", TEST_USERNAME, "password", TEST_PASSWORD,
                        "email", "mail@com", "phone", TEST_PHONE), null);
        message = new Message(jsonStringFromUrl);
        assertEquals("Email is wrong", message.getMessage());


        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", TEST_USERNAME, "password", TEST_PASSWORD,
                        "email", "adeptius@gmail.com", "phone", TEST_PHONE), null);
        message = new Message(jsonStringFromUrl);
        assertEquals("Email already registered", message.getMessage());


        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", TEST_USERNAME, "password", "sdfjuenbn338b48bt84",
                        "email", TEST_EMAIL, "phone", TEST_PHONE), null);
        message = new Message(jsonStringFromUrl);
        assertEquals("Password lenth less than 20", message.getMessage());

        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", TEST_USERNAME, "password", "ёsdfjuenn33ыыыыыыыыы8b48bt84",
                        "email", TEST_EMAIL, "phone", TEST_PHONE), null);
        message = new Message(jsonStringFromUrl);
        assertEquals("Password contains non-eng symbols", message.getMessage());

        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", TEST_USERNAME, "password", TEST_PASSWORD,
                        "email", TEST_EMAIL, "phone", "7378221415"), null);
        message = new Message(jsonStringFromUrl);
        assertEquals("Phone number is not Ukrainian", message.getMessage());

//      На всякий случай удалим прежде все запросы на регистрацию
        List<RegisterQuery> allRegisterQueries = HibernateController.getAllRegisterQueries();
        for (RegisterQuery query : allRegisterQueries) {
            if (query.getLogin().equals(TEST_USERNAME)){
                HibernateController.removeRegisterQuery(query);
            }
        }

        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/register", true, false,
                testUtil.createMap("login", TEST_USERNAME, "password", TEST_PASSWORD,
                        "email", TEST_EMAIL, "phone", TEST_PHONE), null);
        message = new Message(jsonStringFromUrl);
        assertEquals(Message.Status.Success, message.getStatus());

        allRegisterQueries = HibernateController.getAllRegisterQueries();
        RegisterQuery registerQuery = allRegisterQueries.stream()
                .filter(registerQuery1 -> registerQuery1.getLogin().equals(TEST_USERNAME))
                .findFirst()
                .orElse(null);
        if (registerQuery == null){
            fail("Созданный запрос на регистрацию не найден в БД");
        }

        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/registration/key", true, false,
                testUtil.createMap("key", registerQuery.getHash()), null);
        message = new Message(jsonStringFromUrl);
        assertEquals(Message.Status.Success, message.getStatus());
        String hashOfUser = UserContainer.getHashOfUser(new User(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL));
        assertEquals(hashOfUser, message.getMessage());
        System.out.println("register successful! Token: " + message.getMessage());
    }
}