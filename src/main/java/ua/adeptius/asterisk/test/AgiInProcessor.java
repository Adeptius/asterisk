package ua.adeptius.asterisk.test;

import org.asteriskjava.Cli;
import org.asteriskjava.fastagi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.monitor.AsteriskMonitor;
import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static ua.adeptius.asterisk.telephony.DestinationType.GSM;
import static ua.adeptius.asterisk.telephony.DestinationType.SIP;
import static ua.adeptius.asterisk.telephony.ForwardType.QUEUE;
import static ua.adeptius.asterisk.telephony.ForwardType.RANDOM;
import static ua.adeptius.asterisk.telephony.ForwardType.TO_ALL;

public class AgiInProcessor extends BaseAgiScript {

    private static Logger LOGGER = LoggerFactory.getLogger(AgiInProcessor.class.getSimpleName());

    private static HashMap<String, Rule> phoneNumbersAndRules = new HashMap<>();
    private static Random random = new Random();


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

            String username = rule.getUser().getLogin();

            HashMap<Integer, ChainElement> chain = rule.getChain();
            if (chain == null || chain.size() == 0) {
                LOGGER.error("{}: цепочка отсутствует или пуста! Номер {}, правило {}", username, toNumber, rule);
                return;
            }


            answer();
            streamFile("/var/lib/asterisk/sounds/en/chillingmusic");
//            streamFile("/home/adeptius/barns_countney2");
//            playMusicOnHold("/var/lib/asterisk/sounds/en/barns_countney2");

//            playMusicOnHold();

            for (int i = 0; i < chain.size(); i++) {
                ChainElement chainElement = chain.get(i);
                if (chainElement == null) {
                    break; // null будет если это конец цепочки
                }
                Integer position = chainElement.getPosition();
                if (position != i) {
                    LOGGER.error("Не сходится позиция элемента цепочки. i={}, position={}", i, position);
                }

                ForwardType forwardType = chainElement.getForwardType();

                if (forwardType == TO_ALL) {
                    processToAllRedirect(chainElement);
                } else if (forwardType == QUEUE) {
                    processQueueRedirect(chainElement);
                } else if (forwardType == RANDOM) {
                    processRandomRedirect(chainElement);
                }
            }


//            processCall(rule, toNumber);
//            exec("DIAL", "SIP/2001036&SIP/2001037,10,m(slow)");
//            answer();
//            streamFile("welcome");
//            System.out.println("Channel status before = " + getStringedStatus(channel));
//            answer();
//            streamFile("/var/lib/asterisk/sounds/en/alarm");
//            exec("Playback", "/var/lib/asterisk/sounds/en/welcome");
//            playMusicOnHold("slow");
        } catch (AgiHangupException ignored) { // если звонящий кинул трубку

        } catch (AgiException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            hangup();
        } catch (AgiException ignored) {
        }
    }

    private void processToAllRedirect(ChainElement element) throws AgiException {
        List<String> toList = element.getToList();
        int awaitingTime = element.getAwaitingTime();
        DestinationType destinationType = element.getDestinationType();
        String melody = element.getMelody();


        if (destinationType == SIP) {
            List<String> callList = new ArrayList<>(); // составляем список свободных операторов
            HashMap<String, Boolean> sipsState = AsteriskMonitor.getSipsFreeOrRinging();

            for (String number : toList) {
                Boolean numberIsFree = sipsState.get(number);
                if (numberIsFree) {
                    callList.add(number); // если оператор свободен - добавляем в список свободных операторов
                }
            }
            // Список свободных операторов составлен. Теперь всем им звоним одновременно
            dialToSipGroup(callList, awaitingTime, melody);

        } else if (destinationType == GSM) {
            // Звоним на все GSMы
            dialToGsmGroup(toList, awaitingTime, melody);
        }
    }

    private void processQueueRedirect(ChainElement element) {
        List<String> toList = element.getToList();
        int awaitingTime = element.getAwaitingTime();
        DestinationType destinationType = element.getDestinationType();
        String melody = element.getMelody();

    }

    private void processRandomRedirect(ChainElement element) throws AgiException {
        List<String> toList = element.getToList();
//        int awaitingTime = element.getAwaitingTime();
        int awaitingTime = 600; //todo
        DestinationType destinationType = element.getDestinationType();
        String melody = element.getMelody();


        if (destinationType == SIP) {
            List<String> callList = new ArrayList<>(); // составляем список свободных операторов
            HashMap<String, Boolean> sipsState = AsteriskMonitor.getSipsFree();

            for (String number : toList) {
                Boolean numberIsFree = sipsState.get(number);
                if (numberIsFree) {
                    callList.add(number); // если оператор свободен - добавляем в список свободных операторов
                }
            }

            int numbersOfFreeOperators = callList.size();
            if (numbersOfFreeOperators != 0) { //если свободные операторы есть
                int randomInt = random.nextInt(numbersOfFreeOperators);
                String choosenOperator = callList.get(randomInt);
                dialToSip(choosenOperator, awaitingTime, melody);
            }
        } else if (destinationType == GSM) {
            int numbersOfFreeOperators = toList.size();
            int randomInt = random.nextInt(numbersOfFreeOperators);
            String choosenOperator = toList.get(randomInt);
            dialToGsm(choosenOperator, awaitingTime, melody);
        }
    }


    public static HashMap<String, Rule> getPhoneNumbersAndRules() {
        return phoneNumbersAndRules;
    }

    public static void setPhoneNumbersAndRules(HashMap<String, Rule> phoneNumbersAndRules) {
        AgiInProcessor.phoneNumbersAndRules = phoneNumbersAndRules;
    }

    private void dialToSip(String sip, int timeout, String melody) throws AgiException {
        String command = "SIP/" + sip + "," + timeout + ",m(" + melody + ")";
        LOGGER.debug("Производится звонок на SIP {} с ожиданием {} секунд. Команда: {}", sip, timeout, command);
        int responseCode = exec("DIAL", command);
        if (responseCode == -1) {
            LOGGER.debug("SIP {}: разговор состоялся. Завершаем вызов: Hangup", sip);
            hangup();
        } else {
            LOGGER.debug("SIP {}: {}", sip, getStringedCode(responseCode));
        }
    }


    private void dialToSipGroup(List<String> sips, int timeout, String melody) throws AgiException {
        LOGGER.debug("Производится звонок на группу SIP {} с ожиданием {} секунд, мелодией {}", sips, timeout, melody);
        if (sips.size() == 0) {
            LOGGER.debug("Список номеров в SIP группе пуст");
            return;
        }

        String result = "";
        for (String number : sips) {
            result += "&SIP/" + number;
        }
        result = result.substring(1);
        result = result + "," + timeout + ",m(" + melody + ")";

        int responseCode = exec("DIAL", result);
        LOGGER.debug("SIPS {}: {}", sips, getStringedCode(responseCode));
        if (responseCode == -1) {
            LOGGER.debug("SIP {}: разговор состоялся. Завершаем вызов: Hangup", sips);
            hangup();
        }
    }


    private void dialToGsm(String number, int timeout, String melody) throws AgiException {
        String command = "SIP/Intertelekom_main/" + number + "," + timeout + ",m(" + melody + ")";
        LOGGER.debug("Производится звонок на GSM {} с ожиданием {} секунд, мелодией: {}. Команда: {}", number, timeout, melody, command);
        int responseCode = exec("DIAL", command);
        if (responseCode == -1) {
            LOGGER.debug("GSM {}: разговор состоялся. Завершаем вызов: Hangup", number);
            hangup();
        } else {
            LOGGER.debug("GSM {}: {}", number, getStringedCode(responseCode));
        }
    }

    private void dialToGsmGroup(List<String> numbers, int timeout, String melody) throws AgiException {
        LOGGER.debug("Производится звонок на группу GSM {} с ожиданием {} секунд, мелодией: {}", numbers, timeout, melody);

        if (numbers.size() == 0) {
            LOGGER.debug("Список номеров в GSM группе пуст");
            return;
        }

        String result = "";
        for (String number : numbers) {
            result += "&SIP/Intertelekom_main/" + number;
        }
        result = result.substring(1);
        result = result + "," + timeout + ",m(" + melody + ")";

        int responseCode = exec("DIAL", result);
        LOGGER.debug("GSM {}: {}", numbers, getStringedCode(responseCode));
        if (responseCode == -1) {
            LOGGER.debug("GSM {}: разговор состоялся. Завершаем вызов: Hangup", numbers);
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