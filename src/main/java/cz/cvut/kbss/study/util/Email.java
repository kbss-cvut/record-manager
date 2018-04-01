package cz.cvut.kbss.study.util;

import cz.cvut.kbss.study.service.repository.BaseRepositoryService;
import cz.cvut.kbss.study.util.etemplates.BaseEmailTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class Email {
    public Email(BaseEmailTemplate emailTemplate, String recipientEmail) {
        this.emailTemplate = emailTemplate;
        this.recipientEmail = recipientEmail;
    }

    private BaseEmailTemplate emailTemplate;
    private String recipientEmail;

    private static final String HOST = "smtp.gmail.com";
    private static final String USER_NAME = "studymanagercvut";
    private static final String DISPLAY_NAME = "StudyManager";
    private static final String PASSWORD = "Klinika321";
    protected static final Logger LOG = LoggerFactory.getLogger(BaseRepositoryService.class);

    private Session getSession() {
        // Get system properties
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.user", USER_NAME);
        props.put("mail.smtp.password", PASSWORD);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        // Get the default Session object.
        return Session.getDefaultInstance(props);
    }

    public void sendEmail() {
        Session session = getSession();
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(USER_NAME, DISPLAY_NAME));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));

            // Set Subject: header field
            message.setSubject(emailTemplate.getSubject());

            // Send the actual HTML message, as big as you like
            message.setContent(emailTemplate.getHTMLContent(), "text/html");

            // Send message
            Transport transport = session.getTransport("smtp");
            transport.connect(HOST, USER_NAME, PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Email sent successfully");
            }
        } catch (AddressException ae) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Unable to send email.", ae);
            }
        } catch (MessagingException mex) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Unable to send email.", mex);
            }
        } catch (UnsupportedEncodingException e) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Unable to send email.", e);
            }
        }
    }
}