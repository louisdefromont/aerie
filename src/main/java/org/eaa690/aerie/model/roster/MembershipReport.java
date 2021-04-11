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

/**
 * MembershipReport.
 */
public class MembershipReport {

    /**
     * Sum of all regular membership types.
     */
    private Long regularMemberCount = 0L;

    /**
     * Sum of all expired regular membership types.
     */
    private Long regularMemberExpiredCount = 0L;

    /**
     * Sum of all regular membership types that will expire in 30 days.
     */
    private Long regularMemberWillExpire30DaysCount = 0L;

    /**
     * Sum of all regular membership types that will expire in 7 days.
     */
    private Long regularMemberWillExpire7DaysCount = 0L;

    /**
     * Sum of "num of family" for all family membership types.
     */
    private Long familyMemberCount = 0L;

    /**
     * Sum of all expired family members.
     */
    private Long familyMemberExpiredCount = 0L;

    /**
     * Sum of all family members that will expire in 30 days.
     */
    private Long familyMemberWillExpire30DaysCount = 0L;

    /**
     * Sum of all family members that will expire in 7 days.
     */
    private Long familyMemberWillExpire7DaysCount = 0L;

    /**
     * Sum of all family membership types.
     */
    private Long familyMembershipCount = 0L;

    /**
     * Sum of all expired family membership types.
     */
    private Long familyMembershipExpiredCount = 0L;

    /**
     * Sum of all family membership types that will expire in 30 days.
     */
    private Long familyMembershipWillExpire30DaysCount = 0L;

    /**
     * Sum of all family membership types that will expire in 7 days.
     */
    private Long familyMembershipWillExpire7DaysCount = 0L;

    /**
     * Sum of all lifetime membership types.
     */
    private Long lifetimeMemberCount = 0L;

    /**
     * Sum of all honorary membership types.
     */
    private Long honoraryMemberCount = 0L;

    /**
     * Sum of all student membership types.
     */
    private Long studentMemberCount = 0L;

    /**
     * Sum of all expired student membership types.
     */
    private Long studentMemberExpiredCount = 0L;

    /**
     * Sum of all student membership types that will expire in 30 days.
     */
    private Long studentMemberWillExpire30DaysCount = 0L;

    /**
     * Sum of all student membership types that will expire in 7 days.
     */
    private Long studentMemberWillExpire7DaysCount = 0L;

    /**
     * Sum of all prospect membership types.
     */
    private Long prospectMemberCount = 0L;

    /**
     * Sum of all non-member membership types.
     */
    private Long nonMemberCount = 0L;

    public Long getRegularMemberCount() {
        return regularMemberCount;
    }

    public void setRegularMemberCount(Long regularMemberCount) {
        this.regularMemberCount = regularMemberCount;
    }

    public Long getFamilyMemberCount() {
        return familyMemberCount;
    }

    public void setFamilyMemberCount(Long familyMemberCount) {
        this.familyMemberCount = familyMemberCount;
    }

    public Long getFamilyMembershipCount() {
        return familyMembershipCount;
    }

    public void setFamilyMembershipCount(Long familyMembershipCount) {
        this.familyMembershipCount = familyMembershipCount;
    }

    public Long getLifetimeMemberCount() {
        return lifetimeMemberCount;
    }

    public void setLifetimeMemberCount(Long lifetimeMemberCount) {
        this.lifetimeMemberCount = lifetimeMemberCount;
    }

    public Long getHonoraryMemberCount() {
        return honoraryMemberCount;
    }

    public void setHonoraryMemberCount(Long honoraryMemberCount) {
        this.honoraryMemberCount = honoraryMemberCount;
    }

    public Long getStudentMemberCount() {
        return studentMemberCount;
    }

    public void setStudentMemberCount(Long studentMemberCount) {
        this.studentMemberCount = studentMemberCount;
    }

    public Long getProspectMemberCount() {
        return prospectMemberCount;
    }

    public void setProspectMemberCount(Long prospectMemberCount) {
        this.prospectMemberCount = prospectMemberCount;
    }

    public Long getNonMemberCount() {
        return nonMemberCount;
    }

    public void setNonMemberCount(Long nonMemberCount) {
        this.nonMemberCount = nonMemberCount;
    }

    public Long getRegularMemberExpiredCount() {
        return regularMemberExpiredCount;
    }

    public void setRegularMemberExpiredCount(Long regularMemberExpiredCount) {
        this.regularMemberExpiredCount = regularMemberExpiredCount;
    }

    public Long getFamilyMembershipExpiredCount() {
        return familyMembershipExpiredCount;
    }

    public void setFamilyMembershipExpiredCount(Long familyMembershipExpiredCount) {
        this.familyMembershipExpiredCount = familyMembershipExpiredCount;
    }

    public Long getStudentMemberExpiredCount() {
        return studentMemberExpiredCount;
    }

    public void setStudentMemberExpiredCount(Long studentMemberExpiredCount) {
        this.studentMemberExpiredCount = studentMemberExpiredCount;
    }

    public Long getRegularMemberWillExpire30DaysCount() {
        return regularMemberWillExpire30DaysCount;
    }

    public void setRegularMemberWillExpire30DaysCount(Long regularMemberWillExpire30DaysCount) {
        this.regularMemberWillExpire30DaysCount = regularMemberWillExpire30DaysCount;
    }

    public Long getRegularMemberWillExpire7DaysCount() {
        return regularMemberWillExpire7DaysCount;
    }

    public void setRegularMemberWillExpire7DaysCount(Long regularMemberWillExpire7DaysCount) {
        this.regularMemberWillExpire7DaysCount = regularMemberWillExpire7DaysCount;
    }

    public Long getFamilyMembershipWillExpire30DaysCount() {
        return familyMembershipWillExpire30DaysCount;
    }

    public void setFamilyMembershipWillExpire30DaysCount(Long familyMembershipWillExpire30DaysCount) {
        this.familyMembershipWillExpire30DaysCount = familyMembershipWillExpire30DaysCount;
    }

    public Long getFamilyMembershipWillExpire7DaysCount() {
        return familyMembershipWillExpire7DaysCount;
    }

    public void setFamilyMembershipWillExpire7DaysCount(Long familyMembershipWillExpire7DaysCount) {
        this.familyMembershipWillExpire7DaysCount = familyMembershipWillExpire7DaysCount;
    }

    public Long getStudentMemberWillExpire30DaysCount() {
        return studentMemberWillExpire30DaysCount;
    }

    public void setStudentMemberWillExpire30DaysCount(Long studentMemberWillExpire30DaysCount) {
        this.studentMemberWillExpire30DaysCount = studentMemberWillExpire30DaysCount;
    }

    public Long getStudentMemberWillExpire7DaysCount() {
        return studentMemberWillExpire7DaysCount;
    }

    public void setStudentMemberWillExpire7DaysCount(Long studentMemberWillExpire7DaysCount) {
        this.studentMemberWillExpire7DaysCount = studentMemberWillExpire7DaysCount;
    }

    public Long getFamilyMemberExpiredCount() {
        return familyMemberExpiredCount;
    }

    public void setFamilyMemberExpiredCount(Long familyMemberExpiredCount) {
        this.familyMemberExpiredCount = familyMemberExpiredCount;
    }

    public Long getFamilyMemberWillExpire30DaysCount() {
        return familyMemberWillExpire30DaysCount;
    }

    public void setFamilyMemberWillExpire30DaysCount(Long familyMemberWillExpire30DaysCount) {
        this.familyMemberWillExpire30DaysCount = familyMemberWillExpire30DaysCount;
    }

    public Long getFamilyMemberWillExpire7DaysCount() {
        return familyMemberWillExpire7DaysCount;
    }

    public void setFamilyMemberWillExpire7DaysCount(Long familyMemberWillExpire7DaysCount) {
        this.familyMemberWillExpire7DaysCount = familyMemberWillExpire7DaysCount;
    }
}
