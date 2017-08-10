package ua.adeptius.asterisk.senders;


import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.OuterPhone;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.Call;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static ua.adeptius.asterisk.model.Call.Direction.IN;

public class GoogleAnalitycsCallSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(GoogleAnalitycsCallSender.class.getSimpleName());
    private LinkedBlockingQueue<Call> blockingQueue = new LinkedBlockingQueue<>();

    public void send(Call call) {
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
                Call call = blockingQueue.take();
                sendReport(call);
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }

    private void sendReport(Call call) {
        String login = call.getUser().getLogin();
        if (call.getDirection() != IN){ // если исходящий звонок - отбой
            LOGGER.debug("{}: Звонок исходящий. Отмена отправки", login);
            return;
        }

        String userGoogleAnalitycsId = call.getUser().getTrackingId();// если не указан трекинг айди - отбой
        if (StringUtils.isBlank(userGoogleAnalitycsId)){
            LOGGER.debug("{}: Не указан google analytics id у юзера. Отмена отправки", login);
            return;
        }

        String clientGoogleId = call.getGoogleId();
        if (clientGoogleId == null) {
            LOGGER.debug("{}: google ID в звонке пуст. Вероятно клиент давно закрыл страницу со скриптом. Отмена отправки", login);
            return;
        }

        OuterPhone outerPhone = call.getOuterPhone();
        if (outerPhone == null) {
            LOGGER.debug("{}: Вероятно в колл процессоре кэш не обновился еще, а у пользователя телефон уже нет.. Отмена отправки", login);
            return;
        }

        String siteName = outerPhone.getSitename();
        if (siteName == null){
            LOGGER.error(login+": SiteName is null");
            return;
        }

        HashMap<String, Object> map = new HashMap<>(); // формируем запрос
        map.put("v", "1");
        map.put("t", "event"); // Hit Type.
        map.put("tid", userGoogleAnalitycsId); // Tracking ID
        map.put("cid", clientGoogleId); // Client ID.
        map.put("ec", "calltracking"); // Категория
        map.put("ea", siteName+": new call");// Событие

        try {
            String response = Unirest.post("http://www.google-analytics.com/collect").fields(map).asString().getBody();
            LOGGER.trace("{}: Звонок отправлен в Google Analitycs. Ответ: {}", login, response);
        } catch (UnirestException e) {
            LOGGER.error(login+": Ошибка отправки звонка в Google Analitycs", e);
        }
    }
}
