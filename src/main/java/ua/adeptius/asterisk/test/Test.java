package ua.adeptius.asterisk.test;


import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.Call;
import ua.adeptius.asterisk.senders.GoogleAnalitycsCallSender;

import java.lang.reflect.Field;
import java.util.Date;

public class Test {

    public static void main(String[] args) {





//        RoistatCallSender roistatCallSender = new RoistatCallSender();
//        GoogleAnalitycsCallSender googleAnalitycsCallSender = new GoogleAnalitycsCallSender();

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        User user = new User();
        user.setTrackingId("UA-20012098-2");

        Call call = new Call();
        call.setUser(user);
        call.setUtm("someUtm");
        call.setId("23131321321321");
        call.setService(Call.Service.TELEPHONY);
        call.setGoogleId("3524165841.584654646");
        call.setEnded(50);
        call.setAnswered(5);
        call.setCallState(Call.CallState.ANSWERED);
        call.setCalled("2016-06-21 22:04:49");
        call.setDirection(Call.Direction.OUT);
        call.setCalledMillis(new Date().getTime());
        call.setTo("0934027182");
        call.setFrom("200103711");




        try {
            Field field = call.getClass().getDeclaredField("id");
            field.setAccessible(true);
            String s = (String) field.get(call);
            System.out.println(s);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }



//        RoistatPhoneCall roistatPhoneCall = new RoistatPhoneCall(call);


//            roistatCallSender.send(roistatPhoneCall);
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//        new GoogleAnalitycs(call).start();
//        googleAnalitycsCallSender.send(call);
    }
}
