package ua.adeptius.asterisk.test;

import java.io.IOException;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.action.MuteAudioAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.action.RedirectAction;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.response.ManagerResponse;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;

public class HelloManager {

    private ManagerConnection managerConnection;

    public HelloManager() throws IOException, AuthenticationFailedException, TimeoutException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory("cstat.nextel.com.ua", "adeptius", "ccb6f130f89de0bab95df361669e32ba");
        this.managerConnection = factory.createManagerConnection();
        managerConnection.login();

    }

    public void call(String chanel, String context) throws IOException, AuthenticationFailedException, TimeoutException, InterruptedException {

//        OriginateAction action = AsteriskActionsGenerator.callToOutside("2001037", "0934027182");
//        RedirectAction action = new RedirectAction("SIP/201036","from-word","201036",1);
        Thread.sleep(3000);



//        OriginateAction action = new OriginateAction();
//        action.setChannel(chanel);
//        action.setExten("2001037");
//        action.setContext(context);
//        action.setPriority(1);

//        HangupAction action = new HangupAction(chanel);
//        MuteAudioAction action = new MuteAudioAction(chanel, MuteAudioAction.Direction.ALL, MuteAudioAction.State.MUTE  );







        RedirectAction action = new RedirectAction();
        action.setChannel(chanel);
        action.setContext("from-internal");
        action.setExten("2001039");
        action.setPriority(20);
        ManagerResponse originateResponse = managerConnection.sendAction(action, 5000);










//        TODO сделать это каким-то нормальным классом
        System.err.println(originateResponse);
        managerConnection.logoff();
    }

//    public static void main(String[] args) throws Exception {
//        HelloManager helloManager = new HelloManager();
//        helloManager.call("", "");
//    }
}