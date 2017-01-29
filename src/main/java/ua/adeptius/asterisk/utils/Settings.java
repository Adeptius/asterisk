package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.model.LogCategory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
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

    public static boolean checkCategoryLogging(LogCategory category) {

        return true;
    }

    public static String getSetting(String name){
        return map.get(name);
    }
}
