package ua.adeptius.asterisk.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.*;
import org.asteriskjava.manager.response.CommandResponse;
import org.asteriskjava.manager.response.ManagerResponse;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;

public class HelloManager {

    private ManagerConnection managerConnection;

    private HelloManager() throws IOException, AuthenticationFailedException, TimeoutException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory("cstat.nextel.com.ua", "adeptius", "ccb6f130f89de0bab95df361669e32ba");
        this.managerConnection = factory.createManagerConnection();
        managerConnection.login();
    }

    public static void main(String[] args) throws Exception {
        HelloManager helloManager = new HelloManager();
        helloManager.call();
    }

    public void call() throws IOException, AuthenticationFailedException, TimeoutException, InterruptedException {
//        CommandAction action = new CommandAction();
//        action.setCommand("dialplan reload");
//        action.setCommand("core reload");
//        action.setCommand("sip reload");
//        action.setCommand("core show help");
//        action.setCommand("sip show peers");
//        action.setCommand("config reload core");
//        action.setCommand("config list ");
////        action.setCommand("core show hanguphandlers all");

//        action.setCommand("dialplan add extension 6000001,1,Dial into pa-call-file");
//        CommandResponse response = (CommandResponse) managerConnection.sendAction(action);
//        List<String> list = response.getResult();
//        list.forEach(System.err::println);

        OriginateAction action = new OriginateAction();
        action.setChannel("SIP/Intertelekom_main/0934027182");
        action.setExten("2001036");
        action.setCallerId("0934027182");
        action.setContext("from-internal");
        action.setPriority(1);
        action.setApplication("AGI");
        action.setData("agi://78.159.55.63/in_c2c_processor.agi");


//        OriginateAction action = AsteriskActionsGenerator.callToOutside("2001036", "0934027182", "Vova");
        ManagerResponse originateResponse = managerConnection.sendAction(action, 20000);
        System.err.println(originateResponse);
        managerConnection.logoff();
    }
}