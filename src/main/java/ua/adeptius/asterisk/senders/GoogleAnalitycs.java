package ua.adeptius.asterisk.senders;


import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.newmodel.User;
import ua.adeptius.asterisk.utils.logging.LogCategory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.MyLogger.log;

@SuppressWarnings("Duplicates")
public class GoogleAnalitycs extends Thread {

    private static final String GOOGLE_URL = "http://www.google-analytics.com/collect";
    private User user;
    private Call call;
    private String clientGoogleId;


    public GoogleAnalitycs(Call call) {
        this.clientGoogleId = call.getGoogleId();
        this.user = call.getUser();
        this.call = call;

        String id = user.getTrackingId();
        if (id !=null && !"".equals(id) && call.getDirection() == Call.Direction.IN){
            start();
        }
    }

    @Override
    public void run() {
        List<String> params = new ArrayList<>();
        params.add("v=1");
        params.add("tid=" + user.getTrackingId()); // Tracking ID / Property ID.
        params.add("t=event"); // Hit Type.

//        if (customer instanceof OldSite){  TODO придумать обработку
//            params.add("ec=calltracking"); // Category
//        }else {
//            params.add("ec=ip_telephony"); // Category
//        }

        if (clientGoogleId.equals("")){
            params.add("cid="+getGoogleId(call.getFrom())); // Client ID.
        }else {
            params.add("cid=" + clientGoogleId); // Client ID.
        }



        params.add("ea=new call"); // Event
        params.add("el=" + call.getDirection()); // Label

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

    private static String getGoogleId(String number){
        int numberLength = number.length();
        int moreLetters = 10-numberLength;
        String googleId = number;
        for (int i = 0; i < moreLetters; i++) {
            googleId += "0";
        }
        if (googleId.length()>10){
            googleId = googleId.substring(0,10);
        }
        googleId = googleId + ".0000000000";
        return googleId;

    }
}
