package ua.adeptius.asterisk.utils;


import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.action.RedirectAction;
import org.asteriskjava.manager.response.CommandResponse;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AsteriskActionsGenerator {


    public static OriginateAction callToOutside(String internalPhone, String outerPhone, @Nullable String callerName){
        OriginateAction action = new OriginateAction();
        action.setChannel("SIP/"+internalPhone);
        action.setExten(outerPhone);
        if (callerName != null) {
            action.setCallerId(callerName);
        }else {
            action.setCallerId(outerPhone);
        }
        action.setContext("from-internal");
        action.setPriority(1);
        return action;
    }

    public static OriginateAction callToOutsideFromOuter(String outerPhone, String destinationPhone, @Nullable String callerName){
        OriginateAction action = new OriginateAction();
        action.setChannel("SIP/Intertelekom_main/"+outerPhone);
        action.setExten(destinationPhone);
        if (callerName != null) {
            action.setCallerId(callerName);
        }else {
            action.setCallerId(outerPhone);
        }
        action.setContext("from-internal");
        action.setPriority(1);
        return action;
    }

    public static RedirectAction redirectChanelToSip(String chanel, String sipTo){
        RedirectAction action = new RedirectAction();
        action.setChannel(chanel);
        action.setContext("from-internal");
        action.setExten(sipTo);
        action.setPriority(1);
        return action;
    }



}

