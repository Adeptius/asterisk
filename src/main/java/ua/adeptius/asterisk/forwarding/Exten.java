package ua.adeptius.asterisk.forwarding;


import java.util.ArrayList;

import static ua.adeptius.asterisk.forwarding.DestinationType.*;
import static ua.adeptius.asterisk.forwarding.ForwardType.*;

public class Exten {


    private ArrayList<String> from;
    private ArrayList<String> to;
    private ForwardType forwardType;
    private DestinationType destinationType;
    private int time;
    private String melody;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < from.size(); i++) {
            String numberFrom = from.get(i);
            builder.append("exten => ").append(numberFrom).append(",1,Noop(${CALLERID(num)})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Gosub(sub-record-check,s,1(in,${EXTEN},force))\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(__FROM_DID=${EXTEN})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(CDR(did)=${FROM_DID})\n");
            builder.append("exten => ").append(numberFrom).append(",n,Set(num=${CALLERID(num)})\n");

            if (destinationType == SIP){
                for (String sipTo : to) {
                    builder.append("exten => ").append(numberFrom).append(",n(dest-ext),Goto(from-did-direct,").append(sipTo).append(",1)\n");
                }
            }else if (destinationType == GSM){
                if (forwardType == BY_TURNS){ // По очереди
                    for (String numberTo : to) {
                        builder.append("exten => ").append(numberFrom).append(",n,Dial(SIP/Intertelekom_main/")
                                .append(numberTo).append(",").append(time).append(",").append(melody).append(")\n");
                    }
                }else if (forwardType == TO_ALL){ // Сразу всем
                    builder.append("exten => ").append(numberFrom).append(",n,Dial(");
                    for (int j = 0; j < to.size(); j++) {
                        builder.append("SIP/Intertelekom_main/").append(to.get(j));
                        if (j != to.size()-1){
                            builder.append("&");
                        }
                    }
                    builder.append(",").append(600).append(",").append(melody).append(")\n");
                }
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public ArrayList<String> getFrom() {
        return from;
    }

    public void setFrom(ArrayList<String> from) {
        this.from = from;
    }

    public ArrayList<String> getTo() {
        return to;
    }

    public void setTo(ArrayList<String> to) {
        this.to = to;
    }

    public ForwardType getForwardType() {
        return forwardType;
    }

    public void setForwardType(ForwardType forwardType) {
        this.forwardType = forwardType;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getMelody() {
        return melody;
    }

    public void setMelody(String melody) {
        this.melody = melody;
    }
}
