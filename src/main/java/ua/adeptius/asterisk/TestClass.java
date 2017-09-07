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


    }
}