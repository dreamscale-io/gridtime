package com.dreamscale.gridtime.core.capability.integration;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.status.Status;
import com.sendgrid.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class EmailCapability {

    @Value( "${sendgrid.mail.from-name}" )
    private String fromName;

    @Value( "${gridtime.sitelink.url}" )
    private String sitelinkUrl;

    @Value( "${sendgrid.mail.from-address}" )
    private String fromAddress;

    @Value( "${sendgrid.mail.api-key}" )
    private String apiKey;


    //account/profile/property/email/validate
    private static final String EMAIL_VALIDATE_API =
            ResourcePaths.ACCOUNT_PATH +
            ResourcePaths.PROFILE_PATH +
            ResourcePaths.PROPERTY_PATH +
            ResourcePaths.EMAIL_PATH +
            ResourcePaths.VALIDATE_PATH;


    public SimpleStatusDto sendDownloadAndActivationEmail(String toEmailAddress, String activationToken) {

        Mail mail = new Mail();
        mail.setFrom(new Email(fromAddress, fromName));
        mail.setSubject("Activate Your Account");
        mail.setReplyTo(new Email(fromAddress, fromName));

        Personalization personalization = new Personalization();
        Email to = new Email();
        to.setEmail(toEmailAddress);
        personalization.addTo(to);

        mail.addPersonalization(personalization);

        Content content = new Content();
        content.setType("text/html");
        content.setValue("<html><body> <p> Use this Token to Activate Your Account:</p><p>"+activationToken+"</p></body></html>");
        mail.addContent(content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        SimpleStatusDto responseDto = null;
        try {
            request.method = Method.POST;
            request.endpoint = "mail/send";
            request.body = mail.build();
            Response response = sg.api(request);

            if (response.statusCode == 200) {
                responseDto = new SimpleStatusDto(Status.VALID, "Email sent successfully.");
            } else {
                responseDto = new SimpleStatusDto(Status.FAILED, "Unable to send email to "+toEmailAddress +":" + response.body);
            }
        } catch (IOException ex) {
            responseDto = new SimpleStatusDto(Status.FAILED, ex.getMessage());

            log.error("Unable to send email to "+toEmailAddress, ex);
        }

        return responseDto;
    }

    public SimpleStatusDto sendEmailValidationEmail(String toEmailAddress, String ticketCode) {

        Mail mail = new Mail();
        mail.setFrom(new Email(fromAddress, fromName));
        mail.setSubject("Validate your email address");
        mail.setReplyTo(new Email(fromAddress, fromName));

        Personalization personalization = new Personalization();
        Email to = new Email();
        to.setEmail(toEmailAddress);
        personalization.addTo(to);

        mail.addPersonalization(personalization);

        Content content = new Content();
        content.setType("text/html");
        content.setValue("<html><body> <p> Click this link to validate your email address:</p>" +
                "<p><a href='"+sitelinkUrl+ EMAIL_VALIDATE_API + "?validationCode="+ticketCode+"'>Validate Email</a></p></body></html>");
        mail.addContent(content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        SimpleStatusDto responseDto = null;
        try {
            request.method = Method.POST;
            request.endpoint = "mail/send";
            request.body = mail.build();
            Response response = sg.api(request);

            if (response.statusCode == 200) {
                responseDto = new SimpleStatusDto(Status.VALID, "Email sent successfully.");
            } else {
                responseDto = new SimpleStatusDto(Status.FAILED, "Unable to send email to "+toEmailAddress +":" + response.body);
            }
        } catch (IOException ex) {
            responseDto = new SimpleStatusDto(Status.FAILED, ex.getMessage());

            log.error("Unable to send email to "+toEmailAddress, ex);
        }

        return responseDto;

    }


    public SimpleStatusDto sendAccountResetEmail(String toEmailAddress, String activationToken) {

        Mail mail = new Mail();
        mail.setFrom(new Email(fromAddress, fromName));
        mail.setSubject("Reset Your Account");
        mail.setReplyTo(new Email(fromAddress, fromName));

        Personalization personalization = new Personalization();
        Email to = new Email();
        to.setEmail(toEmailAddress);
        personalization.addTo(to);

        mail.addPersonalization(personalization);

        Content content = new Content();
        content.setType("text/html");
        content.setValue("<html><body> <p> Use this Token to Re-Activate Your Account:</p><p>"+activationToken+"</p></body></html>");
        mail.addContent(content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        SimpleStatusDto responseDto = null;
        try {
            request.method = Method.POST;
            request.endpoint = "mail/send";
            request.body = mail.build();
            Response response = sg.api(request);

            if (response.statusCode == 200) {
                responseDto = new SimpleStatusDto(Status.VALID, "Email sent successfully.");
            } else {
                responseDto = new SimpleStatusDto(Status.FAILED, "Unable to send email to "+toEmailAddress +":" + response.body);
            }
        } catch (IOException ex) {
            responseDto = new SimpleStatusDto(Status.FAILED, ex.getMessage());

            log.error("Unable to send email to "+toEmailAddress, ex);
        }

        return responseDto;
    }


}
