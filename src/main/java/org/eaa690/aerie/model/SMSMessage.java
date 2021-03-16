/*
 *  Copyright (C) 2021 Gwinnett County Experimental Aircraft Association
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eaa690.aerie.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SMSMessage.
 */
public class SMSMessage {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSMessage.class);

    /**
     * UTF-8.
     */
    public static final String UTF8 = "UTF-8";

    /**
     * ToCountry.
     */
    private String toCountry;

    /**
     * ToState.
     */
    private String toState;

    /**
     * SmsMessageSid.
     */
    private String smsMessageSid;

    /**
     * NumMedia.
     */
    private String numMedia;

    /**
     * ToCity.
     */
    private String toCity;

    /**
     * FromZip.
     */
    private String fromZip;

    /**
     * SmsSid.
     */
    private String smsSid;

    /**
     * FromState.
     */
    private String fromState;

    /**
     * SmsStatus.
     */
    private String smsStatus;

    /**
     * FromCity.
     */
    private String fromCity;

    /**
     * Body.
     */
    private String body;

    /**
     * FromCountry.
     */
    private String fromCountry;

    /**
     * To.
     */
    private String destination;

    /**
     * ToZip.
     */
    private String toZip;

    /**
     * NumSegments.
     */
    private String numSegments;

    /**
     * MessageSid.
     */
    private String messageSid;

    /**
     * AccountSid.
     */
    private String accountSid;

    /**
     * From.
     */
    private String from;

    /**
     * ApiVersion.
     */
    private String apiVersion;

    /**
     * Initializes an instance of <code>SMSMessage</code> with the default data.
     */
    public SMSMessage() {
        // Default constructor
    }

    /**
     * Initializes an instance of <code>SMSMessage</code> with the default data.
     *
     * @param value to be parsed
     */
    public SMSMessage(final String value) {
        if (value != null) {
            String[] parts = value.split("&");
            for (String part : parts) {
                try {
                    String[] kvPair = part.split("=");
                    TwilioPart twilioPart = TwilioPart.valueOf(kvPair[0]);
                    switch (twilioPart) {
                        case ToCountry:
                            toCountry = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case ToState:
                            toState = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case SmsMessageSid:
                            smsMessageSid = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case NumMedia:
                            numMedia = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case ToCity:
                            toCity = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case FromZip:
                            fromZip = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case SmsSid:
                            smsSid = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case FromState:
                            fromState = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case SmsStatus:
                            smsStatus = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case FromCity:
                            fromCity = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case Body:
                            body = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case FromCountry:
                            fromCountry = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case To:
                            destination = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case ToZip:
                            toZip = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case NumSegments:
                            numSegments = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case MessageSid:
                            messageSid = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case AccountSid:
                            accountSid = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case From:
                            from = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        case ApiVersion:
                            apiVersion = URLDecoder.decode(kvPair[1], UTF8);
                            break;
                        default:
                            break;
                    }
                } catch (UnsupportedEncodingException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Retrieves the value for {@link #toCountry}.
     *
     * @return the current value
     */
    public String getToCountry() {
        return toCountry;
    }

    /**
     * Provides a value for {@link #toCountry}.
     *
     * @param value the new value to set
     */
    public void setToCountry(final String value) {
        toCountry = value;
    }

    /**
     * Retrieves the value for {@link #toState}.
     *
     * @return the current value
     */
    public String getToState() {
        return toState;
    }

    /**
     * Provides a value for {@link #toState}.
     *
     * @param value the new value to set
     */
    public void setToState(final String value) {
        toState = value;
    }

    /**
     * Retrieves the value for {@link #smsMessageSid}.
     *
     * @return the current value
     */
    public String getSmsMessageSid() {
        return smsMessageSid;
    }

    /**
     * Provides a value for {@link #smsMessageSid}.
     *
     * @param value the new value to set
     */
    public void setSmsMessageSid(final String value) {
        smsMessageSid = value;
    }

    /**
     * Retrieves the value for {@link #numMedia}.
     *
     * @return the current value
     */
    public String getNumMedia() {
        return numMedia;
    }

    /**
     * Provides a value for {@link #numMedia}.
     *
     * @param value the new value to set
     */
    public void setNumMedia(final String value) {
        numMedia = value;
    }

    /**
     * Retrieves the value for {@link #toCity}.
     *
     * @return the current value
     */
    public String getToCity() {
        return toCity;
    }

    /**
     * Provides a value for {@link #toCity}.
     *
     * @param value the new value to set
     */
    public void setToCity(final String value) {
        toCity = value;
    }

    /**
     * Retrieves the value for {@link #fromZip}.
     *
     * @return the current value
     */
    public String getFromZip() {
        return fromZip;
    }

    /**
     * Provides a value for {@link #fromZip}.
     *
     * @param value the new value to set
     */
    public void setFromZip(final String value) {
        fromZip = value;
    }

    /**
     * Retrieves the value for {@link #smsSid}.
     *
     * @return the current value
     */
    public String getSmsSid() {
        return smsSid;
    }

    /**
     * Provides a value for {@link #smsSid}.
     *
     * @param value the new value to set
     */
    public void setSmsSid(final String value) {
        smsSid = value;
    }

    /**
     * Retrieves the value for {@link #fromState}.
     *
     * @return the current value
     */
    public String getFromState() {
        return fromState;
    }

    /**
     * Provides a value for {@link #fromState}.
     *
     * @param value the new value to set
     */
    public void setFromState(final String value) {
        fromState = value;
    }

    /**
     * Retrieves the value for {@link #smsStatus}.
     *
     * @return the current value
     */
    public String getSmsStatus() {
        return smsStatus;
    }

    /**
     * Provides a value for {@link #smsStatus}.
     *
     * @param value the new value to set
     */
    public void setSmsStatus(final String value) {
        smsStatus = value;
    }

    /**
     * Retrieves the value for {@link #fromCity}.
     *
     * @return the current value
     */
    public String getFromCity() {
        return fromCity;
    }

    /**
     * Provides a value for {@link #fromCity}.
     *
     * @param value the new value to set
     */
    public void setFromCity(final String value) {
        fromCity = value;
    }

    /**
     * Retrieves the value for {@link #body}.
     *
     * @return the current value
     */
    public String getBody() {
        return body;
    }

    /**
     * Provides a value for {@link #body}.
     *
     * @param value the new value to set
     */
    public void setBody(final String value) {
        body = value;
    }

    /**
     * Retrieves the value for {@link #fromCountry}.
     *
     * @return the current value
     */
    public String getFromCountry() {
        return fromCountry;
    }

    /**
     * Provides a value for {@link #fromCountry}.
     *
     * @param value the new value to set
     */
    public void setFromCountry(final String value) {
        fromCountry = value;
    }

    /**
     * Retrieves the value for {@link #destination}.
     *
     * @return the current value
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Provides a value for {@link #destination}.
     *
     * @param value the new value to set
     */
    public void setDestination(final String value) {
        destination = value;
    }

    /**
     * Retrieves the value for {@link #toZip}.
     *
     * @return the current value
     */
    public String getToZip() {
        return toZip;
    }

    /**
     * Provides a value for {@link #toZip}.
     *
     * @param value the new value to set
     */
    public void setToZip(final String value) {
        toZip = value;
    }

    /**
     * Retrieves the value for {@link #numSegments}.
     *
     * @return the current value
     */
    public String getNumSegments() {
        return numSegments;
    }

    /**
     * Provides a value for {@link #numSegments}.
     *
     * @param value the new value to set
     */
    public void setNumSegments(final String value) {
        numSegments = value;
    }

    /**
     * Retrieves the value for {@link #messageSid}.
     *
     * @return the current value
     */
    public String getMessageSid() {
        return messageSid;
    }

    /**
     * Provides a value for {@link #messageSid}.
     *
     * @param value the new value to set
     */
    public void setMessageSid(final String value) {
        messageSid = value;
    }

    /**
     * Retrieves the value for {@link #accountSid}.
     *
     * @return the current value
     */
    public String getAccountSid() {
        return accountSid;
    }

    /**
     * Provides a value for {@link #accountSid}.
     *
     * @param value the new value to set
     */
    public void setAccountSid(final String value) {
        accountSid = value;
    }

    /**
     * Retrieves the value for {@link #from}.
     *
     * @return the current value
     */
    public String getFrom() {
        return from;
    }

    /**
     * Provides a value for {@link #from}.
     *
     * @param value the new value to set
     */
    public void setFrom(final String value) {
        from = value;
    }

    /**
     * Retrieves the value for {@link #apiVersion}.
     *
     * @return the current value
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Provides a value for {@link #apiVersion}.
     *
     * @param value the new value to set
     */
    public void setApiVersion(final String value) {
        apiVersion = value;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public String toString() {
        return "SMSMessage [toCountry="
                + toCountry
                + ", toState="
                + toState
                + ", smsMessageSid="
                + smsMessageSid
                + ", numMedia="
                + numMedia
                + ", toCity="
                + toCity
                + ", fromZip="
                + fromZip
                + ", smsSid="
                + smsSid
                + ", fromState="
                + fromState
                + ", smsStatus="
                + smsStatus
                + ", fromCity="
                + fromCity
                + ", body="
                + body
                + ", fromCountry="
                + fromCountry
                + ", destination="
                + destination
                + ", toZip="
                + toZip
                + ", numSegments="
                + numSegments
                + ", messageSid="
                + messageSid
                + ", accountSid="
                + accountSid
                + ", from="
                + from
                + ", apiVersion="
                + apiVersion
                + "]";
    }
}
