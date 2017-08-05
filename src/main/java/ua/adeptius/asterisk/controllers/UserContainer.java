package ua.adeptius.asterisk.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.Telephony;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.webcontrollers.AdminController;

import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public class UserContainer {

    private static Logger LOGGER =  LoggerFactory.getLogger(UserContainer.class.getSimpleName());


    private static List<User> users = new ArrayList<>();
    private static HashMap<String, User> hashes = new HashMap<>();

    public static void recalculateHashesForAllUsers(){
        LOGGER.debug("Пересчет хэша для всех пользователей");
        hashes.clear();
        for (User user : users) {
            hashes.put(createMd5(user), user);
        }
    }

    public static User getUserByHash(String hash){
        return hashes.get(hash);
    }

    public static List<User> getUsers() {
        return users;
    }

    public static void removeUser(User user) {
        LOGGER.debug("Удаление пользователя {}",user);
        getUsers().remove(user);
        hashes.remove(getHashOfUser(user));
    }

    public static HashMap<String, User> getHashes() {
        return hashes;
    }

    public static void setUsers(List<User> users) {
        UserContainer.users = users;
        hashes.clear();
        for (User user : users) {
            hashes.put(createMd5(user), user);
        }
    }

    public static void putUser(User user){
        LOGGER.debug("Добавление пользователя {}", user);
        users.add(user);
        hashes.put(createMd5(user), user);
    }

    // todo сделать кеширование через мапу
    public static User getUserByName(String name){
        try {
            return users.stream().filter(user -> user.getLogin().equals(name)).findFirst().get();
        }catch (NoSuchElementException e){
            return null;
        }
    }

//    public static List<Tracking> getAllSites(){
//        return users.stream().filter(user -> user.getTracking() != null).map(User::getTracking).collect(Collectors.toList());
//    }

//    public static Tracking getSiteByName(String name) throws NoSuchElementException {
//        return getUserByName(name).getTracking();
//    }

    public static String getHashOfUser(User user){
        for (Map.Entry<String, User> entry : hashes.entrySet()) {
            if (entry.getValue().equals(user)){
                return entry.getKey();
            }
        }
        return null;
    }


    public static String createMd5(User user){
        return createMd5(user.getLogin()+user.getPassword());
    }

    public static String createMd5(String st) {
        try{MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(st.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        }catch (Exception e){}
        return null;
    }
}
