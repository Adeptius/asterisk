package ua.adeptius.asterisk.model.telephony;



public enum ForwardType {
    TO_ALL("Всем сразу"),
    QUEUE("По очереди"),
    RANDOM("Случайно");

    public String name;

    ForwardType(String name) {
        this.name = name;
    }
}
