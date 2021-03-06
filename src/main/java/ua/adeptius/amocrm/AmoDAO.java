package ua.adeptius.amocrm;


import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.exceptions.*;
import ua.adeptius.amocrm.model.TimePairCookie;
import ua.adeptius.amocrm.model.json.JsonAmoAccount;
import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.amocrm.model.json.JsonAmoDeal;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.telephony.Call;
import ua.adeptius.asterisk.model.IdPairTime;
import ua.adeptius.asterisk.senders.ErrorsMailSender;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("Duplicates")
public class AmoDAO {

    private static Logger LOGGER = LoggerFactory.getLogger(AmoDAO.class.getSimpleName());


    public static ConcurrentHashMap<String, TimePairCookie> cookiesRepo = new ConcurrentHashMap<>();

    private static void storeCookie(String login, String cookie) {
        cookiesRepo.put(login, new TimePairCookie(new GregorianCalendar().getTimeInMillis(), cookie));
    }

    private static String getCookie(AmoAccount amoAccount) throws Exception {
        String amoLogin = amoAccount.getAmoLogin();
        TimePairCookie timePairCookie = cookiesRepo.get(amoLogin);
        String login = amoAccount.getUser().getLogin();

        if (timePairCookie == null) {
            LOGGER.trace("{}: Cookie amo пользователя {} отсутствуют в кеше", login, amoLogin);
            String cookie = auth(amoAccount);
            storeCookie(amoLogin, cookie);
            return cookie;
        } else {
            LOGGER.trace("{}: Cookie amo пользователя {} имеются в кеше", login, amoLogin);
            return timePairCookie.getCoockie();
        }
    }


    public static String auth(AmoAccount amoAccount) throws Exception {
        String domain = amoAccount.getDomain();
        String amoLogin = amoAccount.getAmoLogin();
        String userApiKey = amoAccount.getApiKey();
        String login = amoAccount.getUser().getLogin();
        LOGGER.trace("{}: Запрос amo cookie для {}", login, amoLogin);

        HttpResponse<String> uniRest = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/auth.php?type=json")
                .field("USER_LOGIN", amoLogin)
                .field("USER_HASH", userApiKey)
                .asString();

        String body = uniRest.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Ответ AMO для {}: {}", login, amoLogin, body);
        JSONObject responseJobject = getJResponseIfAllGood(uniRest.getBody());
        Headers headers = uniRest.getHeaders();
        List<String> list = headers.get("Set-Cookie");
        StringBuilder cookies = new StringBuilder();
        for (String s : list) {
            cookies.append(s).append("; ");
        }
        LOGGER.trace("{}: Cookie amo для пользователя {}: {}", login, amoLogin, cookies);
        return cookies.toString();
    }


    public static void checkAllAccess(AmoAccount amoAccount) throws Exception {
        String domain = amoAccount.getDomain();
        String amoLogin = amoAccount.getAmoLogin();
        String login = amoAccount.getUser().getLogin();
        String cookie = auth(amoAccount);
        LOGGER.debug("{}: Тест на возможность добавления новой сделки в amo для {}", login, amoLogin);

        String request = "{\"request\": {\"leads\": {\"add\": [{\"name\": \"Удалить\",\"tags\": \"Nextel\" }]}}}";
        HttpResponse<String> uniresp = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/v2/json/leads/set")
                .header("Cookie", cookie)
                .body(request)
                .asString();

        String body = uniresp.getBody();
        body = StringEscapeUtils.unescapeJava(body);
        LOGGER.trace("{}: Получен ответ тестового добавления новой сделки amo для {}: {}", login, amoLogin, body);
        JSONObject jResponse = getJResponseIfAllGood(body);
        int dealId = jResponse.getJSONObject("leads").getJSONArray("add").getJSONObject(0).getInt("id");
//        try {
//            AmoDAO.removeDeal(amoAccount, dealId);
//        } catch (Exception ignored) {
//             если нет прав на удаление сделки - ну и ладно: сами потом удалят.
//        }
    }


    public static JsonAmoAccount getAmoAccount(AmoAccount amoAccount) throws Exception {
        String amoLogin = amoAccount.getAmoLogin();
        String login = amoAccount.getUser().getLogin();
        LOGGER.debug("{}: Запрос информации об аккаунте {}", login, amoLogin);
        JSONObject jResponse = getJResponse(true, amoAccount, "api/v2/json/accounts/current", null, null);
        String account = jResponse.getJSONObject("account").toString();
        return new JsonAmoAccount(account);
    }

    @Nullable
    public static JsonAmoContact getContactIdByPhoneNumber(AmoAccount amoAccount, String phoneNumber) throws Exception {
        String amoLogin = amoAccount.getAmoLogin();
        String login = amoAccount.getUser().getLogin();
        LOGGER.debug("{}: Запрос id контакта {} для {}", login, phoneNumber, amoLogin);
        String url = "api/v2/json/contacts/list?query=" + phoneNumber;
        JSONObject jResponse = getJResponse(true, amoAccount, url, null, null);
        if (jResponse.toString().equals("{\"no content\":\"true\"}")) {
            return null;
        }
        String firstContactInArray = jResponse.getJSONArray("contacts").getJSONObject(0).toString();
        return new JsonAmoContact(firstContactInArray);
    }


    public static void updateContact(AmoAccount amoAccount, JsonAmoContact jsonAmoContact) throws Exception {
        String userLogin = amoAccount.getAmoLogin();
        LOGGER.debug("{}: Запрос обновления контакта {}", userLogin, jsonAmoContact.getName());
        String request = "{\"request\":{\"contacts\":{\"update\":[" + jsonAmoContact + "]}}}";
        getJResponse(false, amoAccount, "api/v2/json/contacts/set", request, null);
    }

    public static void setResponsibleUserForContact(AmoAccount amoAccount, Call call, String amoUserId) throws Exception {
        String userLogin = amoAccount.getAmoLogin();
        LOGGER.debug("{}: Запрос установки ответственного {} за контакт {}", userLogin, amoUserId, call.getAmoContactId());
        String request = "{\"request\":{\"contacts\":{\"update\":[{"
                + "\"id\":" + call.getAmoContactId()
                + ",\"last_modified\":" + call.getCalculatedModifiedTime()
                + ",\"responsible_user_id\":" + amoUserId
                + "}]}}}";
        getJResponse(false, amoAccount, "api/v2/json/contacts/set", request, null);
    }

    public static int addNewContactNewMethod(AmoAccount amoAccount, String contactNumber, int dealId,
                                             String tags, String contactName, String responsibleUserId) throws Exception {
        String amoLogin = amoAccount.getAmoLogin();
        String login = amoAccount.getUser().getLogin();
        String phoneEnumId = amoAccount.getPhoneEnumId();
        String phoneId = amoAccount.getPhoneId();
        LOGGER.debug("{}: Запрос добавления контакта {} в аккаунт {}", login, contactNumber, amoLogin);

        if (StringUtils.isAnyBlank(phoneEnumId, phoneId)) {
            LOGGER.error("{}: Отсутствует phoneEnumId и phoneId. AmoAccount {}", login, amoAccount);
            throw new AmoException("Отсутствует phoneEnumId и phoneId. Не могу создать контакт.");
        }

        String values = "[{" +
                "\"value\":\"" + contactNumber + "\"," +
                "\"enum\":\"" + phoneEnumId + "\"" +
                "}]";

        String customFields = "[{" +
                "\"code\":\"PHONE\"," +
                "\"values\":" + values + "," +
                "\"name\":\"Телефон\"," +
                "\"id\":\"" + phoneId + "\"}]";

        String contact = "{" +
                "\"tags\":\"" + tags + "\"," +
                "\"name\":\"" + contactName + "\"," +
                "\"responsible_user_id\":\"" + responsibleUserId + "\"," +
                "\"custom_fields\": " + customFields + "," +
                "\"linked_leads_id\":[\"" + dealId + "\"]" +
                "}";

        String request = "{\"request\":{\"contacts\":{\"add\":[" + contact + "]}}}";

        JSONObject jResponse = getJResponse(false, amoAccount, "api/v2/json/contacts/set", request, null);
        int id = jResponse.getJSONObject("contacts").getJSONArray("add").getJSONObject(0).getInt("id");
        return id;
    }

    /**
     * Регистрация в AmoCRM стандартной сделки нового звонка. Возвращает айдишник сделки, который сразу же
     * можно привязать к контакту. Чуствителен ко времени по этому для дальнейших запросов мы берём его
     * и отталкиваемся уже от него, с каждым запросом добавляя одну секунду.
     */
    public static IdPairTime addNewDealAndGetBackIdAndTime(AmoAccount amoAccount, String tags, String responsibleWorker) throws Exception {
        String amoLogin = amoAccount.getAmoLogin();
        String login = amoAccount.getUser().getLogin();
        int pipelineId = amoAccount.getPipelineId();
        int stageId = amoAccount.getStageId();
        LOGGER.debug("{}: Запрос добавления новой сделки в аккаунт {}", login, amoLogin);

        String lead = "{"
                + "\"name\": \"Новая сделка\","
                + "\"tags\": \"" + tags + "\","
                + "\"responsible_user_id\": \"" + responsibleWorker + "\""
                + (stageId > 0 ? ",\"status_id\":" + stageId : "") // добавляем этап сделки, если указан конкретный
                + (pipelineId > 0 ? ",\"pipeline_id\":" + pipelineId : "") // добавляем этап сделки, если указан конкретный
                + "}";

        String request = "{\"request\": {\"leads\": {\"add\": [" + lead + "]}}}";

        JSONObject jResponse = getJResponse(false, amoAccount, "api/v2/json/leads/set", request, null);
        int serverTime = jResponse.getInt("server_time");
        int responseId = jResponse.getJSONObject("leads").getJSONArray("add").getJSONObject(0).getInt("id");
        return new IdPairTime(responseId, serverTime);
    }


    public static void addCallToNotes(AmoAccount amoAccount, Call call) throws Exception {
        String amoLogin = amoAccount.getAmoLogin();
        String login = amoAccount.getUser().getLogin();
        int amoContactId = call.getAmoContactId();

        LOGGER.debug("{}: Запрос добавления входящего звонка в аккаунт {}, id контакта {}", login, amoLogin, amoContactId);

        String calledDate = call.getCalledDate();
        String asteriskId = call.getAsteriskId();
        String calledFrom = call.getCalledFrom();
        int secondsTalk = call.getSecondsTalk();
        String yearMonthDay = calledDate.substring(0, calledDate.indexOf(" "));

        String textIfAnswered = "";

        if (call.getCallState() == Call.CallState.ANSWER) {
            textIfAnswered = "\\\"LINK\\\":\\\"https://cstat.nextel.com.ua:8443/tracking/history/record/" + asteriskId + "/" + yearMonthDay + "\\\"," +
                    "\\\"DURATION\\\": \\\"" + secondsTalk + "\\\",";
        }

        String request = "{\"request\": {\"notes\": {\"add\": [{" +
                "\"element_id\": " + amoContactId + "," +
                "\"element_type\": 1," + //1 - контакт 2-сделка 3-компания
                "\"note_type\": \"10\"," + //10 или 11, входящий или исходящий
                "\"text\": \"{\\\"UNIQ\\\": \\\"" + asteriskId + "\\\"," +
                textIfAnswered +
                "\\\"PHONE\\\":\\\"" + calledFrom + "\\\"," +
                "\\\"call_status\\\": \\\"4\\\"," +
                "\\\"SRC\\\": \\\"nextel_widget\\\"}\"}]}}}";


        JSONObject jResponse = getJResponse(false, amoAccount, "api/v2/json/notes/set", request, null);

//        int serverTime = jResponse.getInt("server_time");
//        int responseId = jResponse.getJSONObject("leads").getJSONArray("add").getJSONObject(0).getInt("id");
//        return new IdPairTime(responseId, serverTime);
    }

//    public static void setTagsToDeal(AmoAccount amoAccount, @Nonnull String tags, int dealid, int dealTime) throws Exception {
//        String amoLogin = amoAccount.getAmoLogin();
//        String login = amoAccount.getUser().getLogin();
//        LOGGER.trace("{}: запрос изменения тэгов сделки {} для {}: {}", login, dealid, amoLogin, tags);
//        String request = "{\"request\": {\"leads\": {\"update\": [{\"id\":" + dealid + ",\"tags\": \"" + tags + "\",\"last_modified\":" + dealTime + "}]}}}";
//        getJResponse(false, amoAccount, "api/v2/json/leads/set", request, null);
//    }

//    public static void removeDeal(AmoAccount amoAccount, int dealId) throws Exception {
//        String amoLogin = amoAccount.getAmoLogin();
//        String login = amoAccount.getUser().getLogin();
//        LOGGER.trace("{}: Запрос удаления сделки {} в аккаунте {}", login, dealId, amoLogin);
//        getJResponse(false, amoAccount, "deals/delete.php", null, "ID", "" + dealId, "ACTION", "DELETE", "pipeline", "Y");
//    }

    @Nullable
    public static JsonAmoDeal getContactsLatestActiveDial(AmoAccount amoAccount, @Nonnull JsonAmoContact contact) throws Exception {
        List<JsonAmoDeal> allDeals = getDealById(amoAccount, contact.getLinked_leads_id());

        if (allDeals == null) {
            return null;
        }

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

    /**
     * Возвращает список сделок по id. String id - перечисление айдишников через запятую
     */
    @Nullable
    public static List<JsonAmoDeal> getDealById(AmoAccount amoAccount, List<String> id) throws Exception {
        String amoLogin = amoAccount.getAmoLogin();
        String login = amoAccount.getUser().getLogin();
        LOGGER.debug("{}: Запрос сделки в amo {} по id: {}", login, amoLogin, id);
        if (id.size() == 0) {
            LOGGER.debug("{}: у контакта сделок нет", login);
            return null;
        }

        String url = "api/v2/json/leads/list?id[]=" + id.get(0);
        for (int i = 1; i < id.size(); i++) {
            url += "&id[]=" + id.get(i);
        }

        JSONObject jResponse = getJResponse(true, amoAccount, url, null, null);
        JSONArray array = jResponse.getJSONArray("leads");
        int serverTimeWhenResponse = jResponse.getInt("server_time");
        if (array.length() == 0) {
            return null;
        }
        List<JsonAmoDeal> deals = new ArrayList<>();
        for (Object o : array) {
            deals.add(new JsonAmoDeal(o.toString(), serverTimeWhenResponse));
        }
        return deals;
    }

//    public static void addNewComent(AmoAccount amoAccount, int leadId, String coment, int time) throws Exception {
//        String amoLogin = amoAccount.getAmoLogin();
//        String login = amoAccount.getUser().getLogin();
//        LOGGER.trace("{}: Запрос добавления нового коментария в аккаунт {} сделка {}: {}", login, amoLogin, leadId, coment);
//
//        String stringedTime = "";
//        if (time > 0) { // Если какое-то время указано
//            stringedTime = ",\"last_modified\":" + time;
//        }
//        String request = "{\"request\":{\"notes\":{\"add\":[{\"element_id\":" + leadId
//                + ",\"element_type\":\"2\",\"note_type\":4 ,\"text\":\"" + coment + "\"" + stringedTime + "}]}}}";
//        getJResponse(false, amoAccount, "api/v2/json/notes/set", request, null);
//    }


    public static String doChangeMailForWidOnly(AmoAccount amoAccount, String dogovor, String email) throws Exception {
        String url = "api/v2/json/leads/list?query=" + dogovor;

        LOGGER.info("WID-MAIL: запрос на добавление email {} в договор {}", email, dogovor);

        JSONObject jResponse = getJResponse(true, amoAccount, url, null);

        String foundedLeadID = null;

        if (jResponse.toString().equals("{\"no content\":\"true\"}")) {
            LOGGER.info("WID-MAIL: ни одной сделки с договором {} не найдено", dogovor);
            return "К сожалению, договор " + dogovor + " не найден.";
        }

        JSONArray leads = jResponse.getJSONArray("leads");
        for (int i = 0; i < leads.length(); i++) {
            JSONObject jLead = leads.getJSONObject(i);
            JSONArray jCustomFields = jLead.getJSONArray("custom_fields");
            for (int j = 0; j < jCustomFields.length(); j++) {
                JSONObject jField = jCustomFields.getJSONObject(j);
                if (jField.get("name").equals("Номер договора")) {
                    String value = jField.getJSONArray("values").getJSONObject(0).getString("value");
                    if (value.equals(dogovor)) {
                        foundedLeadID = jLead.getString("id");
                        LOGGER.info("WID-MAIL: найдена сделка {}, id {}", jLead.getString("name"), foundedLeadID);
                    }
                }
            }
        }

        if (foundedLeadID == null) {
            LOGGER.info("WID-MAIL: ни в одной сделке не указан договор {}", dogovor);
            return "К сожалению, договор " + dogovor + " не найден.";
        }


        String connectedContactID = null;

        url = "api/v2/json/leads/list?id=" + foundedLeadID;
        jResponse = getJResponse(true, amoAccount, url, null);

        JSONArray leads1 = jResponse.getJSONArray("leads");
        JSONObject jsonObject = leads1.getJSONObject(0);
        Object main_contact_id = jsonObject.get("main_contact_id");

        if (main_contact_id instanceof Boolean) {
            // в ответе false если не найден контакт по сделке
        } else {
            String id = "" + (int) main_contact_id;
            connectedContactID = id;
            LOGGER.info("WID-MAIL: в сделке найден ID прикреплённого контакта {}", connectedContactID);
        }

        if (connectedContactID == null) {
            LOGGER.info("WID-MAIL: в сделке не найден закреплённый контакт");
            return "К сожалению, договор " + dogovor + " не найден.";
        }
        // connectedContacts содержит главные контакты по сделкам из списка leadsIdThatHasDogovor

        String values = "[{" +
                "\"value\":\"" + email + "\"," +
                "\"enum\":\"" + 1104359 + "\"" +
                "}]";

        String customFields = "[{" +
                "\"code\":\"EMAIL\"," +
                "\"values\":" + values + "," +
                "\"name\":\"Email\"," +
                "\"id\":\"" + 495305 + "\"}]";

        String contact = "{" +
                "\"id\":\"" + connectedContactID + "\"," +
                "\"last_modified\":\"" + (System.currentTimeMillis() / 1000) + "\"," +
                "\"custom_fields\": " + customFields +
                "}";

        String request = "{\"request\":{\"contacts\":{\"update\":[" + contact + "]}}}";

        jResponse = getJResponse(false, amoAccount, "api/v2/json/contacts/set", request, null);

        String responseAsString = jResponse.toString();
        if (responseAsString.contains("error")){
            LOGGER.error("WID-MAIL: ошибка при изменении контакта {} по договору {}. Ответ от сервера {}",
                    connectedContactID, foundedLeadID, responseAsString);
            ErrorsMailSender.send("WID-MAIL: ошибка при изменении контакта " + connectedContactID +
                    " по договору " + foundedLeadID + ". Ответ от сервера " + responseAsString);
        }

        LOGGER.info("WID-MAIL: email успешно установлен");
        return "Спасибо!";
    }


    private static JSONObject getJResponse(boolean isGET, @Nonnull AmoAccount amoAccount, @Nonnull String relativeUrl,
                                           @Nullable String body, String... fields) throws Exception {

        String domain = amoAccount.getDomain();
        String url = "https://" + domain + ".amocrm.ru/private/" + relativeUrl;
        String cookie = getCookie(amoAccount);

        String login = amoAccount.getUser().getLogin();
        LOGGER.trace("{}: Отправка URL={} метод={} amoLogin={} данные={}", login, url, (isGET ? "GET" : "POST"), amoAccount.getAmoLogin(), body);

        if (isGET && body != null) {
            throw new Exception("GET не может иметь Body");
        }

        HttpResponse<String> stringHttpResponse;
        if (isGET) {
            GetRequest getRequest = Unirest.get(url);
            getRequest.header("Cookie", cookie);
            stringHttpResponse = getRequest.asString();

        } else {
            HttpRequestWithBody post = Unirest.post(url);
            post.header("Cookie", cookie);
            post.body(body);
            if (fields != null) {
                for (int i = 0; i < fields.length; i += 2) {
                    post.field(fields[i], fields[i + 1]);
                }
            }
            stringHttpResponse = post.asString();
        }

        int status = stringHttpResponse.getStatus();


        String responseBody = stringHttpResponse.getBody();
//        responseBody = responseBody.replace("\\u0022","\\\\\"");
        responseBody = StringEscapeUtils.unescapeJava(responseBody);

        LOGGER.trace("{}: Код {} получен ответ: {}", login, status, responseBody);

        if (status == 204) { // 204 No Content
            return new JSONObject("{\"no content\":\"true\"}");
        }

        return getJResponseIfAllGood(responseBody);
    }

    private static JSONObject getJResponseIfAllGood(String response) throws Exception {
        JSONObject jResponse = new JSONObject(response).getJSONObject("response");

        try {
            int errorCode = jResponse.getInt("error_code");

            if (errorCode == 101) {
                throw new AmoAccountNotFoundException();
            } else if (errorCode == 110) {
                throw new AmoWrongLoginOrApiKeyException();
            } else if (errorCode == 244) {
                throw new AmoCantCreateDealException();
            } else if (errorCode == 402) {
                throw new AmoAccountNotPaidException();
//            } else if (errorCode == 232) {
//                throw new AmoTooManyRequestsException();
            }

            LOGGER.error("Добавить обработку кода ошибки авторизации " + errorCode);
            throw new AmoUnknownException("Fix that. Response code from AMO: " + errorCode);

        } catch (JSONException e) {
            // Если не нашли "error_code" в json - значит всё ок.
        }
        return jResponse;
    }
}

