package ua.adeptius.asterisk.test;

import ua.adeptius.asterisk.telephony.ForwardType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ua.adeptius.asterisk.telephony.ForwardType.QUEUE;
import static ua.adeptius.asterisk.telephony.ForwardType.TO_ALL;

public class AsteriskQueqe {

    public ForwardType getForwardType() {
        return forwardType;
    }

    public void setForwardType(ForwardType forwardType) {
        this.forwardType = forwardType;
    }

    private List<String> sips = new ArrayList<>();

    private ForwardType forwardType;

    private HashMap<String, AtomicInteger> sipsAndCount = new HashMap<>();

    private String lastSipNumber = "";

    private AtomicInteger lastSipIndex = new AtomicInteger(-1);

    public List<String> getSips() {
        return sips;
    }

    public void setSips(List<String> sips) {
        this.sips = sips;
    }

    public HashMap<String, AtomicInteger> getSipsAndCount() {
        return sipsAndCount;
    }

    public void setSipsAndCount(HashMap<String, AtomicInteger> sipsAndCount) {
        this.sipsAndCount = sipsAndCount;
    }

    public String getLastSipNumber() {
        return lastSipNumber;
    }

    public void setLastSipNumber(String lastSipNumber) {
        this.lastSipNumber = lastSipNumber;
    }

    public int getLastSipIndex() {
        return lastSipIndex.get();
    }

    public void setLastSipIndex(int lastSipIndex) {
        this.lastSipIndex.set(lastSipIndex);
    }

//    public String getNextOperator(){
//        if (forwardType == TO_ALL){
//
//        }else if (forwardType == QUEUE){
//
//        }else if (forwardType == ROUND){
//            return getNextByRound();
//        }
//        return "";
//    }


    private String getNextByRound(){
        int sipsCount = sips.size();//count 0
        if (sipsCount == 0){    // sip index 0
            return null;
        }

        int nextInt = lastSipIndex.incrementAndGet();
        if (nextInt >= sipsCount){
            lastSipIndex.set(0);
            nextInt = 0;
        }

        return sips.get(nextInt);
    }


    private void getNextByQueue(){
//        try{
//            return sips.get(lastSipIndex.)
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }
}
