package ua.adeptius.asterisk;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class TestClass {

    public static void main(String[] args) throws Exception {
        TestClass testClass = new TestClass();
        testClass.test();
    }

    private void test() throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");


//        String s = "[2001036, 2001037]";
//        s = s.substring(1, s.indexOf(","));
//        System.out.println(s);


    }

}