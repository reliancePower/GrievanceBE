package com.reliance.grievance.helper;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Component
public class MailHelper {

    private final JavaMailSender mailSender;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MailHelper.class);

    public MailHelper(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendMail(String email, String firstName, MultipartFile pdfFile, String fromEmail, String subject, String text) {
        try {
            log.info("MailHelper :: sendMail() ");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text,true);

            if(pdfFile!=null)
                helper.addAttachment(Objects.requireNonNull(pdfFile.getOriginalFilename()), new ByteArrayResource(pdfFile.getBytes()));

            mailSender.send(message);
            return true; // Message accepted by server
        } catch (MailException | IOException | MessagingException e) {
            //logger.error("Failed to send email to " + email, e);
            return false;
        }
    }

    public boolean sendMailCc(String email, String firstName, MultipartFile pdfFile, String fromEmail, String subject, String text , String ccEmail) {
        try {
            log.info("MailHelper :: sendMailCc() ");
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);

            if (ccEmail != null && !ccEmail.isEmpty()) {
                helper.setCc(ccEmail);
            }

            helper.setSubject(subject);
            helper.setText(text,true);

            if(pdfFile!=null)
                helper.addAttachment(Objects.requireNonNull(pdfFile.getOriginalFilename()), new ByteArrayResource(pdfFile.getBytes()));

            mailSender.send(message);
            return true; // Message accepted by server
        } catch (MailException | IOException | MessagingException e) {
            //logger.error("Failed to send email to " + email, e);
            return false;
        }
    }
}


