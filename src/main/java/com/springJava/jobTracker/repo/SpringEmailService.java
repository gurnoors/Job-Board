// Reference: Spring EmailService Tutorial
package com.springJava.jobTracker.repo;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;


@Service
public class SpringEmailService {

  public static void send(String from, String recipients, String subject, String text)
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

		helper.addTo(recipients);

		mailSender.send(message);
		}
}
