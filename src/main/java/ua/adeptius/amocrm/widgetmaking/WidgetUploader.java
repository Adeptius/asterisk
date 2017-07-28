package ua.adeptius.amocrm.widgetmaking;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WidgetUploader {

    private static final String SOURCE = "D:\\Java\\Projects\\asterisk_dmitriy\\src\\main\\java\\ua\\adeptius\\amocrm\\widgetmaking\\";
    private static final String INPUT_FOR_ZIP = SOURCE + "widget\\*";
    private static final String OUTPUT_ZIP_FILE = SOURCE +"widget.zip";
    private static final String PATH_TO_7ZIP = "D:\\JavaProgs\\7-Zip\\7z.exe";
    private static final String ATTRIBUTES = "a -tzip";

    private static String amoAccount = "adeptius@wid.ua";
    private static String amoApiKey = "a99ead2f473e150091360d25aecc2878";

    public static void main(String[] args) throws Exception {
        Process process = Runtime.getRuntime().exec(PATH_TO_7ZIP + " " + ATTRIBUTES + " " + OUTPUT_ZIP_FILE + " " + INPUT_FOR_ZIP);
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            if (line.contains("Everything is Ok")){
                afterCompressCompleted();
            }
        }
    }

    public static void afterCompressCompleted() throws Exception {
        String manifest = new String(Files.readAllBytes(Paths.get(SOURCE + "widget\\manifest.json")), "UTF-8");
        JSONObject jManifest = new JSONObject(manifest);
        JSONObject jWidget = jManifest.getJSONObject("widget");
        String widgetCode = jWidget.getString("code");
        String secretKey = jWidget.getString("secret_key");
        send(widgetCode, secretKey);
    }

    public static void send(String widgetCode, String secretKey) throws Exception{
        HttpResponse<String> stringHttpResponse = Unirest.post("https://widgets.amocrm.ru/adeptiustest2/upload/")
                .field("widget", new File(OUTPUT_ZIP_FILE))
                .field("widget", widgetCode)
                .field("secret", secretKey)
                .field("amouser", amoAccount)
                .field("amohash", amoApiKey)
                .asString();
        System.out.println(stringHttpResponse.getBody());
    }
}
