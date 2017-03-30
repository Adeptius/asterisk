package ua.adeptius.asterisk.forwarding;


import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.forwarding.DestinationType.*;
import static ua.adeptius.asterisk.forwarding.ForwardType.*;

public class Rule {


    private ArrayList<String> from = new ArrayList<>();
    private ArrayList<String> to = new ArrayList<>();
    private ForwardType forwardType = BY_TURNS;
    private DestinationType destinationType = GSM;
    private int time;
    private String melody;
    private String siteName;

    public Rule() {
    }

    public Rule(List<String> lines) {
        for (String line : lines) {
            String numbers = line.substring(line.indexOf("(")+1, line.lastIndexOf(")")); // получаем то, что содержится в скобках

            if (line.contains("Goto")){// определяем сип ли это
                destinationType = SIP;
            }else { // время есть только в GSM
                String s = line.substring(0, line.lastIndexOf(","));
                s = s.substring(s.lastIndexOf(",")+1);
                time = Integer.parseInt(s); // узнали время

                // мелодия есть только в GSM
                melody = numbers.substring(numbers.lastIndexOf(",")+1);
            }

            if (line.contains("&")){
                forwardType = TO_ALL;
            }
            String numberFrom = line.substring(line.indexOf("exten =>")+9, line.indexOf(","));
            addNumberFrom(numberFrom);


            if (forwardType == TO_ALL){ // если всем сразу выбираем номера
                String[] splitted = numbers.substring(0,numbers.indexOf(",")).split("&");
                for (String s : splitted) {
                    addNumberTo(s.substring(s.lastIndexOf("/")+1));
                }
            }else if (forwardType == BY_TURNS){ // если по очереди выбираем номера

                if (destinationType == SIP){
                    String s = numbers;
                    s = s.substring(s.indexOf("direct,")+7);
                    s = s.substring(0, s.indexOf(","));
                    addNumberTo(s);
                }else {
                    String s = numbers.substring(0, numbers.indexOf(","));
                    s = s.substring(s.lastIndexOf("/")+1);
                    addNumberTo(s);
                }
            }
//            System.out.println(line);
        }
        System.out.println(this);
    }

    public String getConfig() {
        StringBuilder builder = new StringBuilder();
        builder.append("; Start Rule\n");
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

        if (builder.toString().endsWith("\n")){
            builder.deleteCharAt(builder.length()-1);
        }
        builder.append("; End Rule\n");
        return builder.toString();
    }


    public void addNumberTo(String number){
        if (!to.contains(number)){
            to.add(number);
        }
    }

    public void addNumberFrom(String number){
        if (!from.contains(number)){
            from.add(number);
        }
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


    @Override
    public String toString() {
        return "Rule{" +
                "from=" + from +
                ", to=" + to +
                ", forwardType=" + forwardType +
                ", destinationType=" + destinationType +
                ", time=" + time +
                ", melody='" + melody + '\'' +
                '}';
    }
}
