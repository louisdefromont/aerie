package org.eaa690.aerie.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OtherInfo {

    private Pattern familyPattern = Pattern.compile("Family=\\[(.*?)\\]");

    private Pattern slackPattern = Pattern.compile("Slack=\\[(.*?)\\]");

    private Pattern rfidPattern = Pattern.compile("RFID=\\[(.*?)\\]");

    private Pattern otherPattern = Pattern.compile("Other=\\[(.*?)\\]");

    private String rfid;

    private String slack;

    private String description;

    private List<String> family = new ArrayList<>();

    public OtherInfo(String otherInfo) {
        final Matcher familyMatcher = familyPattern.matcher(otherInfo);
        if (familyMatcher.find()) {
            final String familyStr = familyMatcher.group(1);
            if (StringUtils.isNotEmpty(familyStr)) {
                family = Stream.of(familyStr.split("\\s*,\\s*")).collect(Collectors.toList());
                family.stream().forEach(name -> name.trim());
            }
        }
        final Matcher slackMatcher = slackPattern.matcher(otherInfo);
        if (slackMatcher.find()) {
            slack = slackMatcher.group(1);
        }
        final Matcher rfidMatcher = rfidPattern.matcher(otherInfo);
        if (rfidMatcher.find()) {
            rfid = rfidMatcher.group(1);
        }
        final Matcher descriptionMatcher = otherPattern.matcher(otherInfo);
        if (descriptionMatcher.find()) {
            description = descriptionMatcher.group(1);
        }
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(final String rfid) {
        this.rfid = rfid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getSlack() {
        return slack;
    }

    public void setSlack(final String slack) {
        this.slack = slack;
    }

    public List<String> getFamily() {
        return family;
    }

    public void setFamily(final List<String> family) {
        this.family = family;
    }

    public String toString() {
        final List<String> elements = new ArrayList<>();
        elements.add(String.format("RFID=[%s]", rfid));
        elements.add(String.format("Slack=[%s]", slack));
        elements.add(String.format("Family=[%s]", family.stream().collect(Collectors.joining(", "))));
        elements.add(String.format("Description=[%s]", description));
        return elements.stream().collect(Collectors.joining("; "));
    }
}
