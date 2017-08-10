package ua.adeptius.asterisk.monitor;

import org.asteriskjava.manager.action.ManagerAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.event.*;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.dao.Settings;
import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.StatusAction;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;

import java.io.IOException;

@SuppressWarnings("Duplicates")
public class AsteriskMonitor implements ManagerEventListener {

    private static Logger LOGGER = LoggerFactory.getLogger(AsteriskMonitor.class.getSimpleName());

    private ManagerConnection managerConnection;

    public AsteriskMonitor() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                Settings.getSetting("asteriskAdress"),
                Settings.getSetting("asteriskLogin"),
                Settings.getSetting("asteriskPassword"));
        this.managerConnection = factory.createManagerConnection();
    }

    public void run() throws IOException, AuthenticationFailedException,
            TimeoutException, InterruptedException {
        managerConnection.addEventListener(this);
        managerConnection.login();
        managerConnection.sendAction(new StatusAction());
    }


    public ManagerResponse sendAction(ManagerAction action, long timeout) throws IOException, TimeoutException {
        return managerConnection.sendAction(action, timeout);
    }


    public ManagerResponse sendCallToOutsideAction(String from, String to) throws IOException, TimeoutException {
        OriginateAction callToOutside = AsteriskActionsGenerator.callToOutside(from, to);
        return managerConnection.sendAction(callToOutside, 10000);
    }

    public void onManagerEvent(ManagerEvent event) {
        try{
            if (event instanceof NewChannelEvent) {
                CallProcessor.processEvent(event, ((NewChannelEvent) event).getUniqueId());

            } else if (event instanceof HangupEvent) {
                CallProcessor.processEvent(event, ((HangupEvent) event).getUniqueId());

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
                if (!key.equals("DIALSTATUS")) {
                    return;
                }
                if (value == null) {
                    return;
                }
                CallProcessor.processEvent(event, varSetEvent.getUniqueId());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}