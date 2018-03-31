package cz.cvut.kbss.study.util;

import cz.cvut.kbss.study.service.repository.BaseRepositoryService;
import cz.cvut.kbss.study.util.etemplates.BaseEmailTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class Email {
    public Email(BaseEmailTemplate emailTemplate, String senderEmail, String recipientEmail) {
        this.emailTemplate = emailTemplate;
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
    }

    private BaseEmailTemplate emailTemplate;
    private String senderEmail;
    private String recipientEmail;
    private static final String HOST = "localhost";

    protected static final Logger LOG = LoggerFactory.getLogger(BaseRepositoryService.class);

    private Session getSession() {
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", HOST);
        // Get the default Session object.
        return Session.getDefaultInstance(properties);
    }

    public void sendEmail() {
        Session session = getSession();
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(senderEmail));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));

            // Set Subject: header field
            message.setSubject(emailTemplate.getSubject());

            // Send the actual HTML message, as big as you like
            message.setContent(emailTemplate.getHTMLContent(), "text/html");

            // Send message
            Transport.send(message);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Email sent successfully");
            }
        } catch (MessagingException mex) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Unable to send email.", mex);
            }
        }
    }
}