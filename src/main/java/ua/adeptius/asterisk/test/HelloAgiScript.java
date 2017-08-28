package ua.adeptius.asterisk.test;

import org.asteriskjava.Cli;
import org.asteriskjava.fastagi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.model.ChainElement;
import ua.adeptius.asterisk.model.Rule;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelloAgiScript extends BaseAgiScript {

    private static Logger LOGGER = LoggerFactory.getLogger(HelloAgiScript.class.getSimpleName());

    private static HashMap<String, Rule> phoneNumbersAndRules = new HashMap<>();


    public void service(AgiRequest request, AgiChannel channel) {
        LOGGER.trace("Caller: {}, request {}", request.getCallerIdNumber(), request);
        try {
            String toNumber = addZero(request.getExtension());
            String fromNumber = addZero(request.getCallerIdNumber());
            LOGGER.debug("Поступил звонок с {} на номер {}", fromNumber, toNumber);
            Rule rule = phoneNumbersAndRules.get(toNumber);
            if (rule == null) {
                LOGGER.debug("Правило по номеру {} не найдено", toNumber);
                return;
            }
            processCall(request, rule, toNumber, fromNumber);

        } catch (AgiHangupException ignored) { // если звонящий кинул трубку

        } catch (AgiException e) {
            e.printStackTrace();
        }
        try {
            hangup();
        } catch (AgiException ignored) {
        }
    }

    private void processCall(AgiRequest request, Rule rule, String toNumber, String fromNumber) throws AgiException {
        String username = rule.getUser().getLogin();

        HashMap<Integer, ChainElement> chain = rule.getChain();
        if (chain == null || chain.size() == 0){
            LOGGER.error("{}: цепочка отсутствует или пуста! Номер {}, правило {}", username, toNumber, rule);
            return;
        }

        for (int i = 0; i < chain.size(); i++) {
            ChainElement chainElement = chain.get(i);
            if (chainElement == null){
                break; // null будет если это конец цепочки
            }
            Integer position = chainElement.getPosition();
            if (position != i){
                LOGGER.error("Не сходится позиция элемента цепочки. i={}, position={}", i, position);
            }
            List<String> toList = chainElement.getToList();
            int awaitingTime = chainElement.getAwaitingTime();
            DestinationType destinationType = chainElement.getDestinationType();
            ForwardType forwardType = chainElement.getForwardType();
            String melody = chainElement.getMelody();



//            dialToSip("2001036", 10);

//            dialToGsm("0995306914",30, "slow");
//            dialToSip("2001036", 10);

//            answer();
//            streamFile("welcome");
//            System.out.println("Channel status before = " + getStringedStatus(channel));
//            answer();
//            streamFile("/var/lib/asterisk/sounds/en/alarm");
//            exec("Playback", "/var/lib/asterisk/sounds/en/welcome");
//            playMusicOnHold("slow");




        }
    }






    public static HashMap<String, Rule> getPhoneNumbersAndRules() {
        return phoneNumbersAndRules;
    }

    public static void setPhoneNumbersAndRules(HashMap<String, Rule> phoneNumbersAndRules) {
        HelloAgiScript.phoneNumbersAndRules = phoneNumbersAndRules;
    }

    private void dialToSip(String sip, int timeout) throws AgiException {
        LOGGER.debug("Производится звонок на SIP {} с ожиданием {} секунд", sip, timeout);
        int responseCode = exec("DIAL", "SIP/" + sip + "," + timeout);
        LOGGER.debug("SIP {}: {}", sip, getStringedCode(responseCode));
        if (responseCode == -1) {
            LOGGER.debug("SIP {}: разговор состоялся. Завершаем вызов: Hangup", sip);
            hangup();
        }
    }

    private void dialToGsm(String number, int timeout, String melody) throws AgiException {
        LOGGER.debug("Производится звонок на GSM {} с ожиданием {} секунд, мелодией: {}", number, timeout, melody);
        int responseCode = exec("DIAL", "SIP/Intertelekom_main/"+number+","+timeout+",m("+melody+")");
        LOGGER.debug("GSM {}: {}", number, getStringedCode(responseCode));
        if (responseCode == -1) {
            LOGGER.debug("GSM {}: разговор состоялся. Завершаем вызов: Hangup", number);
            hangup();
        }
    }


    private String getStringedStatus(AgiChannel channel) throws AgiException {
        int channelStatus = channel.getChannelStatus();
        List<String> statuses = new ArrayList<>();
        statuses.add("Channel is down and available");
        statuses.add("Channel is down, but reserved");
        statuses.add("Channel is off hook");
        statuses.add("Digits (or equivalent) have been dialed");
        statuses.add("Line is ringing");
        statuses.add("Remote end is ringing");
        statuses.add("Line is up");
        statuses.add("Line is busy");
        return statuses.get(channelStatus);
    }

    private String getStringedCode(int code) {
        if (code == -1) {
            return "Разговор состоялся";
        }
        if (code == 0) {
            return "Звонок был отменён либо пропущен";
        }
        return "";
    }

    public static String addZero(String source) {
        try {
            if (source.length() == 9 && !source.startsWith("0")) {
                source = "0" + source;
            }
        } catch (Exception e) {
//            System.out.println("Ошибка добавления нолика. Пришло " + source);
        }
        return source;
    }

    public static void main(String[] args) throws Exception {
        Cli cli = new Cli();
        cli.parseOptions(new String[]{});
    }
}