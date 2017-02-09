package ua.adeptius.asterisk.utils;


import ua.adeptius.asterisk.model.Site;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static ua.adeptius.asterisk.model.LogCategory.*;

public class Mail {

    public void checkTimeAndSendEmail(Site site, String message){
        long lastEmail = site.getLastEmailTime();
        long currentTime = new GregorianCalendar().getTimeInMillis();
        long pastTime = currentTime - lastEmail;
        int pastMinutes = (int) (pastTime / 1000 / 60);
        int antispam = Integer.parseInt(Settings.getSetting("MAIL_ANTISPAM"));
        if (pastMinutes < antispam){
            MyLogger.log(ELSE, "Последнее оповещение было отправлено недавно");
        }else {
            send(site.getMail(), message);
            site.setLastEmailTime(new GregorianCalendar().getTimeInMillis());
        }
    }

    private void send(String to, String message) {
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
            if (Settings.getSettingBoolean(MAIL_SENDING_LOG.toString())){
            props.put("mail.debug", "true");
            }
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
            } catch (MessagingException mex) {
                mex.printStackTrace();
                MyLogger.log(MAIL_SENDING_ERRORS, "Ошибка при отправке письма" + mex);
            }
        }).start();
    }
}
