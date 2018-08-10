package cz.cvut.kbss.study.service;

import cz.cvut.kbss.study.exception.ValidationException;
import cz.cvut.kbss.study.util.ConfigParam;
import static cz.cvut.kbss.study.util.ConfigParam.E_BCC_ADDRESS_LIST;
import static cz.cvut.kbss.study.util.ConfigParam.E_CC_ADDRESS_LIST;
import static cz.cvut.kbss.study.util.ConfigParam.E_DISPLAY_NAME;
import static cz.cvut.kbss.study.util.ConfigParam.E_FROM_ADDRESS;
import static cz.cvut.kbss.study.util.ConfigParam.E_REPLY_TO_ADDRESS_LIST;
import static cz.cvut.kbss.study.util.ConfigParam.SMTP_HOST;
import static cz.cvut.kbss.study.util.ConfigParam.SMTP_PASSWORD;
import static cz.cvut.kbss.study.util.ConfigParam.SMTP_PORT;
import static cz.cvut.kbss.study.util.ConfigParam.SMTP_USER;
import cz.cvut.kbss.study.util.etemplates.BaseEmailTemplate;
import cz.cvut.kbss.study.util.etemplates.UserInvite;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    protected static final Logger LOG = LoggerFactory.getLogger(EmailService.class);
    @Autowired
    private ConfigReader config;

    private Session getSession() {
        // Get system properties
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", getValue(SMTP_HOST));
        props.put("mail.smtp.user", getValue(SMTP_USER));
        props.put("mail.smtp.password", getValue(SMTP_PASSWORD));
        props.put("mail.smtp.port", getValue(SMTP_PORT));
        props.put("mail.smtp.auth", "true");
        // Get the default Session object.
        return Session.getDefaultInstance(props);
    }

    private InternetAddress[] parseAddressList(ConfigParam param) throws AddressException {
        return InternetAddress.parse(getValue(param));
    }

    private InternetAddress parseAddress(ConfigParam param) throws AddressException {
        InternetAddress[] adressList = parseAddressList(param);
        if (adressList.length == 0) {
            throw new IllegalArgumentException("Email address not specified for configuration parameter " + param + ".");
        } else if (adressList.length > 1) {
            throw new IllegalArgumentException("Multiple addresses not allowed for configuration parameter" + param + ", while current value is '" + config.getConfig(param) + "'.");
        }
        return adressList[0];
    }

    private String getValue(ConfigParam param) {
        return config.getConfig(param);
    }

    /**
     * Sends email according to an email template.
     *
     * @param emailTemplate Email template.
     * @param toEmailAddressList Comma-separated list of recipients w.r.t. TO: header.
     * @param ccEmailAddressList Comma-separated list of recepients w.r.t. CC: header.
     */
    public void sendEmail(BaseEmailTemplate emailTemplate, String toEmailAddressList, String ccEmailAddressList, boolean sendCCandBCC) {
        Session session = getSession();
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            final InternetAddress fromAddress;
            if (getValue(E_FROM_ADDRESS).equals("")) {
                fromAddress = parseAddress(SMTP_USER);
            } else {
                fromAddress = parseAddress(E_FROM_ADDRESS);
            }
            fromAddress.setPersonal(getValue(E_DISPLAY_NAME));
            message.setFrom(fromAddress);

            // Set To: header field of the header.
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmailAddressList));

            // Set CC: header field of the header.
            if (ccEmailAddressList != null && !ccEmailAddressList.equals(toEmailAddressList)) {
                message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmailAddressList));
            }
            if (sendCCandBCC) {
                message.addRecipients(Message.RecipientType.CC, parseAddressList(E_CC_ADDRESS_LIST));
                message.addRecipients(Message.RecipientType.BCC, parseAddressList(E_BCC_ADDRESS_LIST));
            }
            message.setReplyTo(parseAddressList(E_REPLY_TO_ADDRESS_LIST));


            // Set Subject: header field
            message.setSubject(emailTemplate.getSubject());

            // Send the actual HTML message, as big as you like
            message.setContent(emailTemplate.getHTMLContent(), "text/html; charset=UTF-8");

            // Send message
            Transport transport = session.getTransport("smtp");
            transport.connect(getValue(SMTP_HOST), getValue(SMTP_USER), getValue(SMTP_PASSWORD));
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            if (LOG.isTraceEnabled()) {
                LOG.trace("{} email was sent to {}.", emailTemplate.getClass().getSimpleName(), toEmailAddressList);
            }
        } catch (AddressException ae) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Unable to send email.", ae);
            }
            throw new ValidationException("Unable to send email", ae);
        } catch (MessagingException | UnsupportedEncodingException mex) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Unable to send email.", mex);
            }
            throw new IllegalArgumentException("Unable to send email.", mex);
        }
    }
}