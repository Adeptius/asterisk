package ua.adeptius.asterisk.senders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

public class ErrorsMailSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(ErrorsMailSender.class.getSimpleName());

    private static LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();

    public ErrorsMailSender() {
        setName("ErrorEmailSender");
        setDaemon(true);
        start();
    }

    public static void send(String message) {
        try {
            blockingQueue.put(message);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }

    public static void send(String message, Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = message + "\n\n" + sw.toString();
            blockingQueue.put(exceptionAsString);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                sendMessage(blockingQueue.take());
            } catch (Exception e) {
                e.printStackTrace();
//            Этого никогда не произойдёт
            }
        }
    }


    private void sendMessage(String body) throws MessagingException {
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("your.cloud.monitor", "357Monitor159");
            }
        };
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        properties.put("mail.smtp.port", "587"); //TLS Port
        properties.put("mail.smtp.auth", "true"); //enable authentication
        properties.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        Session session = Session.getInstance(properties, auth);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("your.cloud.monitor@gmail.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("adeptius@gmail.com"));
        message.setSubject("Ошибка на сервере");

        message.setText(body);
        Transport.send(message);
    }
}