package ua.adeptius.asterisk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.json.Message;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.spring_config.WebConfig;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@Component
public class TestUtil {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    public static final MediaType TEXT_HTML = new MediaType(MediaType.TEXT_HTML.getType(), MediaType.TEXT_HTML.getSubtype(), Charset.forName("utf8"));

    private static final String CHARACTER = "a";

    public static final String TEST_USERNAME = "hibernate";
    public static final String TEST_PASSWORD = "passwordpasswordpassword";
    public static final String TEST_EMAIL = "hibernate@mail.com";
    public static final String TEST_TOKEN = "58fa7c02123c1b644a6ebbed3fb7d218";
    public static final String TEST_SITENAME = "hibernateSite";
    public static final String TEST_PHONE = "0442376283";

    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    public TestUtil(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        System.out.println();
    }

    //    public static Message convertStringToMessage(String json){
//
//    }

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

    public static String createStringWithLength(int length) {
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < length; index++) {
            builder.append(CHARACTER);
        }

        return builder.toString();
    }

    public HashMap<String, String> createMap(String... params) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            map.put(params[i], params[i + 1]);
        }
        return map;
    }

    public Message getMessageFromString(String url, boolean post, boolean json,
                                         HashMap<String, String> fields, String body) throws Exception{
        String response = getJsonStringFromUrl(url, post, json, fields, body);
        return new Message(response);
    }

    public String getJsonStringFromUrl(String url, boolean post, boolean json,
                                        HashMap<String, String> fields, String body) throws Exception {
        MockHttpServletRequestBuilder builder;
        if (post) {
            builder = post(url);
        } else {
            builder = get(url);
        }

        if (json){
            builder.contentType(TestUtil.APPLICATION_JSON_UTF8);
        }else {
            builder.contentType(TestUtil.TEXT_HTML);
        }

        builder.header("host", "localhost:80")
                .header("Origin", "localhost:80")
                .header("Authorization", TEST_TOKEN);
//        if (headers != null) {
//            for (Map.Entry<String, String> entry : fields.entrySet()) {
//                builder.header(entry.getKey(), entry.getValue());
//            }
//        }

        if (fields != null){
            HttpHeaders params = new HttpHeaders();
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                params.add(entry.getKey(), entry.getValue());
            }
            builder.params(params);
        }

        if (body != null) {
            builder.content(body);
        }

        MvcResult mvcResult = mockMvc.perform(builder).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String contentAsString = response.getContentAsString();
        if (response.getStatus() != 200) {
            System.out.println(contentAsString);
            fail("Статус ответа не 200");
        }

        return contentAsString;
    }

    public void createTestUser() throws Exception{
        User user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);
        HibernateController.saveOrUpdateUser(user);
        UserContainer.putUser(user);
    }

    public void deleteTestUserIfExist() throws Exception{
        User user = HibernateController.getUserByLogin(TEST_USERNAME);
        if (user != null) {
            HibernateController.delete(user);
        }
        User hibernate = UserContainer.getUserByName(TEST_USERNAME);
        if (hibernate != null) {
            UserContainer.removeUser(hibernate);
        }
    }

    public void deleteTestUserIfExistAndCreateNew() throws Exception{
        deleteTestUserIfExist();
        createTestUser();
    }
}
