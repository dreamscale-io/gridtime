package com.dreamscale.gridtime.core.capability.integration;

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

    @Value( "${sendgrid.mail.from-address}" )
    private String fromAddress;

    @Value( "${sendgrid.mail.api-key}" )
    private String apiKey;


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

            responseDto = new SimpleStatusDto(Status.VALID, "Email sent successfully.");

            System.out.println(response.statusCode);
            System.out.println(response.body);
            System.out.println(response.headers);
        } catch (IOException ex) {
            responseDto = new SimpleStatusDto(Status.FAILED, ex.getMessage());

            log.error("Unable to send email to "+toEmailAddress, ex);
        }

        return responseDto;
    }


}
