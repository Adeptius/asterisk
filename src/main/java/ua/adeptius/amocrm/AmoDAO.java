package ua.adeptius.amocrm;


import com.google.gson.Gson;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.istack.internal.Nullable;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.exceptions.AmoAccountNotFoundException;
import ua.adeptius.amocrm.exceptions.AmoUnknownException;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyExeption;
import ua.adeptius.amocrm.model.TimePairCookie;
import ua.adeptius.amocrm.model.json.AmoAccount;


import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class AmoDAO {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoDAO.class.getSimpleName());


    public static HashMap<String, TimePairCookie> cookiesRepo = new HashMap<>();

    private static void storeCookie(String login, String cookie) {
        cookiesRepo.put(login, new TimePairCookie(new GregorianCalendar().getTimeInMillis(), cookie));
    }

    private static String getCookie(String domain, String userLogin, String userApiKey) throws Exception {
        TimePairCookie timePairCookie = cookiesRepo.get(userLogin); // TODO по идее можно использовать логин nextel
        if (timePairCookie == null) {
            LOGGER.trace("Cookie пользователя {} отсутствуют или просрочены", userLogin);
            String cookie = auth(domain, userLogin, userApiKey);
            storeCookie(userLogin, cookie);
            return cookie;
        } else {
            LOGGER.trace("Cookie пользователя {} имеются в кеше", userLogin);
            return timePairCookie.getCoockie();
        }
    }

    //
    public static String auth(String domain, String userLogin, String userApiKey) throws UnirestException, AmoAccountNotFoundException, AmoWrongLoginOrApiKeyExeption, AmoUnknownException {
        LOGGER.trace("Запрос cookie для пользователя {}", userLogin);
        HttpResponse<String> uniRest = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/auth.php?type=json")
                .field("USER_LOGIN", userLogin)
                .field("USER_HASH", userApiKey)
                .asString();

        String body = uniRest.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("Ответ AMO для пользователя {}: {}", userLogin, body);
        JSONObject responseJobject = new JSONObject(uniRest.getBody()).getJSONObject("response");

        boolean authSuccess = responseJobject.getBoolean("auth");
        if (authSuccess) {
            Headers headers = uniRest.getHeaders();
            List<String> list = headers.get("Set-Cookie");
            StringBuilder cookies = new StringBuilder();
            for (String s : list) {
                cookies.append(s).append("; ");
            }
            LOGGER.trace("Cookie для пользователя {}: {}", userLogin, cookies);
            return cookies.toString();
        } else {
            int errcode = responseJobject.getInt("error_code");
            if (errcode == 101) {
                String lookingDomain = responseJobject.getString("domain");
                throw new AmoAccountNotFoundException("Domain " + lookingDomain + " not found");
            } else if (errcode == 110) {
                throw new AmoWrongLoginOrApiKeyExeption();
            }
            LOGGER.error("Добавить обработку кода ошибки авторизации " + errcode + ". " +
                    "\nПопытка авторизации пользователя" +
                    "\ndomain: " + domain +
                    "\nuser: " + userLogin +
                    "\napi: " + userApiKey +
                    "\nresponse: " + responseJobject.toString());
            throw new AmoUnknownException("Fix that");
        }
    }


    public static AmoAccount getAllUserInfo(String amocrmProject, String userLogin, String userApiKey) throws Exception {
        LOGGER.trace("Запрос информации об аккаунте {}", userLogin);
        String cookie = getCookie(amocrmProject, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + amocrmProject + ".amocrm.ru/private/api/v2/json/accounts/current")
                .header("Cookie", cookie)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("Получена информация об аккаунте {}: {}", userLogin, body);
        String response = new JSONObject(body).getJSONObject("response").toString();
        String account = new JSONObject(response).getJSONObject("account").toString();
        return new Gson().fromJson(account, AmoAccount.class);
    }

    public static String getContacts(String amocrmProject, String userLogin, String userApiKey) throws Exception {
        LOGGER.trace("Запрос контактов аккаунта {}", userLogin);
        String cookie = getCookie(amocrmProject, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + amocrmProject + ".amocrm.ru/private/api/v2/json/contacts/list")
                .header("Cookie", cookie)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("Получены контакты аккаунта {}: {}", userLogin, body);
        String response = new JSONObject(body).getJSONObject("response").toString();
        JSONArray contacts = new JSONObject(response).getJSONArray("contacts");
//        for (Object contact : contacts) {
//            System.out.println(contact);
//        }
//TODO Возвращать обьект
        return response;
    }

    @Nullable
    public static int getContactIdByNumber(String amocrmProject, String userLogin, String userApiKey, String phoneNumber) throws Exception {
        LOGGER.trace("Запрос id контакта {} из аккаунта {}", phoneNumber, userLogin);
        String cookie = getCookie(amocrmProject, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + amocrmProject + ".amocrm.ru/private/api/v2/json/contacts/list?query=" + phoneNumber)
                .header("Cookie", cookie)
                .asString();
        String body = uniresp.getBody();
        if (uniresp.getStatus() == 204){ // 204 No Content
            LOGGER.trace("Контакт {} не найден в аккаунте {}: ", phoneNumber, userLogin);
            return -1;
        }
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("Получен id контакта {} для аккаунта {}: ", userLogin, body);

        return new JSONObject(body).getJSONObject("response").getJSONArray("contacts").getJSONObject(0).getInt("id");
    }


}

