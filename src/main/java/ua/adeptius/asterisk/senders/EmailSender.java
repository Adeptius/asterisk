package ua.adeptius.asterisk.senders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.Settings;
import ua.adeptius.asterisk.model.Email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;


public class EmailSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(EmailSender.class.getSimpleName());
    private LinkedBlockingQueue<Email> blockingQueue = new LinkedBlockingQueue<>();
    private final String username = "your.cloud.monitor";
    private final String password = "357Monitor159";
    private String basicHtmlRegistrationBody;
    private String serverAddress;
    private Properties properties;

    public EmailSender() {
        setName("EmailSender");
        setDaemon(true);
        serverAddress = Main.getOptions().getServerAddress();
        start();
    }

    public void send(Email email) {
        try {
            blockingQueue.put(email);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }

    private String getBasicHtmlRegistrationBody() throws IOException {
        if (basicHtmlRegistrationBody == null) {
            LOGGER.info("Читаем basicHtmlRegistrationBody из ресурсов");
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("registrationMail.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder builder = new StringBuilder(4300);
            while (reader.ready()) {
                builder.append(reader.readLine());
            }
            basicHtmlRegistrationBody = builder.toString();
        }
        return basicHtmlRegistrationBody;
    }

    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
            properties.put("mail.smtp.port", "587"); //TLS Port
            properties.put("mail.smtp.auth", "true"); //enable authentication
            properties.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS
        }
        return properties;
    }

    private MimeMessage getMimeMessage(String toEmail, String subject) throws MessagingException {
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
        Session session =  Session.getInstance(getProperties(), auth);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject);
        return message;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Email email = blockingQueue.take();
                Email.EmailType emailType = email.getEmailType();
                if (emailType == Email.EmailType.REGISTRATION){
                    sendRegistrationMail(email);
                }


            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
//            Этого никогда не произойдёт
            }
        }
    }



    private void sendRegistrationMail(Email email) {
        final String toEmail = email.getEmail(); // can be any email id
        String registrationUrl = "https://" + serverAddress + "/tracking/registerresult.html?key=" + email.getKey();
        String pendingUserLogin = email.getUserLogin();

        LOGGER.debug("{}: Отправка регистрационного письма на {}", pendingUserLogin, toEmail);

        try {
            String registrationHtmlBody = getBasicHtmlRegistrationBody()
                    .replaceAll("USERNAME", pendingUserLogin)
                    .replaceAll("REGISTRATION_URL", registrationUrl);

            MimeMessage message = getMimeMessage(toEmail, "Подтверждение регистрации");
            Multipart multipart = new MimeMultipart("alternative");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(registrationHtmlBody, "text/html; charset=UTF-8");

            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);

            Transport.send(message);
            LOGGER.debug("{}: Отправка регистрационного письма успешна", pendingUserLogin);
        } catch (Exception ex) {
            LOGGER.error(pendingUserLogin + ": Ошибка отправки регистрационного письма", ex);
        }
    }
}
