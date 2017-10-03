package ua.adeptius.asterisk.webcontrollers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ua.adeptius.asterisk.TestUtil;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.JsonUser;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.RecoverQuery;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.spring_config.WebConfig;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static ua.adeptius.asterisk.TestUtil.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class})
@WebAppConfiguration
public class UserWebControllerTest {

    @Autowired
    private TestUtil testUtil;

    @Test
    public void setPassword() throws Exception {
        testUtil.deleteTestUserIfExistAndCreateNew();
        String jsonStringFromUrl = testUtil.getJsonStringFromUrl("/user/setPassword", true, false,
                testUtil.createMap("password", "newpassnewpass"), null);
        String message = new Message(jsonStringFromUrl).getMessage();
        assertEquals("Password is too short. Must be minimum 20 characters", message);


        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/user/setPassword", true, false,
                testUtil.createMap("password", "ёёёыыыыыыыыewpassnewpass"), null);
        message = new Message(jsonStringFromUrl).getMessage();
        assertEquals("Password contains non-eng symbols", message);


        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/user/setPassword", true, false,
                testUtil.createMap("password", "newpassnewpassnewpass"), null);
        assertEquals(Message.Status.Success, new Message(jsonStringFromUrl).getStatus());

        User userFromContainer = UserContainer.getUserByName(TEST_USERNAME);
        assertEquals("newpassnewpassnewpass", userFromContainer.getPassword());

        User userFromDb = HibernateController.getUserByLogin(TEST_USERNAME);
        assertEquals("newpassnewpassnewpass", userFromDb.getPassword());
    }

    @Test
    public void setUser() throws Exception {
        testUtil.deleteTestUserIfExistAndCreateNew();
        String jsonStringFromUrl = testUtil.getJsonStringFromUrl("/user/set", true, true,
                null, "{\"email\":\"someMail@gmail.com\",\"trackingId\":\"someId\"}");
        assertEquals("User changed", new Message(jsonStringFromUrl).getMessage());

        User userFromContainer = UserContainer.getUserByName(TEST_USERNAME);
        assertEquals("someMail@gmail.com", userFromContainer.getEmail());

        User userFromDb = HibernateController.getUserByLogin(TEST_USERNAME);
        assertEquals("someMail@gmail.com", userFromDb.getEmail());
    }

    @Test
    public void getUser() throws Exception {
        testUtil.deleteTestUserIfExist();
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
        HibernateController.saveUser(user);
        UserContainer.putUser(user);
        String jsonStringFromUrl = testUtil.getJsonStringFromUrl("/user/get", true, false,
                null, null);
        JsonUser jsonUser = new ObjectMapper().readValue(jsonStringFromUrl, JsonUser.class);
        assertEquals(TEST_USERNAME, jsonUser.getLogin());
        assertEquals(TEST_EMAIL, jsonUser.getEmail());
        assertEquals(HibernateController.getUserByLogin(TEST_USERNAME).getPassword(), TEST_PASSWORD);
    }

    @Test
    public void recoverPassword() throws Exception {
        testUtil.deleteTestUserIfExistAndCreateNew();
//        На всякий случай удяем все предыдущие запросы на смену пароля
        List<RecoverQuery> allRecoverQueries = HibernateController.getAllRecoverQueries();
        for (RecoverQuery query : allRecoverQueries) {
            if (query.getLogin().equals(TEST_USERNAME)){
                HibernateController.removeRecoverQuery(query);
            }
        }

        String jsonStringFromUrl = testUtil.getJsonStringFromUrl("/user/recoverPassword", true, false,
                testUtil.createMap("userName", TEST_USERNAME), null);
        Message message = new Message(jsonStringFromUrl);
        assertEquals("Mail sended", message.getMessage());

        RecoverQuery recoverQuery = HibernateController.getAllRecoverQueries().stream()
                .filter(recoverQuery1 -> recoverQuery1.getLogin().equals(TEST_USERNAME))
                .findFirst().orElse(null);
        if (recoverQuery == null) {
            fail("В БД нет записи о смене пароля");
        }

        String queryKey = recoverQuery.getHash(); // ключ получен

        jsonStringFromUrl = testUtil.getJsonStringFromUrl("/user/recoverConfirm", true, false,
                testUtil.createMap("key", queryKey, "newPass", "12345678901234567890"), null);
        message = new Message(jsonStringFromUrl);
        Message.Status status = message.getStatus();
        assertEquals(Message.Status.Success, status);

        assertEquals(HibernateController.getUserByLogin(TEST_USERNAME).getPassword(), "12345678901234567890");

        User userByHash = UserContainer.getUserByHash(message.getMessage());
        if (userByHash == null) {
            fail("Не найден юзер по токену, вернувшимся после смены пароля");
        }
    }
}