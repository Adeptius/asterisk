package ua.adeptius.asterisk.monitor;

import org.asteriskjava.Cli;
import org.asteriskjava.fastagi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.amocrm.AmoDAO;
import ua.adeptius.amocrm.model.json.JsonAmoContact;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.model.telephony.*;

import java.util.*;

import static ua.adeptius.asterisk.model.telephony.DestinationType.GSM;
import static ua.adeptius.asterisk.model.telephony.DestinationType.SIP;
import static ua.adeptius.asterisk.model.telephony.ForwardType.QUEUE;
import static ua.adeptius.asterisk.model.telephony.ForwardType.RANDOM;
import static ua.adeptius.asterisk.model.telephony.ForwardType.TO_ALL;
import static ua.adeptius.asterisk.utils.MyStringUtils.addZero;

public class AgiInProcessor extends BaseAgiScript {

    private static Logger LOGGER = LoggerFactory.getLogger(AgiInProcessor.class.getSimpleName());

    private static HashMap<String, Rule> phoneNumbersAndRules = new HashMap<>();
    private static Random random = new Random();
    private static ThreadLocal<String> threadLocalMelody = new ThreadLocal<>();
    private static ThreadLocal<String> threadLocalLogin = new ThreadLocal<>();

    public static void replacePhoneAndRule(String phone, Rule rule){
        LOGGER.debug("В AGI передан номер {} и правило {}", phone, rule);
        phoneNumbersAndRules.put(phone, rule);
    }


    public void service(AgiRequest request, AgiChannel channel) {
        LOGGER.trace("Caller: {}, request {}", request.getCallerIdNumber(), request);
        try {

//            dialToSip("2001036", 10, "slow");
//            dialToSip("2001037", 10, "slow");
//            dialToGsm("0995306914", 10, "slow");
//            dialToGsm("0936518480", 10, "slow");
//            dialToSipGroup(Arrays.asList("2001036", "2001037"), 10, "slow");
//            dialToGsmGroup(Arrays.asList("0995306914", "0936518480"), 10, "slow");
            String toNumber = addZero(request.getExtension());
            String fromNumber = addZero(request.getCallerIdNumber());
//
            Rule rule = phoneNumbersAndRules.get(toNumber);
            if (rule == null) {
                LOGGER.trace("Правило по номеру {} не найдено. Сбрасываю звонок.", toNumber);
                hangup(); // правило не найдено
                return;
            }

            User user = rule.getUser();
            threadLocalMelody.set(rule.getMelody());
            threadLocalLogin.set(user.getLogin());
            LOGGER.debug("{}: Поступил звонок с {} на номер {}", threadLocalLogin.get(), fromNumber, toNumber);

            HashMap<Integer, ChainElement> chain = rule.getChain();
            if (chain == null || chain.size() == 0) {
                LOGGER.error("{}: цепочка отсутствует или пуста! Номер {}, правило {}", threadLocalLogin.get(), toNumber, rule);
                return;
            }

//            answer();
//            LOGGER.debug("{}: Снятие трубки {}", threadLocalLogin.get(), fromNumber);
//
//            LOGGER.debug("{}: удерживаю звонок и играю {}", threadLocalLogin.get(), threadLocalMelody.get());
//            playMusicOnHold(threadLocalMelody.get());
//
//
//            Thread.sleep(10000);
//
//
//            LOGGER.debug("{}: отпускаю звонок", threadLocalLogin.get());
//            stopMusicOnHold();


            // Приветствие
            UserAudio greetingMelody = rule.getGreetingMelody();
            if (greetingMelody != null) {
                answer();
                playMelody(greetingMelody);
            }

            checkAmoResponsibleUser(user, fromNumber);

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

            // Прощание
            UserAudio messageMelody = rule.getMessageMelody();
            if (messageMelody != null) { //если пользователь установил какое-то сообщение
                playMelody(messageMelody);
            }

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

    private void checkAmoResponsibleUser(User user, String fromNumber) throws AgiException {
        AmoAccount amoAccount = user.getAmoAccount();
        if (amoAccount == null) {
            LOGGER.trace("{}: амо аккаунт отсутствует. Ответственного пользователя не ищем", user.getLogin());
            return;
        }

        if (!amoAccount.isCling()) {
            LOGGER.trace("{}: липучка отключена.", user.getLogin());
            return;
        }

        try {
            LOGGER.trace("{}: ищем контакт по номеру {}", user.getLogin(), fromNumber);
            JsonAmoContact contactByNumber = AmoDAO.getContactIdByPhoneNumber(amoAccount, fromNumber);
            if (contactByNumber == null) {
                LOGGER.trace("{}: контакт по телефону {} не найден в AMO", user.getLogin(), fromNumber);
                return;
            }

//        JsonAmoDeal contactsLatestActiveDial = AmoDAO.getContactsLatestActiveDial(amoAccount, contactByNumber);
//        if (contactsLatestActiveDial == null) {
//            LOGGER.debug("{}: активных сделок не найдено по контакту {}", user.getLogin(), contactByNumber);
//            return;
//        }

            String responsibleUserId = contactByNumber.getResponsible_user_id();
            LOGGER.trace("{}: найдена сделка по телефону {}. Ответственный пользователь: {}", user.getLogin(), fromNumber, responsibleUserId);

            HashMap<String, String> amoUserIdAndInnerNumber = user.getOperatorLocation().getAmoUserIdAndInnerNumber();
            String responsibleOperatorNumber = amoUserIdAndInnerNumber.get(responsibleUserId);
            if (responsibleOperatorNumber == null) {
                LOGGER.trace("{}: номер телефона ответственного сотрудника не найден", user.getLogin());
                return;
            }
//            else if (user.getInnerPhoneByNumber(responsibleOperatorNumber) == null){
//                LOGGER.warn("{}: номер телефона ответственного сотрудника найден, но телефон уже, видимо, не его", user.getLogin());
//                return;
//            }не подходит если звонить на gsm
            LOGGER.debug("{}: найден номер телефона ответственного сотрудника: {}", user.getLogin(), responsibleOperatorNumber);

            autodetectTypeAndDialToNumber(responsibleOperatorNumber);
        } catch (AgiException ae) {
            throw ae;
        } catch (Exception e) {
            e.printStackTrace();//вообще ignored но для первичного дебага так будет
        }
    }


    private void playMelody(UserAudio melody) throws AgiException {
        String filename = melody.getFilename();
        filename = filename.substring(0, filename.length() - 4); //астериск не принимает расширение файлов
        String filePath = "/var/lib/asterisk/sounds/user/" + threadLocalLogin.get() + "/" + filename;
        LOGGER.trace("{}: проигрывание файла {}. Мелодия {}", threadLocalLogin.get(), filePath, melody);
        streamFile(filePath);
    }


    public static HashMap<String, Rule> getPhoneNumbersAndRules() {
        return phoneNumbersAndRules;
    }

    public static void setPhoneNumbersAndRules(HashMap<String, Rule> phoneNumbersAndRules) {
        AgiInProcessor.phoneNumbersAndRules = phoneNumbersAndRules;
    }

    /**
     * Алгоритмы редиректов
     */

    private void processToAllRedirect(ChainElement element) throws AgiException, InterruptedException {
        List<String> toList = element.getToList();
        int awaitingTime = element.getAwaitingTime();
        DestinationType destinationType = element.getDestinationType();


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
            dialToSipGroup(callList, awaitingTime);

        } else if (destinationType == GSM) {
            // Звоним на все GSMы
            dialToGsmGroup(toList, awaitingTime);
        }
    }

    private void processQueueRedirect(ChainElement element) throws AgiException {
        List<String> toList = element.getToList();
        int awaitingTime = element.getAwaitingTime();
        DestinationType destinationType = element.getDestinationType();

        for (String operatorNumber : toList) {
            if (destinationType == SIP) {
                HashMap<String, Boolean> sipsState = AsteriskMonitor.getSipsFree(); //берём только тех, кто не разговаривает и не звонит

                boolean operatorIsFree = sipsState.get(operatorNumber);
                if (operatorIsFree) {// оператор свободен
                    dialToSipGroup(Arrays.asList(operatorNumber), awaitingTime);
                } else {// оператор занят. Ищем следующего
                    continue;
                }

            } else if (destinationType == GSM) {

                dialToGsmGroup(Arrays.asList(operatorNumber), awaitingTime);
            }
        }
    }

    private void processRandomRedirect(ChainElement element) throws AgiException {
        List<String> toList = element.getToList();
        int awaitingTime = element.getAwaitingTime();
        DestinationType destinationType = element.getDestinationType();


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
                dialToSipGroup(Arrays.asList(choosenOperator), awaitingTime);
            }
        } else if (destinationType == GSM) {
            int numbersOfFreeOperators = toList.size();
            int randomInt = random.nextInt(numbersOfFreeOperators);
            String choosenOperator = toList.get(randomInt);
            dialToGsmGroup(Arrays.asList(choosenOperator), awaitingTime);
        }
    }

    /**
     * Звонки
     */
    private void autodetectTypeAndDialToNumber(String number) throws AgiException {
        if (number.length() <= 8) {//оператор на сипке сидит
            HashMap<String, Boolean> sipsState = AsteriskMonitor.getSipsFree();
            boolean operatorIsFree = sipsState.get(number);
            if (!operatorIsFree) { // оператор занят.
                return;
            }
            dialToSipGroup(Collections.singletonList(number), 10);
        } else {
            dialToGsmGroup(Collections.singletonList(number), 13);
        }
    }


    private void dialToSipGroup(List<String> sips, int timeout) throws AgiException {
        getChannelStatus();// это чисто что бы вылетел hangUpException если канал уже не существует
        LOGGER.debug("{}: Производится звонок на группу SIP {} с ожиданием {} секунд", threadLocalLogin.get(), sips, timeout);
        if (sips.size() == 0) {
            LOGGER.warn("{}: Список номеров в SIP группе пуст", threadLocalLogin.get());
            return;
        }

        String result = "";
        for (String number : sips) {
            result += "&SIP/" + number;
        }
        result = result.substring(1);
        result = result + "," + timeout + ",m(" + threadLocalMelody.get() + ")";
        setVariable("redirectedTo", sips.toString());
//        AmoMultiThreadController.sendWsMessageCallingToNumber(sips);
        int responseCode = exec("DIAL", result);
        LOGGER.debug("{}: SIPS {}: {}", threadLocalLogin.get(), sips, getStringedCode(responseCode));
        if (responseCode == -1) {
            LOGGER.trace("{}: SIP {}: разговор состоялся. Завершаем вызов: Hangup", threadLocalLogin.get(), sips);
            hangup();
        } else if (responseCode == 0) {
            LOGGER.trace("{}: SIP {}: Никто не ответил.", threadLocalLogin.get(), sips);
        }
    }

    private void dialToGsmGroup(List<String> numbers, int timeout) throws AgiException {
        getChannelStatus();// это чисто что бы вылетел hangUpException если канал уже не существует
        LOGGER.debug("{}: Производится звонок на группу GSM {} с ожиданием {} секунд", threadLocalLogin.get(), numbers, timeout);

        if (numbers.size() == 0) {
            LOGGER.warn("{}: Список номеров в GSM группе пуст", threadLocalLogin.get());
            return;
        }

        String result = "";
        for (String number : numbers) {
            result += "&SIP/Intertelekom_main/" + number;
        }
        result = result.substring(1);
        result = result + "," + timeout + ",m(" + threadLocalMelody.get() + ")";

        setVariable("redirectedTo", numbers.toString());
//        AmoMultiThreadController.sendWsMessageCallingToNumber(numbers);
        int responseCode = exec("DIAL", result);
        LOGGER.debug("{}: GSM {}: {}",threadLocalLogin.get(), numbers, getStringedCode(responseCode));
        if (responseCode == -1) {
            LOGGER.trace("{}: GSM {}: разговор состоялся. Завершаем вызов: Hangup",threadLocalLogin.get(), numbers);
            hangup();
        }
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


    public static void main(String[] args) throws Exception {
        Cli cli = new Cli();
        cli.parseOptions(new String[]{});
    }
}