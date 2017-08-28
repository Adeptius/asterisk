package ua.adeptius.asterisk;



import org.asteriskjava.Cli;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.test.AsteriskQueqe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class TestClass {


    public static void main(String[] args) throws Exception {
        TestClass testClass = new TestClass();
        testClass.test();
    }
    private void test() throws Exception{
        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");
        Settings.load(this.getClass());

        new Cli().parseOptions(new String[]{});
//        MySqlStatisticDao.init();
//        MySqlStatisticDao.getListOfTables().forEach(System.out::println);
//        MySqlStatisticDao.getStatisticOfRange()


//        List<String> sips = new ArrayList<>();
//        sips.add("1111");
//        sips.add("2222");
//        sips.add("3333");
//
//        AsteriskQueqe queqe = new AsteriskQueqe();
//        queqe.setSips(sips);
//        queqe.setForwardType(ROUND);
//
//        for (int i = 0; i < 10; i++) {
//            System.out.println(queqe.getNextOperator());
//        }


//        Random random = new Random();
//        HashMap<Integer, Integer> map = new HashMap<>();
//        for (int i = 0; i < 10000; i++) {
//            int randomInt = 1+random.nextInt(5);
//            Integer integer = map.computeIfAbsent(randomInt, k -> 0);
//            integer++;
//            map.put(randomInt, integer);
//        }
//        map.forEach((integer, integer2) -> System.out.println(integer + " = " + integer2));


    }

}