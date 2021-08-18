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

import lombok.Getter;
import lombok.Setter;
import org.eaa690.aerie.model.roster.Country;
import org.eaa690.aerie.model.roster.Gender;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.State;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.model.roster.WebAdminAccess;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Member.
 */
@Entity
@Table(name = "MEMBER")
@Getter
@Setter
public class Member extends BaseEntity implements Comparable<Member> {

    /**
     * Date representing the beginning of dates.
     */
    private static final Date ZERO_DATE = new Date(0);

    /**
     * Roster management system ID.
     */
    private Long rosterId;

    /**
     * Assigned RFID.
     */
    private String rfid;

    /**
     * Slack handle.
     */
    private String slack;

    /**
     * First Name.
     */
    private String firstName;

    /**
     * Last Name.
     */
    private String lastName;

    /**
     * Nickname.
     */
    private String nickname;

    /**
     * Username.
     */
    private String username;

    /**
     * Spouse.
     */
    private String spouse;

    /**
     * Gender.
     */
    private Gender gender;

    /**
     * Member Type.
     */
    private MemberType memberType;

    /**
     * Status.
     */
    private Status status;

    /**
     * Web Admin Access.
     */
    private WebAdminAccess webAdminAccess;

    /**
     * Address Line 1.
     */
    private String addressLine1;

    /**
     * Address Line 2.
     */
    private String addressLine2;

    /**
     * City.
     */
    private String city;

    /**
     * State.
     */
    private State state;

    /**
     * Zip Code.
     */
    private String zipCode;

    /**
     * Country.
     */
    private Country country;

    /**
     * Birth Date.
     */
    private String birthDate;

    /**
     * Joined Date.
     */
    private String joined;

    /**
     * Other Information.
     *
     * "RFID=[ABC123ZXY43221]; Slack=[@brian]; Family=[Jennifer Michael, Billy Michael]; # of Family=[2]; Additional "
     * "Info=[some random text]"
     */
    private String otherInfo;

    /**
     * Family.
     */
    private String family;

    /**
     * Num of Family.
     */
    private Long numOfFamily;

    /**
     * AdditionalInfo.
     */
    private String additionalInfo;

    /**
     * Home Phone.
     */
    private String homePhone;

    /**
     * Ratings.
     */
    private String ratings;

    /**
     * Aircraft Owned.
     */
    private String aircraftOwned;

    /**
     * Aircraft Project.
     */
    private String aircraftProject;

    /**
     * Aircraft Built.
     */
    private String aircraftBuilt;

    /**
     * IMC Club.
     */
    private boolean imcClub = Boolean.FALSE;

    /**
     * VMC Club.
     */
    private boolean vmcClub = Boolean.FALSE;

    /**
     * YE Pilot.
     */
    private boolean yePilot = Boolean.FALSE;

    /**
     * YE Volunteer.
     */
    private boolean yeVolunteer = Boolean.FALSE;

    /**
     * Eagle Pilot.
     */
    private boolean eaglePilot = Boolean.FALSE;

    /**
     * Eagle Volunteer.
     */
    private boolean eagleVolunteer = Boolean.FALSE;

    /**
     * EAA Membership Expiration Date.
     */
    private String eaaExpiration;

    /**
     * Youth Protection Expiration Date.
     */
    private String youthProtection;

    /**
     * Background Check Expiration Date.
     */
    private String backgroundCheck;

    /**
     * EAA Number.
     */
    private String eaaNumber;

    /**
     * Email.
     */
    private String email;

    /**
     * Cell Phone.
     */
    private String cellPhone;

    /**
     * Membership Expiration.
     */
    private Date expiration;

    /**
     * SimpleDateFormat.
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yy");

    /**
     * Email Enabled Flag.
     */
    private boolean emailEnabled = false;

    /**
     * SMS Enabled Flag.
     */
    private boolean smsEnabled = false;

    /**
     * Slack Enabled Flag.
     */
    private boolean slackEnabled = false;

    /**
     * Sets Slack.
     *
     * @param value Slack username
     */
    public void setSlack(final String value) {
        this.slack = value;
        if (value != null) {
            slackEnabled = true;
        }
    }

    /**
     * Gets birth date as a Date.
     *
     * @return birth date
     */
    public Date getBirthDateAsDate() {
        if (birthDate == null || "".equals(birthDate)) {
            return ZERO_DATE;
        }
        try {
            return SDF.parse(birthDate);
        } catch (ParseException e) {
            return ZERO_DATE;
        }
    }

    /**
     * Gets joined date as a Date.
     *
     * @return Joined Date
     */
    public Date getJoinedAsDate() {
        if (joined == null || "".equals(joined)) {
            return ZERO_DATE;
        }
        try {
            return SDF.parse(joined);
        } catch (ParseException e) {
            return ZERO_DATE;
        }
    }

    /**
     * Sets Cell Phone.
     *
     * @param value Cell Phone
     */
    public void setCellPhone(final String value) {
        this.cellPhone = value;
        if (value != null) {
            smsEnabled = true;
        }
    }

    /**
     * Set email.
     *
     * @param value Email address
     */
    public void setEmail(final String value) {
        this.email = value;
        if (value != null) {
            emailEnabled = true;
        }
    }

    /**
     * Gets NumOfFamily.
     *
     * @return numOfFamily
     */
    public Long getNumOfFamily() {
        if (numOfFamily == null) {
            return 0L;
        }
        return numOfFamily;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int compareTo(final Member other) {
        if (equals(other)) {
            return 0;
        }
        return 1;
    }

    /**
     * {@inheritDoc} Required implementation.
     *
     * @param o other Object
     * @return if the same
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(email, member.email)
                && Objects.equals(firstName, member.firstName)
                && Objects.equals(lastName, member.lastName);
    }

    /**
     * {@inheritDoc} Required implementation.
     *
     * @return generated hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(rosterId, rfid, firstName, lastName, eaaNumber, expiration);
    }

    /**
     * {@inheritDoc} Required implementation.
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        return "Member{"
                + "rosterId=" + rosterId
                + ", rfid='" + rfid + '\''
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", nickname='" + nickname + '\''
                + ", spouse='" + spouse + '\''
                + ", gender=" + gender
                + ", memberType=" + memberType
                + ", status=" + status
                + ", webAdminAccess=" + webAdminAccess
                + ", addressLine1='" + addressLine1 + '\''
                + ", addressLine2='" + addressLine2 + '\''
                + ", city='" + city + '\''
                + ", state=" + state
                + ", zipCode='" + zipCode + '\''
                + ", country=" + country
                + ", birthDate='" + birthDate + '\''
                + ", joined='" + joined + '\''
                + ", otherInfo='" + otherInfo + '\''
                + ", homePhone='" + homePhone + '\''
                + ", cellPhone='" + cellPhone + '\''
                + ", email='" + email + '\''
                + ", ratings='" + ratings + '\''
                + ", aircraftOwned='" + aircraftOwned + '\''
                + ", aircraftProject='" + aircraftProject + '\''
                + ", aircraftBuilt='" + aircraftBuilt + '\''
                + ", imcClub=" + imcClub
                + ", vmcClub=" + vmcClub
                + ", yePilot=" + yePilot
                + ", yeVolunteer=" + yeVolunteer
                + ", eaglePilot=" + eaglePilot
                + ", eagleVolunteer=" + eagleVolunteer
                + ", eaaNumber='" + eaaNumber + '\''
                + ", expiration=" + expiration
                + '}';
    }

}
