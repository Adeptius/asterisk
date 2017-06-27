package ua.adeptius.asterisk.test;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) throws Exception{

        Process process = Runtime.getRuntime().exec("ping 8.8.8.8");
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = input.readLine();
        while (line != null) {
            System.out.println(line);
            line = input.readLine();
        }
    }
}
