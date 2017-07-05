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
        Thread.sleep(2000);



//        OriginateAction action = new OriginateAction();
//        action.setChannel(chanel);
//        action.setExten("2001037");
//        action.setContext(context);
//        action.setPriority(1);

//        HangupAction action = new HangupAction(chanel);
//        MuteAudioAction action = new MuteAudioAction(chanel, MuteAudioAction.Direction.ALL, MuteAudioAction.State.MUTE  );





//from-sip-external   Timeout waiting for response to Command
//        Набранный вами номер отключен

//        [2017-06-29 23:13:59] WARNING[29763][C-0000045c]: pbx.c:4365 __ast_pbx_run:
// Channel 'SIP/Kievstar-000003fd' sent to invalid extension but no invalid handler: context,exten,priority=from-word,2001036,1
//                == MixMonitor close filestream (mixed)
//                == End MixMonitor Recording SIP/Kievstar-000003fd



//        RedirectAction action = new RedirectAction();
//        action.setChannel(chanel);
//        action.setContext("from-internal");
////        action.setExtraContext("from-internal");
//        action.setExten(exten);
////        action.setExtraExten(exten);
//        action.setPriority(1);
//        ManagerResponse originateResponse = managerConnection.sendAction(action, 5000);
//        System.err.println(action);


//        Набранный номер не может быть вызван. Пожалуйста проверьте и повторите попытку.
//        переадресация первого канала который приходит на шлюз.



        RedirectAction action = new RedirectAction();
        action.setChannel(chanel);
        action.setContext("from-internal");
        action.setExten("0994803031");
        action.setPriority(1);
        ManagerResponse originateResponse = managerConnection.sendAction(action, 5000);
        System.err.println(action);


//        Thread.sleep(10000);
//        action = new RedirectAction();
//        action.setChannel(chanel);
//        action.setContext("from-internal");
//        action.setExten("2001037");
//        action.setPriority(1);
//        originateResponse = managerConnection.sendAction(action, 5000);
//        System.err.println(action);







//        TODO сделать это каким-то нормальным классом
        System.err.println(originateResponse);
        managerConnection.logoff();
    }

//    public static void main(String[] args) throws Exception {
//        HelloManager helloManager = new HelloManager();
//        helloManager.call("", "");
//    }
}