package org.eaa690.aerie.communication;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.service.JotFormService;
import org.eaa690.aerie.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendGridEmailSender extends MessageSender<org.eaa690.aerie.model.communication.Email> {

    @Autowired
    public SendGridEmailSender(AcceptsEmailPredicate acceptsMessagePredicate) {
        super("Send_Grid_Email", acceptsMessagePredicate);
    }

    @Autowired
    private SendGrid sendGrid;

    @Autowired
    private PropertyService propertyService;

    private Logger LOGGER = LoggerFactory.getLogger(SendGridEmailSender.class);

    @Autowired
    private JotFormService jotFormService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");

    public String sendMessage(org.eaa690.aerie.model.communication.Email message) {
        try {
            String to = message.getRecipientAddress();
            if (Boolean.parseBoolean(
                    propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_ENABLED_KEY).getValue())) {
                to = propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_RECIPIENT_KEY).getValue();
            }

            final Mail mail = new Mail();
            mail.setSubject(message.getSubject());
            if (message.getTemplateID() != null) {
                mail.setTemplateId(message.getTemplateID());
                mail.addPersonalization(personalize(message.getRecipientMember(), to));
            } else {
                mail.addContent(new Content("text/plain", message.getBody()));
            }
            mail.setFrom(new Email(propertyService
                    .get(PropertyKeyConstants.SEND_GRID_FROM_ADDRESS_KEY).getValue()));

            final Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            return String.format("Response... statusCode [%s]; body [%s]; headers [%s]",
            response.getStatusCode(), response.getBody(), response.getHeaders());

        } catch (IOException | ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }

    /**
     * Personalizes an email to the member.
     *
     * @param member Member
     * @param to address
     * @return Personalization
     * @throws ResourceNotFoundException when property is not found
     */
    private Personalization personalize(final Member member, final String to) throws ResourceNotFoundException {
        final Personalization personalization = new Personalization();
        personalization.addTo(new Email(to));
        personalization.addBcc(new Email(propertyService.get(PropertyKeyConstants.EMAIL_BCC_KEY).getValue()));
        personalization.addDynamicTemplateData("firstName", member.getFirstName());
        personalization.addDynamicTemplateData("lastName", member.getLastName());
        personalization.addDynamicTemplateData("url", jotFormService.buildRenewMembershipUrl(member));
        if (member.getExpiration() == null) {
            personalization.addDynamicTemplateData("expirationDate", sdf.format(new Date()));
        } else {
            personalization.addDynamicTemplateData("expirationDate", sdf.format(member.getExpiration()));
        }
        return personalization;
    }
    
}
