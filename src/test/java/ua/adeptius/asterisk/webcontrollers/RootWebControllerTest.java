package ua.adeptius.asterisk.webcontrollers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.spring_config.WebConfig;
import ua.adeptius.asterisk.TestUtil;

import static junit.framework.TestCase.assertEquals;
import static ua.adeptius.asterisk.TestUtil.TEST_PASSWORD;
import static ua.adeptius.asterisk.TestUtil.TEST_TOKEN;
import static ua.adeptius.asterisk.TestUtil.TEST_USERNAME;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class})
@WebAppConfiguration
public class RootWebControllerTest {

    @Autowired
    private TestUtil testUtil;

    @Test
    public void getTokenTest() throws Exception {
        testUtil.deleteTestUserIfExistAndCreateNew();
        String jsonStringFromUrl = testUtil.getJsonStringFromUrl("/getToken", true, false,
                testUtil.createMap("login", TEST_USERNAME, "password", TEST_PASSWORD), null);
        Message message = new Message(jsonStringFromUrl);
        assertEquals(TEST_TOKEN, message.getMessage());
    }
}
