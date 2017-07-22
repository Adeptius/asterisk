package ua.adeptius.asterisk.senders;


import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.NewCall;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class GoogleAnalitycsCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(GoogleAnalitycsCallSender.class.getSimpleName());
    private LinkedBlockingQueue<NewCall> blockingQueue = new LinkedBlockingQueue<>();

    public void send(NewCall call) {
        try {
            blockingQueue.put(call);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }


    public GoogleAnalitycsCallSender() {
        setName("AmoCallSender");
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                NewCall call = blockingQueue.take();
                sendReport(call);
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }



    private void sendReport(NewCall call) {
        if (call.getDirection() != NewCall.Direction.IN){
            return;
        }

        User user = call.getUser();
        String userGoogleAnalitycsId = user.getTrackingId();
        if (StringUtils.isBlank(userGoogleAnalitycsId)){
            return;
        }


        String clientGoogleId = call.getGoogleId();

        HashMap<String, Object> map = new HashMap<>();
        map.put("v", "1");
        map.put("tid", userGoogleAnalitycsId); // Tracking ID / Property ID.
        map.put("t", "event"); // Hit Type.
        map.put("ea", "new call");// Event
//          map.put("el", call.getDirection()); // Label

        NewCall.Service service = call.getService();
        if (service == NewCall.Service.TRACKING) {
            map.put("ec", "calltracking"); // Category
        } else if (service == NewCall.Service.TELEPHONY) {
            map.put("ec", "ip_telephony"); // Category
        }

        if (clientGoogleId.equals("")) {
            map.put("cid", getGoogleId(call.getCalledFrom())); // Client ID.
        } else {
            map.put("cid", clientGoogleId); // Client ID.
        }

        try {
            Unirest.post("http://www.google-analytics.com/collect").fields(map).asString();
            LOGGER.trace("Звонок отправлен в Google Analitycs");
        } catch (UnirestException e) {
            LOGGER.error("Ошибка отправки звонка в Google Analitycs", e);
        }
    }

    private static String getGoogleId(String number) {
        int numberLength = number.length();
        int moreLetters = 10 - numberLength;
        String googleId = number;
        for (int i = 0; i < moreLetters; i++) {
            googleId += "0";
        }
        if (googleId.length() > 10) {
            googleId = googleId.substring(0, 10);
        }
        googleId = googleId + ".0000000000";
        return googleId;

    }
}
