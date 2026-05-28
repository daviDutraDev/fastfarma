package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String email = "fastfarmasistema@gmail.com";

    private final String senha = "mpnvxvbsbvvukzjb";

    public boolean enviarEmail(
            String destinatario,
            String assunto,
            String mensagem
    ) {

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(
                props,
                new Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                email,
                                senha
                        );
                    }
                }
        );

        try {

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(email));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(destinatario)
            );

            message.setSubject(assunto);

            message.setText(mensagem);

            Transport.send(message);

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Erro ao enviar email: "
                            + e.getMessage()
            );

            return false;
        }
    }
}