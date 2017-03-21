package ua.adeptius.asterisk.model;

import java.io.IOException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;

public class AsteriskManager {
    private ManagerConnection managerConnection;

    public AsteriskManager() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(
                "localhost", "manager", "pa55w0rd");

        this.managerConnection = factory.createManagerConnection();
    }

    public void run() throws IOException, AuthenticationFailedException, TimeoutException {
        OriginateAction originateAction;
        ManagerResponse originateResponse;

        originateAction = new OriginateAction();
        originateAction.setChannel("SIP/John");
        originateAction.setContext("default");
        originateAction.setExten("1300");
        originateAction.setPriority(1);
        originateAction.setTimeout(30000L);


        // connect to Asterisk and log in
        managerConnection.login();

        // send the originate action and wait for a maximum of 30 seconds for Asterisk
        // to send a reply
        originateResponse = managerConnection.sendAction(originateAction, 30000);

        // print out whether the originate succeeded or not
        System.out.println(originateResponse.getResponse());

        // and finally log off and disconnect
        managerConnection.logoff();
    }

    public static void main(String[] args) throws Exception {
        AsteriskManager helloManager;

        helloManager = new AsteriskManager();
        helloManager.run();
    }
}