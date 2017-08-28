package ua.adeptius.asterisk.test;


import org.asteriskjava.fastagi.*;

public class HelloAgiScript2 extends BaseAgiScript {
    private static int counter = 0;

    public void service(AgiRequest request, AgiChannel channel) throws AgiException {
        try {
            setVariable("myvar", "Hello World!");
            // Answer the channel...
            answer();
            // ...say hello...
            streamFile("welcome");
            //streamFile("tt-monkeys");
            channel.exec("Verbose", "2, Hello world");
            verbose("Hello World", 2);
            System.out.println("call count :" + counter + channel.getChannelStatus());
        } catch (org.asteriskjava.fastagi.AgiHangupException e) {
            System.out.println("the user hanged up!!");
            setVariable("myvar", "the user hanged up!!");
        }
        counter++;
    }
}