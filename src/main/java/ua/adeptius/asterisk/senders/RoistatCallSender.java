package ua.adeptius.asterisk.senders;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.json.RoistatPhoneCall;
import ua.adeptius.asterisk.model.RoistatAccount;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.Call;

import java.util.concurrent.LinkedBlockingQueue;

public class RoistatCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(RoistatPhoneCall.class.getSimpleName());
    private LinkedBlockingQueue<Call> blockingQueue = new LinkedBlockingQueue<>();
    private static ObjectMapper mapper = new ObjectMapper();

    public void send(Call call) {
        try {
            blockingQueue.put(call);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }


    public RoistatCallSender() {
        setName("RoistatCallSender");
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Call call = blockingQueue.take();
                User user = call.getUser();
                RoistatAccount roistatAccount = user.getRoistatAccount();
                if (roistatAccount == null) {
                    return;
                }

                String roistatApi = roistatAccount.getApiKey();
                String roistatProject = roistatAccount.getProjectNumber();

                if (StringUtils.isAnyBlank(roistatApi, roistatProject)) {
                    return;
                }

                sendReport(new RoistatPhoneCall(call));

            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }


    private void sendReport(RoistatPhoneCall roistatPhoneCall) {
        try {
//            String json = new Gson().toJson(roistatPhoneCall);
            String json = mapper.writeValueAsString(roistatPhoneCall);
            String apiKey = roistatPhoneCall.getRoistatApiKey();
            String project = roistatPhoneCall.getRoistatProjectNumber();
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
        } catch (Exception e) {
            LOGGER.error("Ошибка отправки звонка в Roistat", e);
        }
    }
}
