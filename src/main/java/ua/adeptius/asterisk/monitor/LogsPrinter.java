package ua.adeptius.asterisk.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.event.ManagerEvent;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.json.RoistatPhoneCall;
import ua.adeptius.asterisk.model.Call;
import ua.adeptius.asterisk.model.RoistatAccount;
import ua.adeptius.asterisk.model.User;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogsPrinter extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(RoistatPhoneCall.class.getSimpleName());
    private LinkedBlockingQueue<List<ManagerEvent>> blockingQueue = new LinkedBlockingQueue<>();
    private static ObjectMapper mapper = new ObjectMapper();

    public void send(List<ManagerEvent> list) {
        try {
            blockingQueue.put(list);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }


    public LogsPrinter() {
        setName("LogsPrinter");
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<ManagerEvent> list = blockingQueue.take();
                print(list);
            } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
            }
        }
    }


    private void print(List<ManagerEvent> list) {
        System.err.println("---------------------");
        for (ManagerEvent managerEvent : list) {
            System.err.println(makePrettyLog(managerEvent));
        }
        System.err.println("---------------------");
    }

    private static String makePrettyLog(ManagerEvent event) {
        String s = event.toString();
        s = s.substring(31);
        if (s.contains("timestamp=null,")) {
            s = s.replaceAll("timestamp=null,", "");
        }
        if (s.contains("sequencenumber=null,")) {
            s = s.replaceAll("sequencenumber=null,", "");
        }
        if (s.contains("server=null,")) {
            s = s.replaceAll("server=null,", "");
        }
        if (s.contains("actionid=null,")) {
            s = s.replaceAll("actionid=null,", "");
        }
        if (s.contains("connectedlinenum=null,")) {
            s = s.replaceAll("connectedlinenum=null,", "");
        }
        if (s.contains("accountcode=null,")) {
            s = s.replaceAll("accountcode=null,", "");
        }
        if (s.contains("connectedlinename=null,")) {
            s = s.replaceAll("connectedlinename=null,", "");
        }
        if (s.contains("calleridname=null,")) {
            s = s.replaceAll("calleridname=null,", "");
        }
        s = removeRegexFromString(s, "dateReceived='.*2017',");
        s = removeRegexFromString(s, "systemHashcode=\\d{8,10}");
        s = removeRegexFromString(s, "channel='SIP\\/\\d*-[\\d|\\w]*',");
        s = removeRegexFromString(s, "privilege='\\w*,\\w*',");
        s = removeRegexFromString(s, "privilege='\\w*,\\w*',");
        s = removeRegexFromString(s, "priority='\\d*',");
        return s;
    }

    private static String removeRegexFromString(String log, String regex) {
        Matcher regexMatcher = Pattern.compile(regex).matcher(log);
        if (regexMatcher.find()) {
            log = log.replaceAll(regexMatcher.group(), "");
        }
        return log;
    }

}
