package ua.adeptius.asterisk.senders;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.Email;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.*;

import static ua.adeptius.asterisk.model.Email.EmailType.NO_OUTER_PHONES_LEFT;


public class EmailSender extends Thread {

    private static Logger LOGGER = LoggerFactory.getLogger(EmailSender.class.getSimpleName());
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            1, 10, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(15), new ThreadFactoryBuilder().setNameFormat("EmailSender-Pool-%d").build());

    private LinkedBlockingQueue<Email> blockingQueue = new LinkedBlockingQueue<>();
    private String serverAddress;

    public EmailSender() {
        setName("EmailSender-Manager");
        setDaemon(true);
        serverAddress = Main.settings.getServerAddress();
        start();
    }

    public void send(Email email) {
        try {
            blockingQueue.put(email);
        } catch (InterruptedException ignored) {
//            Этого никогда не произойдёт
        }
    }

    private HashMap<String, String> htmlBodies = new HashMap<>();

    private String readResourceAsString(String filename)throws IOException {
        String stringToReturn = htmlBodies.get(filename);
        if (stringToReturn == null) {
            LOGGER.trace("Читаем {} из ресурсов", filename);
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder builder = new StringBuilder(130);
            while (reader.ready()) {
                builder.append(reader.readLine());
            }
            htmlBodies.put(filename, builder.toString());
            stringToReturn = htmlBodies.get(filename);
        }
        return stringToReturn;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Email email = blockingQueue.take();
                EXECUTOR.submit(() -> detectTypeAndSend(email));
            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
//            Этого никогда не произойдёт
            }
        }
    }


    private void detectTypeAndSend(Email email) {
        Email.EmailType emailType = email.getEmailType();
        if (emailType == Email.EmailType.REGISTRATION) {
            sendRegistrationMail(email);
        } else if (emailType == Email.EmailType.RECOVER) {
            sendRecoverMail(email);
        }else if (emailType == NO_OUTER_PHONES_LEFT){
            sendNoPhonesLeft(email);
        }
    }

    private void sendNoPhonesLeft(Email email) {
        final String toEmail = email.getEmail();
        String login = email.getUserLogin();
        String siteName = email.getSiteName();
        LOGGER.info("{}: Отправка письма \"закончились номера\" на {}", login, toEmail);

        try {
            String basicHtmlNoPhonesBody = readResourceAsString("noPhonesMail.html")
                    .replace("SITENAME", siteName);

            sendMessage(toEmail, "Закончились свободные номера", basicHtmlNoPhonesBody);
            LOGGER.info("{}: Отправка письма \"закончились номера\" успешна", login);
        } catch (Exception ex) {
            LOGGER.error(login + ": Ошибка отправки письма \"закончились номера\"", ex);
        }
    }

    private void sendRecoverMail(Email email) {
        final String toEmail = email.getEmail();
        String recoverUrl = "https://" + serverAddress + "/tracking/recover.html?key=" + email.getHash();
        String login = email.getUserLogin();
        LOGGER.info("{}: Отправка письма восстановления пароля на {}", login, toEmail);

        try {
            String basicHtmlRecoverBody = readResourceAsString("recoverMail.html")
                    .replace("USERNAME", login)
                    .replace("RECOVER_URL", recoverUrl);

            sendMessage(toEmail, "Восстановление пароля", basicHtmlRecoverBody);
            LOGGER.info("{}: Отправка письма о восстановлении пароля успешна", login);
        } catch (Exception ex) {
            LOGGER.error(login + ": Ошибка отправки письма восстановления пароля", ex);
        }
    }


    private void sendRegistrationMail(Email email) {
        final String toEmail = email.getEmail(); // can be any email id
        String registrationUrl = "https://" + serverAddress + "/tracking/registerresult.html?key=" + email.getHash();
        String login = email.getUserLogin();
        LOGGER.info("{}: Отправка регистрационного письма на {}", login, toEmail);

        try {
            String registrationHtmlBody = readResourceAsString("registrationMail.html")
                    .replace("USERNAME", login)
                    .replace("REGISTRATION_URL", registrationUrl);

            sendMessage(toEmail, "Подтверждение регистрации", registrationHtmlBody);
            LOGGER.info("{}: Отправка регистрационного письма успешна", login);
        } catch (Exception ex) {
            LOGGER.error(login + ": Ошибка отправки регистрационного письма", ex);
        }
    }


    private void sendMessage(String toEmail, String subject, String body) throws MessagingException {
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("do_not_reply@nextel.com.ua", "7aEpg01Aa8UF");
            }
        };
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "mail.ukraine.com.ua"); //SMTP Host
        properties.put("mail.smtp.port", "25"); //TLS Port
        properties.put("mail.smtp.auth", "true"); //enable authentication
        properties.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        Session session = Session.getInstance(properties, auth);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("do_not_reply@nextel.com.ua"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject);
        Multipart multipart = new MimeMultipart("alternative");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body, "text/html; charset=UTF-8");
        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);

        Transport.send(message);
    }
}
