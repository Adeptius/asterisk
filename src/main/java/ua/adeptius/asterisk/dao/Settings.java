package ua.adeptius.asterisk.dao;


import ua.adeptius.asterisk.Main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Settings {

    private static HashMap<String, String> map = new HashMap<>();

    public static void load(Class clazz){
        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            InputStream inputStream = clazz.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
                initSettings(prop);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initSettings(Properties prop) {
        for (final String name: prop.stringPropertyNames())
            map.put(name, prop.getProperty(name));
    }

    public static boolean getSettingBoolean(String category) {
        return Boolean.parseBoolean(map.get(category.toString()));
    }

    public static void setSetting(String key, String value){
        map.put(key, value);
        Properties prop = new Properties();
        try {
            String filename = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"config.properties";
            OutputStream output = new FileOutputStream(filename);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                prop.setProperty(entry.getKey(), entry.getValue());
            }
            prop.store(output, null);
            System.out.println(key + " " + value);
        } catch (Exception io) {
            io.printStackTrace();
        }
    }

    public static String getSetting(String name){
        return map.get(name);
    }
}
