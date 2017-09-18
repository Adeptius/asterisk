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


//        long t0 = System.nanoTime();
        CommandAction action = new CommandAction();
        action.setCommand("dialplan reload");
//        action.setCommand("core reload");
//        action.setCommand("sip reload");
//        action.setCommand("core show help");
//        action.setCommand("config reload core");
//        action.setCommand("service asterisk reload");
//        action.setCommand("config list ");
////        action.setCommand("core show hanguphandlers all");
        CommandResponse response = (CommandResponse) managerConnection.sendAction(action);
        List<String> list = response.getResult();
//        System.out.println(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - t0));

        list.forEach(System.err::println);

//        OriginateAction action = AsteriskActionsGenerator.callToOutside("2001036", "0934027182", "Vova");
//        ManagerResponse originateResponse = managerConnection.sendAction(action, 20000);
//        System.err.println(originateResponse);
        managerConnection.logoff();
    }
}