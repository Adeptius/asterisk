package ua.adeptius.asterisk.utils;


import org.asteriskjava.manager.action.OriginateAction;

public class AsteriskActionsGenerator {


    public static OriginateAction callToOutside(String internalPhone, String outerPhone){
        OriginateAction action = new OriginateAction();
        action.setChannel("SIP/"+internalPhone);
        action.setExten(outerPhone);
        action.setContext("from-internal");
        action.setPriority(1);
//        action.setVariable("customernum", "1212");
//        action.setVariable("SIPURI", "sip:2001037@78.159.55.63:39566");
//        action.setVariable("SIPDOMAIN", "cstat.nextel.com.ua");
//        action.setVariable("calleridnum", "1212");
//        action.setVariable("calleridname", "2001037");
        return action;
    }





}

