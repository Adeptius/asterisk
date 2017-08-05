package ua.adeptius.asterisk.senders;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.dao.Settings;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Mail {

    private static Logger LOGGER =  LoggerFactory.getLogger(Mail.class.getSimpleName());

//    public void checkTimeAndSendEmail(Tracking tracking, String message){
//        long lastEmail = tracking.getLastEmailTime();
//        long currentTime = new GregorianCalendar().getTimeInMillis();
//        long pastTime = currentTime - lastEmail;
//        int pastMinutes = (int) (pastTime / 1000 / 60);
//        int antispam = Integer.parseInt(Settings.getSetting("MAIL_ANTISPAM"));
//        String login = tracking.getLogin();
//        if (pastMinutes < antispam){
//            LOGGER.trace("{}: последнее письмо было отправлено недавно ({} минут назад)", login, pastMinutes);
//        }else {
//            LOGGER.debug("{}: отправляем письмо - нет свободных номеров", login);
//            send(tracking.getUser().getEmail(), message, login);
//            tracking.setLastEmailTime(new GregorianCalendar().getTimeInMillis());
//        }
//    }

    private void send(String to, String message, String login) {
        new Thread(() -> {
            String from = "call-tracking.pro@mail.ru";
            String host = "smtp.mail.ru";
            int port = 465;
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
//            props.put("mail.debug", "true");
            Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                // Указываем логин пароль, от почты, с которой будем отправлять сообщение.
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("call-tracking.pro", "A45934593");
                }
            });

            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(from));
                InternetAddress[] address = {new InternetAddress(to)};
                msg.setRecipients(Message.RecipientType.TO, address);
                msg.setSubject("Закончились свободные номера");
                msg.setSentDate(new Date());
                msg.setText(message);
                Transport.send(msg);
                LOGGER.debug("{}: письмо отправлено.", login);
            } catch (MessagingException e) {
                LOGGER.error(login+": ошибка отправки письма.", e);
            }
        }).start();
    }
}
