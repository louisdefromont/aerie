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

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

/**
 * Member.
 */
@Entity
@Table(name = "MEMBER")
public class Member extends BaseEntity implements Comparable<Member> {

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
     * Member Type.
     */
    private String memberType;

    /**
     * Membership Expiration.
     */
    private Date expiration;

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

    public Long getRosterId() {
        return rosterId;
    }

    public void setRosterId(Long rosterId) {
        this.rosterId = rosterId;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getSlack() {
        return slack;
    }

    public void setSlack(String slack) {
        this.slack = slack;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEaaNumber() {
        return eaaNumber;
    }

    public void setEaaNumber(String eaaNumber) {
        this.eaaNumber = eaaNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public void setSmsEnabled(boolean enabled) {
        smsEnabled = enabled;
    }

    public boolean smsEnabled() {
        return smsEnabled;
    }

    public void setSlackEnabled(boolean enabled) {
        slackEnabled = enabled;
    }

    public boolean slackEnabled() {
        return slackEnabled;
    }

    public void setEmailEnabled(boolean enabled) {
        emailEnabled = enabled;
    }

    public boolean emailEnabled() {
        return emailEnabled;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(rosterId, member.rosterId) &&
                Objects.equals(rfid, member.rfid) &&
                Objects.equals(firstName, member.firstName) &&
                Objects.equals(lastName, member.lastName) &&
                Objects.equals(eaaNumber, member.eaaNumber) &&
                Objects.equals(expiration, member.expiration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rosterId, rfid, firstName, lastName, eaaNumber, expiration);
    }
}