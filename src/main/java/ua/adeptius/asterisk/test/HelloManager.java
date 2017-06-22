package ua.adeptius.asterisk.test;

import java.io.IOException;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
import ua.adeptius.asterisk.utils.AsteriskActionsGenerator;

public class HelloManager {

    private ManagerConnection managerConnection;

    public HelloManager() throws IOException {
        ManagerConnectionFactory factory = new ManagerConnectionFactory("cstat.nextel.com.ua", "adeptius", "ccb6f130f89de0bab95df361669e32ba");

        this.managerConnection = factory.createManagerConnection();
    }

    public void call() throws IOException, AuthenticationFailedException, TimeoutException {
        OriginateAction action;
        ManagerResponse originateResponse;

        action = AsteriskActionsGenerator.callToOutside("2001037", "0934027182");

        managerConnection.login();
        originateResponse = managerConnection.sendAction(action, 30000);
        System.out.println(originateResponse);
        managerConnection.logoff();
    }

    public static void main(String[] args) throws Exception {
        HelloManager helloManager = new HelloManager();
        helloManager.call();
    }
}