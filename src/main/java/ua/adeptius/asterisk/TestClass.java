package ua.adeptius.asterisk;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;

public class TestClass {

    public static void main(String[] args) throws Exception {
        TestClass testClass = new TestClass();
        testClass.test();
    }

    private void test() throws Exception {
//        ApplicationContext context = new AnnotationConfigApplicationContext("ua.adeptius");

//        AudioAttributes audioAttributes = new AudioAttributes();
//        audioAttributes.setChannels(1);
//        audioAttributes.setBitRate(128);
//        audioAttributes.setSamplingRate(8000);
//
//        EncodingAttributes encodingAttributes = new EncodingAttributes();
//        encodingAttributes.setAudioAttributes(audioAttributes);
//        encodingAttributes.setDuration(20f);
//
//        Encoder encoder = new Encoder();
//
//        encoder.encode(
//                new File("D:\\Barns Courtney – Fire.mp3"),
//                new File("D:\\Barns.mp3"),
//                encodingAttributes
//                );


        HttpResponse<String> stringHttpResponse = Unirest.post("http://localhost:8080/tracking/melodies/upload")
                .header("Authorization", "i am registered user")
                .field("file", new File("D:\\YandexDisk\\Мои фото\\Java\\20170812_184811_Richtone(HDR).jpg"))
//                .field("file", new File("D:\\YandexDisk\\YouTube\\BEST OF DASHCAMS - Driving Fails Compilation - Episode #180 HD.mp4"))
                .field("name", "Some greeting")
                .asString();
        System.out.println(stringHttpResponse.getBody());

    }
}