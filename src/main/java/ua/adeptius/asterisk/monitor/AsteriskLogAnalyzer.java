package ua.adeptius.asterisk.monitor;

import org.asteriskjava.manager.event.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AsteriskLogAnalyzer {

    private static HashMap<String, List<ManagerEvent>> chanelsAndEvents = new HashMap<>();
    private static LogsPrinter logsPrinter = new LogsPrinter();

    public static void analyze(ManagerEvent event){

        if (event instanceof NewChannelEvent) { // если это newChannel - добавляем в мапу новый ключ
            NewChannelEvent newChannelEvent = (NewChannelEvent) event;
            List<ManagerEvent> list = new ArrayList<>();
            list.add(newChannelEvent);
            chanelsAndEvents.put(newChannelEvent.getChannel(), list);
            return;
        }

        if (event instanceof NewExtenEvent) {
            NewExtenEvent newExtenEvent = (NewExtenEvent) event;
            List<ManagerEvent> list = chanelsAndEvents.get(newExtenEvent.getChannel());
            if (list == null){
                return;// null может быть только если NewChannelEvent был до запуска сервера
            }
            list.add(newExtenEvent);
            return;
        }


        if (event instanceof VarSetEvent) {
            VarSetEvent varSetEvent = (VarSetEvent) event;
            List<ManagerEvent> list = chanelsAndEvents.get(varSetEvent.getChannel());
            if (list == null){
                return;// null может быть только если NewChannelEvent был до запуска сервера
            }
            list.add(varSetEvent);
            return;
        }

        if (event instanceof HangupEvent) {
            HangupEvent hangupEvent = (HangupEvent) event;// если окончание разговора - удаяем из мапы и передаём список дальше
            List<ManagerEvent> list = chanelsAndEvents.remove(hangupEvent.getChannel());
            if (list == null){
                return;// null может быть только если NewChannelEvent был до запуска сервера
            }
            list.add(hangupEvent);

            logsPrinter.send(list);
        }
    }
}
