package com.springJava.jobTracker.repo;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
//import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
//import java.io.InputStream;
import java.util.*;


@Service
public class SpringEmailService {

    /**
     * Sends an email message with no attachments.
     *
     * @param from       email address from which the message will be sent.
     * @param recipients the recipients of the message.
     * @param subject    subject header field.
     * @param text       content of the message.
     * @throws MessagingException
     * @throws IOException
     */
    //public void send(String from, String recipients, String subject, String text) throws MessagingException, IOException {
      //  send(from, recipients, subject, text);
    //}
    
    public static void send(String from, String recipients, String subject, String text)
      //      List<InputStream> attachments, List<String> fileNames, List<String> mimeTypes)
            			throws MessagingException, IOException {

		// check for null references
		Objects.requireNonNull(from);
		Objects.requireNonNull(recipients);
		
		// load email configuration from properties file
		Properties properties = new Properties();
		properties.load(SpringEmailService.class.getResourceAsStream("/mail.properties"));
		String username = properties.getProperty("mail.smtp.username");
		String password = properties.getProperty("mail.smtp.password");
		
		// configure the connection to the SMTP server
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setJavaMailProperties(properties);
		mailSender.setUsername(username);
		mailSender.setPassword(password);
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(from);
		helper.setSubject(subject);
		helper.setText(text, true);
		
		//for (String recipient : recipients) {
		helper.addTo(recipients);
		//}
		
		/*if (attachments != null) {
		for (int i = 0; i < attachments.size(); i++) {
		// create a data source to wrap the attachment and its mime type
		ByteArrayDataSource dataSource = new ByteArrayDataSource(attachments.get(i), mimeTypes.get(i));
		
		// add the attachment
		helper.addAttachment(fileNames.get(i), dataSource);
		}
		}*/
		
		mailSender.send(message);
		}
}