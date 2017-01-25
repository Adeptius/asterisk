package ua.adeptius.asterisk.utils;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.utils.MyLogger.log;

@SuppressWarnings("Duplicates")
public class GoogleAnalitycs  extends Thread {

    private static final String GOOGLE_URL = "http://www.google-analytics.com/collect";
    private String googleId;
    private String phone;

    public GoogleAnalitycs(String googleId) {
        this.googleId = googleId;
    }

    public GoogleAnalitycs(String googleId, String phone) {
        this.googleId = googleId;
        this.phone = phone;
    }

    @Override
    public void run() {
        List<String> params = new ArrayList<>();
        params.add("v=1");
        params.add("tid="+Settings.googleAnalyticsTrackingId); // Tracking ID / Property ID.
        params.add("cid="+googleId); // Client ID.
        params.add("t=event"); // Hit Type.
        params.add("ec="+Settings.googleAnalyticsCategoryName); // Category
        params.add("ea="+Settings.googleAnalyticsEventName); // Event
        if (phone != null) {
            params.add("el="+phone); // Label
        }

        try {
            String url = GOOGLE_URL;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            String urlParameters = "";
            for (int i = 0; i < params.size(); i++) {
                if (i != 0) urlParameters += "&";
                urlParameters += params.get(i);
            }
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = in.readLine();
            if (!response.startsWith("GIF")) System.out.println(response);
        }catch (Exception e){
            log("Не удалось отправить данные в Google Analitycs " + e, this.getClass());
        }
    }
}
