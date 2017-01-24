package com.luxoft.webapplication.utils;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Newpost {

    public static final String GOOGLE_URL = "http://www.google-analytics.com/collect";
    public static final String LOCAL = "http://localhost";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36";
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";


    public static void main(String[] args) throws Exception {
        Newpost newpost = new Newpost();
        for (int i = 1000; i < 1100; i++) {
            newpost.start();

        }
    }

    private void start() throws Exception {
        String[] params = new String[7];
        params[1] = "v=1";
        params[2] = "tid=UA-88866926-1";
        params[3] = "cid=1433766020.1481115478";
        params[4] = "t=pageview";
        params[5] = "dh=test.prometriki.ru";
        params[6] = "dp=/my-test-page";

        String url = GOOGLE_URL;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        String urlParameters = "";
        for (int i = 1; i < params.length; i++) {
            if(i != 1) urlParameters += "&"; // добавлять "&" в начале не нужно
            urlParameters+= params[i];
        }

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        System.out.println("Передаю параметры: " + urlParameters);
        wr.writeBytes(urlParameters);
        wr.flush(); wr.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String response = in.readLine();
        System.out.println(response);
    }
}
