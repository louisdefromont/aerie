package org.eaa690.aerie.communication;

import java.io.IOException;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;

import org.eaa690.aerie.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;

public class SendGridEmailSender implements MessageSender {

    @Autowired
    private SendGrid sendGrid;

    public String sendMessage(String recipientAddress, Mail message, PropertyService propertyService) throws IOException {
        final Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(message.build());
        Response response = sendGrid.api(request);

        return String.format("Response... statusCode [%s]; body [%s]; headers [%s]",
        response.getStatusCode(), response.getBody(), response.getHeaders());
    }

    @Override
    public String sendMessage(String recipientAddress, String message, PropertyService propertyService) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
