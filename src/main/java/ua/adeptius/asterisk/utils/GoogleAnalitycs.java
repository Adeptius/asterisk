package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.model.LogCategory;
import ua.adeptius.asterisk.model.Site;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.utils.MyLogger.log;

@SuppressWarnings("Duplicates")
public class GoogleAnalitycs extends Thread {

    private static final String GOOGLE_URL = "http://www.google-analytics.com/collect";
    private String phone;
    private Site site;
    private String clientGoogleId;

    public GoogleAnalitycs(Site site, String clientGoogleId, String phone) {
        this.clientGoogleId = clientGoogleId;
        this.phone = phone;
        this.site = site;
    }

    @Override
    public void run() {
        List<String> params = new ArrayList<>();
        params.add("v=1");
        params.add("tid=" + site.getGoogleAnalyticsTrackingId()); // Tracking ID / Property ID.
        params.add("cid=" + clientGoogleId); // Client ID.
        params.add("t=event"); // Hit Type.
        params.add("ec=calltracking"); // Category
        params.add("ea=new call"); // Event
        params.add("el=" + phone); // Label

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
        } catch (Exception e) {
            log(LogCategory.ERROR_SENDING_ANALYTICS, "Не удалось отправить данные в Google Analitycs " + e);
        }
    }
}
