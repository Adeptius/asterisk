package ua.adeptius.asterisk.test;

import java.io.IOException;
import java.util.List;

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
        CommandAction action= new CommandAction();
//        action.setCommand("sip show channelstats");
        action.setCommand("core show help");
////        action.setCommand("core show hanguphandlers all");
        CommandResponse response = (CommandResponse) managerConnection.sendAction(action);
        List<String> list = response.getResult();
        list.forEach(System.err::println);

//        OriginateAction action = AsteriskActionsGenerator.callToOutside("2001036", "0934027182", "Vova");
//        ManagerResponse originateResponse = managerConnection.sendAction(action, 20000);
//        System.err.println(originateResponse);
        managerConnection.logoff();
    }
}