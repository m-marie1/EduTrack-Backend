package com.college.attendance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * Mock email configuration for development environment.
 * Instead of sending real emails, this configuration logs email content.
 */
@Configuration
@Profile("mock-email")
public class MockEmailConfig {

    private static final Logger log = LoggerFactory.getLogger(MockEmailConfig.class);

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new MockJavaMailSender();
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "true");
        
        log.info("Configured mock email sender - emails will be logged but not sent");
        
        return mailSender;
    }
    
    /**
     * Mock implementation of JavaMailSender that logs emails instead of sending them
     */
    public static class MockJavaMailSender extends JavaMailSenderImpl {
        @Override
        public void send(SimpleMailMessage simpleMessage) {
            log.info("Mock email sent: To: {}, Subject: {}, Text: {}", 
                    simpleMessage.getTo(), 
                    simpleMessage.getSubject(), 
                    simpleMessage.getText());
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            for (SimpleMailMessage message : simpleMessages) {
                send(message);
            }
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) {
            log.info("Mock mime message sent via preparator");
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) {
            for (MimeMessagePreparator preparator : mimeMessagePreparators) {
                send(preparator);
            }
        }

        @Override
        public void send(MimeMessage mimeMessage) {
            log.info("Mock mime message sent");
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
            for (MimeMessage message : mimeMessages) {
                send(message);
            }
        }
    }
} 