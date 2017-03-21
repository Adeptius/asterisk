package ua.adeptius.asterisk.forwarding;


import java.util.ArrayList;

import static ua.adeptius.asterisk.forwarding.DestinationType.*;
import static ua.adeptius.asterisk.forwarding.ForwardType.*;

public class Main {


    public static void main(String[] args) {
        Exten exten = new Exten();

        ArrayList<String> listFrom = new ArrayList<>();
        listFrom.add("443211125");
        listFrom.add("555555555");
        exten.setFrom(listFrom);

        ArrayList<String> listTo = new ArrayList<>();
        listTo.add("3211125");
        listTo.add("2222222");
        exten.setTo(listTo);

        exten.setForwardType(TO_ALL);
        exten.setDestinationType(GSM);
        exten.setTime(15);
        exten.setMelody("m(simple)");

        System.out.println(exten);

    }


}
