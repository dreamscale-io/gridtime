package com.dreamscale.gridtime.core.capability.external;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.OrganizationDto;
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

    @Value( "${torchie.downloadlink.url}" )
    private String downloadLinkUrl;

    @Value( "${sendgrid.mail.from-address}" )
    private String fromAddress;

    @Value( "${sendgrid.mail.api-key}" )
    private String apiKey;


    //account/profile/root/property/email/validate
    private static final String ACCOUNT_PROFILE_ROOT_EMAIL_VALIDATE_API =
            ResourcePaths.ACCOUNT_PATH +
            ResourcePaths.PROFILE_PATH +
            ResourcePaths.ROOT_PATH +
            ResourcePaths.PROPERTY_PATH +
            ResourcePaths.EMAIL_PATH +
            ResourcePaths.VALIDATE_PATH;

    //account/profile/org/property/email/validate
    private static final String ACCOUNT_PROFILE_ORG_EMAIL_VALIDATE_API =
            ResourcePaths.ACCOUNT_PATH +
                    ResourcePaths.PROFILE_PATH +
                    ResourcePaths.ORG_PATH +
                    ResourcePaths.PROPERTY_PATH +
                    ResourcePaths.EMAIL_PATH +
                    ResourcePaths.VALIDATE_PATH;

    private static final String ORG_JOIN_EMAIL_VALIDATE_API =
            ResourcePaths.ORGANIZATION_PATH +
                    ResourcePaths.JOIN_PATH +
                    ResourcePaths.EMAIL_PATH +
                    ResourcePaths.VALIDATE_PATH;

    public SimpleStatusDto sendDownloadAndActivationEmail(String toEmailAddress, String activationToken) {

        Mail mail = new Mail();
        mail.setFrom(new Email(fromAddress, fromName));
        mail.setSubject("Activate Your Torchie Account");
        mail.setReplyTo(new Email(fromAddress, fromName));

        Personalization personalization = new Personalization();
        Email to = new Email();
        to.setEmail(toEmailAddress);
        personalization.addTo(to);

        mail.addPersonalization(personalization);

        Content content = new Content();
        content.setType("text/html");
        content.setValue("<html><body> <p> To install Torchie Shell, first download the latest release here:</p>" +
                "<p><a href='"+downloadLinkUrl+ "' > Download Torchie Shell</a></p>" +
                "<br/><p>Then use this token to activate Your Torchie account:</p><p>"+activationToken+"</p></body></html>");
        mail.addContent(content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        SimpleStatusDto responseDto = null;
        try {
            request.method = Method.POST;
            request.endpoint = "mail/send";
            request.body = mail.build();
            Response response = sg.api(request);

            if (isEmailSuccessStatus(response.statusCode)) {
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

    public SimpleStatusDto sendDownloadAndInviteToPublicEmail(String fromUser, String toEmailAddress, OrganizationDto publicOrg, String ticketCode) {
        Mail mail = new Mail();
        mail.setFrom(new Email(fromAddress, fromName));
        mail.setSubject("You've been invited to join "+publicOrg.getDomainName() + " in hyperspace.");
        mail.setReplyTo(new Email(fromAddress, fromName));

        Personalization personalization = new Personalization();
        Email to = new Email();
        to.setEmail(toEmailAddress);
        personalization.addTo(to);

        mail.addPersonalization(personalization);

        Content content = new Content();
        content.setType("text/html");
        content.setValue("<html><body> <p>"+fromUser+ " has invited you to join " + publicOrg.getDomainName() + " in hyperspace.</p>" +
                "<p>First, download the Torchie Shell here: " +
                "<p><a href='"+downloadLinkUrl+ "' > Download Torchie Shell</a></p> " +
                "then use the activation code below, to activate your account:</p>" +
                "<p>"+ticketCode + "</p></body></html>");
        mail.addContent(content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        SimpleStatusDto responseDto = null;
        try {
            request.method = Method.POST;
            request.endpoint = "mail/send";
            request.body = mail.build();
            Response response = sg.api(request);

            if (isEmailSuccessStatus(response.statusCode)) {
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

    private boolean isEmailSuccessStatus(int statusCode) {
        return statusCode == 200 || statusCode == 202;
    }


    public SimpleStatusDto sendDownloadActivateAndOrgInviteEmail(String toEmailAddress, OrganizationDto org, String ticketCode) {
        Mail mail = new Mail();
        mail.setFrom(new Email(fromAddress, fromName));
        mail.setSubject("You've been invited to join "+org.getDomainName() + " in hyperspace.");
        mail.setReplyTo(new Email(fromAddress, fromName));

        Personalization personalization = new Personalization();
        Email to = new Email();
        to.setEmail(toEmailAddress);
        personalization.addTo(to);

        mail.addPersonalization(personalization);

        Content content = new Content();
        content.setType("text/html");
        content.setValue("<html><body> <p> You've been invited to join " + org.getDomainName() + " in hyperspace.</p>" +
                        "<p>If you already have an existing Torchie Account, you can join the organization within the app, " +
                "by selecting 'Use Invitation Key' from the help menu, and pasting in : </p>"+
                "<p>" + ticketCode + "</p><br/><p>If you don't have an existing Torchie Account, first download Torchie Shell here: " +
                "<p><a href='"+downloadLinkUrl+ "' > Download Torchie Shell</a></p> " +
                "and use this same token to activate your Torchie Account, and you will be automatically joined to the organization.</p></body></html>");
        mail.addContent(content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        SimpleStatusDto responseDto = null;
        try {
            request.method = Method.POST;
            request.endpoint = "mail/send";
            request.body = mail.build();
            Response response = sg.api(request);

            if (isEmailSuccessStatus(response.statusCode)) {
                responseDto = new SimpleStatusDto(Status.VALID, "Email sent successfully.");
            } else {
                responseDto = new SimpleStatusDto(Status.FAILED, "Unable to send email to "+toEmailAddress +":" + response.statusCode + ":" + response.body);
            }
        } catch (IOException ex) {
            responseDto = new SimpleStatusDto(Status.FAILED, ex.getMessage());

            log.error("Unable to send email to "+toEmailAddress, ex);
        }

        return responseDto;
    }

    public SimpleStatusDto sendEmailToValidateRootAccountProfileAddress(String toEmailAddress, String ticketCode) {
        return sendEmailValidationEmail(toEmailAddress, ticketCode, ACCOUNT_PROFILE_ROOT_EMAIL_VALIDATE_API);
    }

    public SimpleStatusDto sendEmailToValidateOrgAccountProfileAddress(String toEmailAddress, String ticketCode) {
        return sendEmailValidationEmail(toEmailAddress, ticketCode, ACCOUNT_PROFILE_ORG_EMAIL_VALIDATE_API);
    }

    public SimpleStatusDto sendEmailToValidateOrgEmailAddress(String toEmailAddress, String ticketCode) {
        return sendEmailValidationEmail(toEmailAddress, ticketCode, ORG_JOIN_EMAIL_VALIDATE_API);
    }

    private SimpleStatusDto sendEmailValidationEmail(String toEmailAddress, String ticketCode, String validateAPI) {

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
                "<p><a href='"+sitelinkUrl+ validateAPI + "?validationCode="+ticketCode+"'>Validate Email</a></p></body></html>");
        mail.addContent(content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        SimpleStatusDto responseDto = null;
        try {
            request.method = Method.POST;
            request.endpoint = "mail/send";
            request.body = mail.build();
            Response response = sg.api(request);

            if (isEmailSuccessStatus(response.statusCode)) {
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
        mail.setSubject("Reset Your Torchie Account");
        mail.setReplyTo(new Email(fromAddress, fromName));

        Personalization personalization = new Personalization();
        Email to = new Email();
        to.setEmail(toEmailAddress);
        personalization.addTo(to);

        mail.addPersonalization(personalization);

        Content content = new Content();
        content.setType("text/html");

        content.setValue("<html><body> <p> If you need to install Torchie Shell, first download the latest release here:</p>" +
                "<p><a href='"+downloadLinkUrl+ "' > Download Torchie Shell</a></p>" +
                "<br/><p>Then use this token to re-activate your account:</p><p>"+activationToken+"</p></body></html>");
        mail.addContent(content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        SimpleStatusDto responseDto = null;
        try {
            request.method = Method.POST;
            request.endpoint = "mail/send";
            request.body = mail.build();
            Response response = sg.api(request);

            if (isEmailSuccessStatus(response.statusCode)) {
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
