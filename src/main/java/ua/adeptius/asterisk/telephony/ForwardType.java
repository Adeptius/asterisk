package ua.adeptius.asterisk.telephony;



public enum ForwardType {
    TO_ALL("Всем сразу"),
    QUEUE("По очереди");

    public String name;

    ForwardType(String name) {
        this.name = name;
    }
}
