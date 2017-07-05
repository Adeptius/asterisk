package ua.adeptius.amocrm;


import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.istack.internal.Nullable;
import org.apache.commons.text.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.exceptions.AmoAccountNotFoundException;
import ua.adeptius.amocrm.exceptions.AmoUnknownException;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyExeption;
import ua.adeptius.amocrm.model.TimePairCookie;
import ua.adeptius.amocrm.model.json.AmoAccount;
import ua.adeptius.amocrm.model.json.contact.AmoContact;
import ua.adeptius.amocrm.model.json.contact.CustomFieldsItem;
import ua.adeptius.amocrm.model.json.contact.ValuesItem;


import java.util.ArrayList;
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
            LOGGER.trace("{}: Cookie пользователя отсутствуют в кеше", userLogin);
            String cookie = auth(domain, userLogin, userApiKey);
            storeCookie(userLogin, cookie);
            return cookie;
        } else {
            LOGGER.trace("{}: Cookie пользователя имеются в кеше", userLogin);
            return timePairCookie.getCoockie();
        }
    }

    //
    public static String auth(String domain, String userLogin, String userApiKey) throws UnirestException, AmoAccountNotFoundException, AmoWrongLoginOrApiKeyExeption, AmoUnknownException {
        LOGGER.trace("{}: Запрос cookie в API", userLogin);
        HttpResponse<String> uniRest = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/auth.php?type=json")
                .field("USER_LOGIN", userLogin)
                .field("USER_HASH", userApiKey)
                .asString();

        String body = uniRest.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Ответ AMO: {}", userLogin, body);
        JSONObject responseJobject = new JSONObject(uniRest.getBody()).getJSONObject("response");

        boolean authSuccess = responseJobject.getBoolean("auth");
        if (authSuccess) {
            Headers headers = uniRest.getHeaders();
            List<String> list = headers.get("Set-Cookie");
            StringBuilder cookies = new StringBuilder();
            for (String s : list) {
                cookies.append(s).append("; ");
            }
            LOGGER.trace("{}: Cookie для пользователя: {}", userLogin, cookies);
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


    public static AmoAccount getAmoAccount(String amocrmProject, String userLogin, String userApiKey) throws Exception {
        LOGGER.trace("{}: Запрос информации об аккаунте", userLogin);
        String cookie = getCookie(amocrmProject, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + amocrmProject + ".amocrm.ru/private/api/v2/json/accounts/current")
                .header("Cookie", cookie)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получена информация об аккаунте: {}", userLogin, body);
        String response = new JSONObject(body).getJSONObject("response").toString();
        String account = new JSONObject(response).getJSONObject("account").toString();
        return new AmoAccount(account);
    }

    public static String getContacts(String amocrmProject, String userLogin, String userApiKey) throws Exception {
        LOGGER.trace("{}: Запрос контактов", userLogin);
        String cookie = getCookie(amocrmProject, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + amocrmProject + ".amocrm.ru/private/api/v2/json/contacts/list")
                .header("Cookie", cookie)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получены контакты {}:", userLogin, body);
        String response = new JSONObject(body).getJSONObject("response").toString();
        JSONArray contacts = new JSONObject(response).getJSONArray("contacts");
//        for (Object contact : contacts) {
//            System.out.println(contact);
//        }
//TODO Возвращать обьект
        return response;
    }

    @Nullable
    public static AmoContact getContactIdByNumber(String amocrmProject, String userLogin, String userApiKey, String phoneNumber) throws Exception {
        LOGGER.trace("{}: Запрос id контакта {}", userLogin, phoneNumber);
        String cookie = getCookie(amocrmProject, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + amocrmProject + ".amocrm.ru/private/api/v2/json/contacts/list?query=" + phoneNumber)
                .header("Cookie", cookie)
                .asString();
        String body = uniresp.getBody();
        if (uniresp.getStatus() == 204) { // 204 No Content
            LOGGER.trace("{}: Контакт {} не найден", userLogin, phoneNumber);
            return null;
        }
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("Получен id контакта {} для аккаунта {}: ", userLogin, body);
        String firstContactInArray = new JSONObject(body).getJSONObject("response").getJSONArray("contacts").getJSONObject(0).toString();
        AmoContact contact = new ObjectMapper().readValue(firstContactInArray, AmoContact.class);
        return contact;
    }


    public static String updateContact(String domain, String userLogin, String userApiKey, AmoContact contact) throws Exception {
        LOGGER.trace("{}: Запрос обновления контакта {}", userLogin, contact.getName());
        String cookie = getCookie(domain, userLogin, userApiKey);
        String contactJson = new ObjectMapper().writeValueAsString(contact);
        String request = "{\"request\":{\"contacts\":{\"update\":["+contactJson+"]}}}";
        System.out.println("Отправляю: "+request);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/contacts/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ обновления контакта {}: ", userLogin, body);
//        String firstContactInArray = new JSONObject(body).getJSONObject("response").getJSONArray("contacts").getJSONObject(0).toString();
//        AmoContact contact = new ObjectMapper().readValue(firstContactInArray, AmoContact.class);
        return ""; //FIXME возвращать id
    }


    public static int addNewContact(String domain, String userLogin, String userApiKey, AmoContact contact) throws Exception {
        LOGGER.trace("{}: Запрос добавления контакта {}", userLogin, contact.getName());
        String cookie = getCookie(domain, userLogin, userApiKey);
        String contactJson = new ObjectMapper().writeValueAsString(contact);
        String request = "{\"request\":{\"contacts\":{\"add\":["+contactJson+"]}}}";
        System.out.println("Отправляю: "+request);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/contacts/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ добавления контакта {}: ", userLogin, body);
        int responseId = new JSONObject(body).getJSONObject("response").getJSONObject("contacts").getJSONArray("add").getJSONObject(0).getInt("id");

        return responseId;
    }

    public static void addNewContact(String domain, String userLogin, String userApiKey, String contact, int dealId, String phoneId, String phoneEnumId) throws Exception {

        AmoContact newContact = new AmoContact();
            newContact.setName("Новый контакт");

            ValuesItem valuesItem = new ValuesItem();
            valuesItem.setEnumType(phoneEnumId);
            valuesItem.setValue(contact);
            ArrayList<ValuesItem> valuesItems = new ArrayList<>();
            valuesItems.add(valuesItem);

            CustomFieldsItem item = new CustomFieldsItem();
            item.setId(phoneId);
            item.setCode("PHONE");
            item.setName("Телефон");
            item.setValues(valuesItems);

            List<CustomFieldsItem> customFieldsItems = new ArrayList<>();
            customFieldsItems.add(item);
            newContact.setCustomFields(customFieldsItems);

            ArrayList<String> leads = new ArrayList<>();
            leads.add(dealId+"");
            newContact.setLinkedLeadsId(leads);
        addNewContact(domain,userLogin, userApiKey, newContact);
    }




     public static int addNewDeal(String domain, String userLogin, String userApiKey) throws Exception {
        LOGGER.trace("{}: Запрос добавления новой сделки", userLogin);
        String cookie = getCookie(domain, userLogin, userApiKey);
        String request = "{\"request\": {\"leads\": {\"add\": [{\"name\": \"Новая сделка\",\"tags\": \"Входящий звонок\"}]}}}";
        System.out.println("Отправляю: "+request);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/leads/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ добавления новой сделки: {}", userLogin, body);
        int responseId = new JSONObject(body).getJSONObject("response").getJSONObject("leads").getJSONArray("add").getJSONObject(0).getInt("id");

        return responseId;
    }




}

