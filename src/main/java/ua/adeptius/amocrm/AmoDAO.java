package ua.adeptius.amocrm;


import com.google.gson.Gson;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.exceptions.AmoAccountNotFoundException;
import ua.adeptius.amocrm.model.TimePairCookie;
import ua.adeptius.amocrm.model.json.AmoAccount;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

//    public static String auth(String amocrmProject, String userLogin, String userApiKey) throws Exception {
//        LOGGER.trace("Запрос cookie для пользователя {}", userLogin);
//        URL obj = new URL("https://"+amocrmProject+".amocrm.ru/private/api/auth.php?type=json");
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        con.setRequestMethod("POST");
//        String urlParameters = "USER_LOGIN=" + userLogin + "&USER_HASH=" + userApiKey;
//        con.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//        wr.writeBytes(urlParameters);
//        wr.flush();
//        wr.close();
//        List<String> list = con.getHeaderFields().get("Set-Cookie");
//        String cookies = "";
//        for (String s : list) {
//            cookies = cookies + s + "; ";
//        }
//        System.out.println("COOKIE IS: " + cookies);
//        return cookies;
//    }
//    static AmoAccount getAllUserInfo(String amocrmProject, String userLogin, String userApiKey) throws Exception{
//        String cookie = getCookie(amocrmProject,userLogin,userApiKey);
//        URL obj = new URL("https://"+amocrmProject+".amocrm.ru/private/api/v2/json/accounts/current");
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        con.setRequestMethod("GET");
//        con.setRequestProperty("Cookie", cookie);
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String result = in.readLine();
//        String response = new JSONObject(result).getJSONObject("response").toString();
//        TODO проверить response на всевозможные ошибки
//        String account = new JSONObject(response).getJSONObject("account").toString();
//        return new Gson().fromJson(account, AmoAccount.class);
//    }

    public static AmoAccount getAllUserInfo(String amocrmProject, String userLogin, String userApiKey) throws Exception {
        String cookie = getCookie(amocrmProject, userLogin, userApiKey);
        HttpResponse<String> uniresp = Unirest
                .get("https://" + amocrmProject + ".amocrm.ru/private/api/v2/json/accounts/current")
                .header("Cookie", cookie)
                .asString();
        String response = new JSONObject(uniresp.getBody()).getJSONObject("response").toString();
        String account = new JSONObject(response).getJSONObject("account").toString();
        return new Gson().fromJson(account, AmoAccount.class);
    }

    //
    public static String auth(String domain, String userLogin, String userApiKey) throws Exception {
        LOGGER.trace("Запрос cookie для пользователя {}", userLogin);
        HttpResponse<String> uniRest = Unirest
                .post("https://" + domain + ".amocrm.ru/private/api/auth.php?type=json")
                .field("USER_LOGIN", userLogin)
                .field("USER_HASH", userApiKey)
                .asString();

        String body = uniRest.getBody();
        System.out.println("BODY RESPONSE IS: " + body); // TODO кидать ексепшены разного характера
        JSONObject responseJobject = new JSONObject(uniRest.getBody()).getJSONObject("response");

        boolean authSuccess = responseJobject.getBoolean("auth");
        if (!authSuccess){
            int errcode = responseJobject.getInt("error_code");
            if (errcode == 101){
                String lookingDomain = responseJobject.getString("domain");
                throw new AmoAccountNotFoundException("Domain " + lookingDomain + " not found");
            }
        }
        // Unauthorized
        // Wrong login or password

        Headers headers = uniRest.getHeaders();
        List<String> list = headers.get("Set-Cookie");
        String cookies = "";
        for (String s : list) {
            cookies += s;
        }
        System.out.println("COOKIE IS: " + cookies);
        return cookies;
    }
}

