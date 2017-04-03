package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.model.CustomerType;

public class CustomerGroup {

    public String name;
    public CustomerType type;

    public CustomerGroup(String name, CustomerType type) {
        this.name = name;
        this.type = type;
    }
}
