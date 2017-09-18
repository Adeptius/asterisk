package ua.adeptius.asterisk.senders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.json.RoistatPhoneCall;
import ua.adeptius.asterisk.model.RoistatAccount;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.Call;

import java.util.concurrent.*;

public class RoistatCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(RoistatPhoneCall.class.getSimpleName());
    private LinkedBlockingQueue<Call> blockingQueue = new LinkedBlockingQueue<>();
    private static ObjectMapper mapper = new ObjectMapper();
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            1,10,60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(30), new ThreadFactoryBuilder().setNameFormat("RoistatCallSender-Pool-%d").build());
    Settings settings = Main.settings;


    public void send(Call call) {
        if (!settings.isCallToRoistatEnabled()){
            LOGGER.debug("RoistatCallSender отключен в настройках");
            return;
        }
        try {
            blockingQueue.put(call);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }


    public RoistatCallSender() {
        setName("RoistatCallSender-Manager");
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Call call = blockingQueue.take();
                EXECUTOR.submit(() -> sendReport(call));
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }


    private void sendReport(Call call) {
        User user = call.getUser();
        String login = user.getLogin();

        RoistatAccount roistatAccount = user.getRoistatAccount();
        if (roistatAccount == null) {
            LOGGER.trace("{}: roistat аккаунт не подключен. Отмена отправки", login);
            return;
        }

        String roistatApi = roistatAccount.getApiKey();
        String roistatProject = roistatAccount.getProjectNumber();

        if (StringUtils.isAnyBlank(roistatApi, roistatProject)) {
            LOGGER.trace("{}: не указаны или пустые поля ключа и номера проекта. Отмена отправки", login);
            return;
        }

        if (StringUtils.isBlank(call.getGoogleId())){
            LOGGER.trace("{}: google ID в звонке пуст. Отмена отправки", login);
            return;
        }


        RoistatPhoneCall roistatPhoneCall = new RoistatPhoneCall(call);

        try {
            String json = mapper.writeValueAsString(roistatPhoneCall);
            LOGGER.trace("{}: в Roistat отправляются данные: {}", login, json);
            HttpResponse<String> response = Unirest
                    .post("https://cloud.roistat.com/api/v1/project/phone-call?project=" + roistatProject + "&key=" + roistatApi)
                    .header("content-type", "application/json")
                    .body(json)
                    .asString();

            String body = response.getBody();

            String result = new JSONObject(body).getString("status");

            if ("success".equals(result)) {
                LOGGER.info("{}: Звонок отправлен в Roistat", login);
            } else {
                LOGGER.error("{}: Ошибка отправки звонка в Roistat: {}", login, new JSONObject(body).getString("error"));
            }
        } catch (Exception e) {
            LOGGER.error(login+": Ошибка отправки звонка в Roistat", e);
        }
    }
}
