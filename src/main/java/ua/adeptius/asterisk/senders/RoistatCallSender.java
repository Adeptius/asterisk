package ua.adeptius.asterisk.senders;


import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.json.RoistatPhoneCall;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.NewCall;

import java.util.concurrent.LinkedBlockingQueue;

public class RoistatCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(RoistatPhoneCall.class.getSimpleName());
    private LinkedBlockingQueue<NewCall> blockingQueue = new LinkedBlockingQueue<>();

    public void send(NewCall call) {
        try {
            blockingQueue.put(call);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }


    public RoistatCallSender() {
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                NewCall call = blockingQueue.take();
                User user = call.getUser();
                String roistatApi = user.getRoistatApiKey();
                String roistatProject = user.getRoistatProjectNumber();
                if (roistatApi != null && roistatProject != null && !(roistatApi.equals("")) && !(roistatProject.equals(""))) {
                    sendReport(new RoistatPhoneCall(call));
                }
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }


    private void sendReport(RoistatPhoneCall roistatPhoneCall) {
        String json = new Gson().toJson(roistatPhoneCall);
        String apiKey = roistatPhoneCall.getRoistatApiKey();
        String project = roistatPhoneCall.getRoistatProjectNumber();
        try {
            HttpResponse<String> response = Unirest
                    .post("https://cloud.roistat.com/api/v1/project/phone-call?project=" + project + "&key=" + apiKey)
                    .header("content-type", "application/json")
                    .body(json)
                    .asString();

            String body = response.getBody();

            String result = new JSONObject(body).getString("status");

            if ("success".equals(result)) {
                LOGGER.trace("Звонок отправлен в Roistat");
            } else {
                LOGGER.error("Ошибка отправки звонка в Roistat: " + new JSONObject(body).getString("error"));
            }
        } catch (UnirestException e) {
            LOGGER.error("Ошибка отправки звонка в Roistat", e);
        }
    }
}
