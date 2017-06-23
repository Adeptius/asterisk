package ua.adeptius.asterisk.monitor;

import org.asteriskjava.manager.action.ManagerAction;
import org.asteriskjava.manager.event.*;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.Settings;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.StatusAction;

import java.io.IOException;
import java.util.HashMap;

import static ua.adeptius.asterisk.utils.logging.LogCategory.ANSWER_CALL;
import static ua.adeptius.asterisk.utils.logging.LogCategory.ENDED_CALL;
import static ua.adeptius.asterisk.utils.logging.LogCategory.INCOMING_CALL;

@SuppressWarnings("Duplicates")
public class AsteriskMonitor implements ManagerEventListener {

    private static Logger LOGGER = LoggerFactory.getLogger(AsteriskMonitor.class.getSimpleName());

    private ManagerConnection managerConnection;

    public AsteriskMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                Settings.getSetting("___asteriskAdress"),
                Settings.getSetting("___asteriskLogin"),
                Settings.getSetting("___asteriskPassword"));
        this.managerConnection = factory.createManagerConnection();
    }

    public void run() throws IOException, AuthenticationFailedException,
            TimeoutException, InterruptedException {
        managerConnection.addEventListener(this);
        managerConnection.login();
        //TODO сделать сюда отправку екшенов
        managerConnection.sendAction(new StatusAction());
    }

    public ManagerResponse sendAction(ManagerAction action, long timeout) throws IOException, TimeoutException {
        return managerConnection.sendAction(action, timeout);
    }

    public void onManagerEvent(ManagerEvent event) {
//        System.out.println(event);
        /**
         if (!(event instanceof NewChannelEvent) // Первоначальный фильтр
         && !(event instanceof HangupEvent)
         && !(event instanceof NewStateEvent)
         && !(event instanceof NewExtenEvent)
         && !(event instanceof VarSetEvent)
         ) {
         return;
         }

         if ((event instanceof VarSetEvent)&& !( ((VarSetEvent) event).getVariable().equals("DIALSTATUS"))){
         return;
         }

         if ((event instanceof NewExtenEvent) && !((NewExtenEvent) event).getApplication().equals("Dial")) {
         return; // фильтр для переадресации на GSM
         }

         if ((event instanceof NewStateEvent) && ((NewStateEvent) event).getChannelStateDesc().equals("Ring")) {
         return; // фильтр для переадресации на GSM
         }
         **/


        if (event instanceof NewChannelEvent) {
            CallProcessor.processEvent(event, ((NewChannelEvent) event).getUniqueId());


        } else if (event instanceof HangupEvent) {
            CallProcessor.processEvent(event, ((HangupEvent) event).getUniqueId());
//        } else if (event instanceof NewStateEvent) {
//            CallProcessor.processEvent(event,((NewStateEvent)event).getUniqueId());


        } else if (event instanceof NewExtenEvent) {
            NewExtenEvent extenEvent = (NewExtenEvent) event;
            if (!(extenEvent.getApplication().equals("Dial"))) {
                return;
            }
            CallProcessor.processEvent(event, extenEvent.getUniqueId());


        } else if (event instanceof VarSetEvent) {
            VarSetEvent varSetEvent = (VarSetEvent) event;
            String key = varSetEvent.getVariable();
            String value = varSetEvent.getValue();
            if (
                    key.equals("ARG3")
                    || key.equals("ARG1")
                    || key.equals("ARG2")
                    || key.equals("ARG4")
                    || key.equals("MACRO_PRIORITY")
                    || key.equals("MACRO_DEPTH")
                    || key.equals("SIPURI")
                    || key.equals("SIPDOMAIN")
                    || key.equals("SIPCALLID")
                    || key.equals("TOUCH_MONITOR")
                    || key.equals("__REC_STATUS")
                    || key.equals("__DAY")
                    || key.equals("__MONTH")
                    || key.equals("__TIMESTR")
                    || key.equals("__FROMEXTEN")
                    || key.equals("__MON_FMT")
                    || key.equals("__REC_POLICY_MODE")
                    || key.equals("__CALLFILENAME")
                    || key.equals("LOCAL_MIXMON_ID")
                    || key.equals("__MIXMON_ID")
                    || key.equals("__RECORD_ID")
                    || key.equals("__REC_STATUS")
                    || key.equals("NOW")
                    || key.equals("TRUNKOUTCID")
                    || key.equals("custom")
                    || key.equals("DIALEDPEERNAME")
                    || key.equals("DIALEDPEERNUMBER")
                    || key.equals("BRIDGEPEER")
                    || key.equals("BRIDGEPVTCALLID")
                    || value.equals("null")
                    || value.equals("default")
                    || value.equals("1")
                    || value.equals("2")
                    || value.equals("3")
                    || value.equals("4")
                    || value.equals("5")
                    || value.equals("6")
                    || value.equals("7")
                    || value.equals("8")
                    ) {
                return; // ненужные данные
            }

            CallProcessor.processEvent(event, varSetEvent.getUniqueId());
        }
    }
}