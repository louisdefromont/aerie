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

package org.eaa690.aerie.model.roster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.service.RosterService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OtherInfoBuilder {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(OtherInfoBuilder.class);

    /**
     * Family Pattern.
     */
    public static Pattern additionalFamilyPattern = Pattern.compile("Family=\\[(.*?)\\]");

    /**
     * # of Family Pattern.
     */
    public static Pattern numOfFamilyPattern = Pattern.compile("# of Family=\\[(.*?)\\]");

    /**
     * Slack Pattern.
     */
    public static Pattern slackPattern = Pattern.compile("Slack=\\[(.*?)\\]");

    /**
     * RFID Pattern.
     */
    public static Pattern rfidPattern = Pattern.compile("RFID=\\[(.*?)\\]");

    /**
     * Additional Info Pattern.
     */
    public static Pattern additionalInfoPattern = Pattern.compile("Additional Info=\\[(.*?)\\]");

    /**
     * Additional Family.
     */
    private String additionalFamily;

    /**
     * Slack.
     */
    private String slack;

    /**
     * Number of Family.
     */
    private String numOfFamily;

    /**
     * RFID.
     */
    private String rfid;

    /**
     * Additional Information.
     */
    private String additionalInfo;

    /**
     * Default constructor.
     */
    public OtherInfoBuilder() {
        // Default constructor
    }

    public String getAdditionalFamily() {
        return additionalFamily;
    }

    public void setAdditionalFamily(String additionalFamily) {
        this.additionalFamily = additionalFamily;
    }

    public String getNumberOfFamily() {
        return numOfFamily;
    }

    public void setNumberOfFamily(String numOfFamily) {
        this.numOfFamily = numOfFamily;
    }

    public String getSlack() {
        return slack;
    }

    public void setSlack(String slack) {
        this.slack = slack;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getRaw() {
        final List<String> parts = new ArrayList<>();
        if (additionalFamily != null) {
            parts.add(String.format("Family=[%s]", additionalFamily));
        }
        if (numOfFamily != null) {
            parts.add(String.format("# of Family=[%s]", numOfFamily));
        }
        if (slack != null) {
            parts.add(String.format("Slack=[%s]", slack));
        }
        if (rfid != null) {
            parts.add(String.format("RFID=[%s]", rfid));
        }
        if (additionalInfo != null) {
            parts.add(String.format("Additional Info=[%s]", additionalInfo));
        }
        return String.join("; ", parts);
    }

    public void setRaw(String raw) {
        boolean matched = false;
        if (raw != null) {
            final Matcher additionalFamilyMatcher = additionalFamilyPattern.matcher(raw);
            if (additionalFamilyMatcher.find()) {
                matched = true;
                setAdditionalFamily(additionalFamilyMatcher.group(1));
                LOGGER.info("Set additional family to ["+getAdditionalFamily()+"]");
            }
            final Matcher numOfFamilyMatcher = numOfFamilyPattern.matcher(raw);
            if (numOfFamilyMatcher.find()) {
                matched = true;
                setNumberOfFamily(numOfFamilyMatcher.group(1));
                LOGGER.info("Set number of family to ["+getNumberOfFamily()+"]");
            }
            final Matcher slackMatcher = slackPattern.matcher(raw);
            if (slackMatcher.find()) {
                matched = true;
                setSlack(slackMatcher.group(1));
                LOGGER.info("Set Slack to ["+getSlack()+"]");
            }
            final Matcher rfidMatcher = rfidPattern.matcher(raw);
            if (rfidMatcher.find()) {
                matched = true;
                setRfid(rfidMatcher.group(1));
                LOGGER.info("Set RFID to ["+getRfid()+"]");
            }
            final Matcher additionalInfoMatcher = additionalInfoPattern.matcher(raw);
            if (additionalInfoMatcher.find()) {
                matched = true;
                setAdditionalInfo(additionalInfoMatcher.group(1));
                LOGGER.info("Set additional info to ["+getAdditionalInfo()+"]");
            }
            if (!matched) {
                LOGGER.info("No patterns matched.  Setting additional info to ["+raw+"]");
                setAdditionalInfo(raw);
            }
        }
    }

    @Override
    public String toString() {
        return getRaw();
    }
}
