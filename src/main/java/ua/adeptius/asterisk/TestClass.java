package ua.adeptius.asterisk;


import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SuppressWarnings("Duplicates")
@Component
@EnableWebMvc
public class TestClass {

//    private static Logger LOGGER = LoggerFactory.getLogger(EmailSender.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        TestClass testClass = new TestClass();
        testClass.test();
    }

    private void test() throws Exception {
//        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");

    }
}