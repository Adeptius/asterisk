package ua.adeptius.amocrm;


import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.exceptions.AmoAccountNotFoundException;
import ua.adeptius.amocrm.exceptions.AmoCantCreateDealException;
import ua.adeptius.amocrm.exceptions.AmoUnknownException;
import ua.adeptius.amocrm.exceptions.AmoWrongLoginOrApiKeyExeption;
import ua.adeptius.amocrm.model.TimePairCookie;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.amocrm.model.json.JsonAmoDeal;


import java.util.*;

public class AmoDAO {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoDAO.class.getSimpleName());


    public static HashMap<String, TimePairCookie> cookiesRepo = new HashMap<>();

    private static void storeCookie(String login, String cookie) {
        cookiesRepo.put(login, new TimePairCookie(new GregorianCalendar().getTimeInMillis(), cookie));
    }

    private static String getCookie(String domain, String userLogin, String userApiKey) throws Exception {
        TimePairCookie timePairCookie = cookiesRepo.get(userLogin);
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


    public static String auth(String domain, String userLogin, String userApiKey) throws Exception {
        LOGGER.trace("{}: Запрос cookie в API", userLogin);
        HttpResponse<String> uniRest = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/auth.php?type=json")
                .field("USER_LOGIN", userLogin)
                .field("USER_HASH", userApiKey)
                .asString();

        String body = uniRest.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Ответ AMO: {}", userLogin, body);
        JSONObject responseJobject = getJResponseIfAllGood(uniRest.getBody());
        Headers headers = uniRest.getHeaders();
        List<String> list = headers.get("Set-Cookie");
        StringBuilder cookies = new StringBuilder();
        for (String s : list) {
            cookies.append(s).append("; ");
        }
        LOGGER.trace("{}: Cookie для пользователя: {}", userLogin, cookies);
        return cookies.toString();
    }


    public static void checkAllAccess(String domain, String userLogin, String userApiKey) throws Exception {
//        Проверяем права на создание сделок
        LOGGER.debug("{}: Тест на возможность добавления новой сделки", userLogin);
        String cookie = getCookie(domain, userLogin, userApiKey);
        String request = "{\"request\": {\"leads\": {\"add\": [{}]}}}";
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/leads/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ тестового добавления новой сделки: {}", userLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
        int dealId = jResponse.getJSONObject("leads").getJSONArray("add").getJSONObject(0).getInt("id");
        try {
            AmoDAO.removeDeal(domain, userLogin, userApiKey, dealId);
        } catch (Exception e) {
//             если нет прав на удаление сделки - ну и ладно: сами потом удалят.
        }
    }


    public static JsonAmoAccount getAmoAccount(String amocrmProject, String userLogin, String userApiKey) throws Exception {
        LOGGER.debug("{}: Запрос информации об аккаунте", userLogin);
        String cookie = getCookie(amocrmProject, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + amocrmProject + ".amocrm.ru/private/api/v2/json/accounts/current")
                .header("Cookie", cookie)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получена информация об аккаунте: {}", userLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
        String account = jResponse.getJSONObject("account").toString();
        return new JsonAmoAccount(account);
    }

    @Nullable
    public static JsonAmoContact getContactIdByPhoneNumber(String amocrmProject, String userLogin, String userApiKey, String phoneNumber) throws Exception {
        LOGGER.debug("{}: Запрос id контакта {}", userLogin, phoneNumber);
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
        JSONObject jResponse = getJResponseIfAllGood(body);
        String firstContactInArray = jResponse.getJSONArray("contacts").getJSONObject(0).toString();
        return new JsonAmoContact(firstContactInArray);
    }


    public static void updateContact(String domain, String userLogin, String userApiKey, JsonAmoContact jsonAmoContact) throws Exception {
        LOGGER.debug("{}: Запрос обновления контакта {}", userLogin, jsonAmoContact.getName());
        String cookie = getCookie(domain, userLogin, userApiKey);
//        String contactJson = new ObjectMapper().writeValueAsString(contact);
        String request = "{\"request\":{\"contacts\":{\"update\":[" + jsonAmoContact + "]}}}";
        System.out.println("Отправляю: " + request);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/contacts/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ обновления контакта {}: ", userLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
    }

    public static void addNewContactNewMethod(String domain, String userLogin, String userApiKey, String contactNumber,
                                              int dealId, String phoneId, String phoneEnumId, String tags, String contactName) throws Exception {
        LOGGER.debug("{}: Запрос добавления контакта {}", userLogin, contactNumber);
        String contact = "{\"tags\":\"" + tags + "\",\"name\":\"" + contactName + "\",\"custom_fields\": [{\"code\":\"PHONE\",\"values\":[{\"value\":\"" + contactNumber + "\",\"enum\":\"" + phoneEnumId + "\"}],\"name\":\"Телефон\",\"id\":\"" + phoneId + "\"}],\"linked_leads_id\":[\"" + dealId + "\"]}";
        addNewContact(domain, userLogin, userApiKey, contact);
    }

    public static int addNewContact(String domain, String userLogin, String userApiKey, String contact) throws Exception {
        String cookie = getCookie(domain, userLogin, userApiKey);
        String request = "{\"request\":{\"contacts\":{\"add\":[" + contact + "]}}}";
        LOGGER.trace("{}: Отправляю: {}", userLogin, request);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/contacts/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ добавления контакта {}: ", userLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
        int responseId = jResponse.getJSONObject("contacts").getJSONArray("add").getJSONObject(0).getInt("id");
        return responseId;
    }


    /**
     * Регистрация в AmoCRM стандартной сделки нового звонка. Возвращает айдишник сделки, который сразу же
     * можно привязать к контакту.
     */
    public static int addNewDeal(String domain, String userLogin, String userApiKey, String tags, int leadId) throws Exception {
        LOGGER.debug("{}: Запрос добавления новой сделки", userLogin);
        String cookie = getCookie(domain, userLogin, userApiKey);
        String request = "{\"request\": {\"leads\": {\"add\": [{\"name\": \"Новая сделка\",\"tags\": \"" + tags + "\""
                + (leadId > 0 ? ",\"status_id\":" + leadId + "" : "") // добавляем этап сделки, если указан конкретный
                + "}]}}}";
        System.out.println("Отправляю: " + request);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/leads/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ добавления новой сделки: {}", userLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
        int responseId = jResponse.getJSONObject("leads").getJSONArray("add").getJSONObject(0).getInt("id");
        return responseId;

    }

    public static void setTagsToDeal(String domain, String userLogin, String userApiKey, @NotNull String tags, int dealid) throws Exception {
        LOGGER.debug("{}: Запрос изменения тэгов для сделки {}: {}", userLogin, dealid, tags);
        String cookie = getCookie(domain, userLogin, userApiKey);
        int currentTime = (int) (new Date().getTime() / 1000);
        String request = "{\"request\": {\"leads\": {\"update\": [{\"id\":"+dealid+",\"tags\": \"" + tags + "\",\"last_modified\":"+currentTime+"}]}}}";
        System.out.println("Отправляю: " + request);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/leads/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ добавления тагов в сделку: {}", userLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
    }

    public static void removeDeal(String domain, String userLogin, String userApiKey, int dealId) throws Exception {
        LOGGER.debug("{}: Запрос удаления сделки {}", userLogin, dealId);
        String cookie = getCookie(domain, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/deals/delete.php")
                .header("Cookie", cookie)
                .field("ID", dealId)
                .field("ACTION", "DELETE")
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        JSONObject jResponse = getJResponseIfAllGood(body);
        LOGGER.trace("{}: Получен ответ удаления сделки: {}", userLogin, body);
    }

    @Nullable
    public static JsonAmoDeal getContactsLatestActiveDial(String domain, String userLogin, String userApiKey, @NotNull JsonAmoContact contact) throws Exception {
        List<JsonAmoDeal> allDeals = getAllContactDeals(domain, userLogin, userApiKey, contact);
        List<JsonAmoDeal> activeDials = new ArrayList<>();
        for (JsonAmoDeal deal : allDeals) {
            if (deal.isOpen()) {
                activeDials.add(deal);
            }
        }

        if (activeDials.size() == 0) { // активных сделок нет вообще
            return null;
        }

        if (activeDials.size() == 1) { // активная сделка только одна - возвращаем её
            return activeDials.get(0);
        }

        // если мы дошли до сюда, значит активных сделок несколько - выберем последнюю по времени.
        int latestDealTime = activeDials.get(0).getDateCreate();
        JsonAmoDeal latestDeal = activeDials.get(0);
        for (int i = 1; i < activeDials.size(); i++) {
            JsonAmoDeal currentInCycle = activeDials.get(i);
            if (currentInCycle.getDateCreate() > latestDealTime) {
                latestDealTime = currentInCycle.getDateCreate();
                latestDeal = currentInCycle;
            }
        }
        return latestDeal;
    }

    public static List<JsonAmoDeal> getAllContactDeals(String domain, String userLogin, String userApiKey, JsonAmoContact contact) throws Exception {
//        TODO уточнить в саппорте АМО как запросить это всё одним запросом
        ArrayList<String> dealsIds = contact.getLinked_leads_id();
        List<JsonAmoDeal> allDeals = new ArrayList<>();
        for (String dealId : dealsIds) {
            JsonAmoDeal deal = getDealById(domain, userLogin, userApiKey, dealId);
            allDeals.add(deal);
        }
        return allDeals;
    }

    /**
     * Возвращает список сделок по id. String id - перечисление айдишников через запятую
     */
    @Nullable
    public static JsonAmoDeal getDealById(String domain, String userLogin, String userApiKey, String id) throws Exception {
        LOGGER.debug("{}: Запрос сделки по id: {}", userLogin, id);
        String cookie = getCookie(domain, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + domain + ".amocrm.ru/private/api/v2/json/leads/list?id=" + id)
                .header("Cookie", cookie)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ поиска сделки по id: {}", userLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
        JSONArray array = jResponse.getJSONArray("leads");
        if (array.length() == 0) {
            return null;
        }
        return new JsonAmoDeal(array.getJSONObject(0).toString());
    }

    public static void addNewComent(String domain, String userLogin, String userApiKey, int leadId, String coment) throws Exception {
        LOGGER.debug("{}: Запрос добавления нового коментария: {}", userLogin, coment);
        String cookie = getCookie(domain, userLogin, userApiKey);

//        https://developers.amocrm.ru/rest_api/notes_list.php
//        element_type	Тип привязанного элемента: 1 - контакт, 2 - сделка, 3 - компания, 4 - задача (результат задачи)
//        element_id	Уникальный идентификатор привязанной сделки/контакта
//        note_type	    Тип примечания https://developers.amocrm.ru/rest_api/notes_type.php#notetypes
//        text	        Текстовое обозначение задачи (выводит разную информацию, в зависимости от типов) https://developers.amocrm.ru/rest_api/notes_type.php#notetext

        String request = "{\"request\":{\"notes\":{\"add\":[{\"element_id\":" + leadId + ",\"element_type\":\"2\",\"note_type\":4 ,\"text\":\"" + coment + "\"}]}}}";
        LOGGER.trace("{}: Отправляю: {}", userLogin, request);
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/notes/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();
        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ добавления нового коментария: {}", userLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
    }

    private static JSONObject getJResponseIfAllGood(String response) throws Exception {
        JSONObject jResponse = new JSONObject(response).getJSONObject("response");

        try {
            int errorCode = jResponse.getInt("error_code");

            if (errorCode == 101) {
                throw new AmoAccountNotFoundException();
            } else if (errorCode == 110) {
                throw new AmoWrongLoginOrApiKeyExeption();
            } else if (errorCode == 244) {
                throw new AmoCantCreateDealException();
            }

            LOGGER.error("Добавить обработку кода ошибки авторизации " + errorCode);
            throw new AmoUnknownException("Fix that. Response code from AMO: " + errorCode);

        } catch (JSONException e) {
            // Если не нашли "error_code" в json - значит всё ок.
        }
        return jResponse;
    }
}

