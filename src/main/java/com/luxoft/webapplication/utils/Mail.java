package com.luxoft.webapplication.utils;


import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {


    public void sendMail(String message) {
        new Thread(() -> {
            String to = "adeptius@gmail.com";
            String from = "server-asterisk@mail.ru";
            String host = "smtp.mail.ru";
            int port = 465;
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            if (Settings.showSendingMailLogs){
            props.put("mail.debug", "true");
            }
            Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
                // Указываем логин пароль, от почты, с которой будем отправлять сообщение.
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("server-asterisk", "dmitryAster4593");
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
                MyLogger.log("Ошибка при отправке письма" + mex, Mail.this.getClass());
            }
        }).start();
    }
}
