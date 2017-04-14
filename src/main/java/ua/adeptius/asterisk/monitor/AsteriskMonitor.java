package ua.adeptius.asterisk.monitor;

import org.asteriskjava.manager.event.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.Settings;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.StatusAction;

import java.io.IOException;
import java.util.HashMap;

import static ua.adeptius.asterisk.utils.logging.LogCategory.ANSWER_CALL;
import static ua.adeptius.asterisk.utils.logging.LogCategory.ENDED_CALL;
import static ua.adeptius.asterisk.utils.logging.LogCategory.INCOMING_CALL;

public class AsteriskMonitor implements ManagerEventListener {

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
        managerConnection.sendAction(new StatusAction());
    }


    public void onManagerEvent(ManagerEvent event) {
//        System.out.println(event);
        if (!(event instanceof NewChannelEvent)
                && !(event instanceof HangupEvent)
                && !(event instanceof NewStateEvent)
//                && !(event instanceof VarSetEvent)
                && !(event instanceof NewExtenEvent)) {
            return;
        }

//        if ((event instanceof VarSetEvent)) {
//            if (!((VarSetEvent) event).getVariable().equals("DIALEDPEERNUMBER") || ((VarSetEvent) event).getValue().equals("null"))
//            return; // фильтр для переадресации на GSM
//        }  ТУТ ЕСТЬ ИНФА НА КОГО ПЕРЕАДРЕСАЦИЯ


        if ((event instanceof NewExtenEvent) && !((NewExtenEvent) event).getApplication().equals("Dial")) {
            return; // фильтр для переадресации на GSM
        }

        if ((event instanceof NewStateEvent) && ((NewStateEvent) event).getChannelStateDesc().equals("Ring")) {
            return; // фильтр для переадресации на GSM
        }


//        if (event.toString().contains("4027182") ||
//                event.toString().contains("5306914") ||
//                event.toString().contains("443211118") ||
//                event.toString().contains("8999500")) {
            CallProcessor.processEvent(event);
//        }
    }
}