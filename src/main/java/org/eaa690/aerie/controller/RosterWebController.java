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

package org.eaa690.aerie.controller;

import org.eaa690.aerie.model.roster.MembershipReport;
import org.eaa690.aerie.service.RosterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RosterWebController {

    /**
     * REGULAR_MEMBERSHIP_COUNT.
     */
    public static final String REGULAR_MEMBERSHIP_COUNT = "regularMembershipCount";

    /**
     * REGULAR_MEMBERSHIP_EXPIRED_COUNT.
     */
    public static final String REGULAR_MEMBERSHIP_EXPIRED_COUNT = "regularMembershipExpiredCount";

    /**
     * REGULAR_MEMBERSHIP_WILL_EXPIRE_30_COUNT.
     */
    public static final String REGULAR_MEMBERSHIP_WILL_EXPIRE_30_COUNT = "regularMembershipWillExpire30Count";

    /**
     * REGULAR_MEMBERSHIP_WILL_EXPIRE_7_COUNT.
     */
    public static final String REGULAR_MEMBERSHIP_WILL_EXPIRE_7_COUNT = "regularMembershipWillExpire7Count";

    /**
     * FAMILY_MEMBERSHIP_COUNT.
     */
    public static final String FAMILY_MEMBERSHIP_COUNT = "familyMembershipCount";

    /**
     * FAMILY_MEMBERSHIP_EXPIRED_COUNT.
     */
    public static final String FAMILY_MEMBERSHIP_EXPIRED_COUNT = "familyMembershipExpiredCount";

    /**
     * FAMILY_MEMBERSHIP_WILL_EXPIRE_30_COUNT.
     */
    public static final String FAMILY_MEMBERSHIP_WILL_EXPIRE_30_COUNT = "familyMembershipWillExpire30Count";

    /**
     * FAMILY_MEMBERSHIP_WILL_EXPIRE_7_COUNT.
     */
    public static final String FAMILY_MEMBERSHIP_WILL_EXPIRE_7_COUNT = "familyMembershipWillExpire7Count";

    /**
     * FAMILY_MEMBER_COUNT.
     */
    public static final String FAMILY_MEMBER_COUNT = "familyMemberCount";

    /**
     * FAMILY_MEMBER_EXPIRED_COUNT.
     */
    public static final String FAMILY_MEMBER_EXPIRED_COUNT = "familyMemberExpiredCount";

    /**
     * FAMILY_MEMBER_WILL_EXPIRE_30_COUNT.
     */
    public static final String FAMILY_MEMBER_WILL_EXPIRE_30_COUNT = "familyMemberWillExpire30Count";

    /**
     * FAMILY_MEMBER_WILL_EXPIRE_7_COUNT.
     */
    public static final String FAMILY_MEMBER_WILL_EXPIRE_7_COUNT = "familyMemberWillExpire7Count";

    /**
     * STUDENT_MEMBERSHIP_COUNT.
     */
    public static final String STUDENT_MEMBERSHIP_COUNT = "studentMembershipCount";

    /**
     * STUDENT_MEMBERSHIP_EXPIRED_COUNT.
     */
    public static final String STUDENT_MEMBERSHIP_EXPIRED_COUNT = "studentMembershipExpiredCount";

    /**
     * STUDENT_MEMBERSHIP_WILL_EXPIRE_30_COUNT.
     */
    public static final String STUDENT_MEMBERSHIP_WILL_EXPIRE_30_COUNT = "studentMembershipWillExpire30Count";

    /**
     * STUDENT_MEMBERSHIP_WILL_EXPIRE_7_COUNT.
     */
    public static final String STUDENT_MEMBERSHIP_WILL_EXPIRE_7_COUNT = "studentMembershipWillExpire7Count";

    /**
     * LIFETIME_MEMBERSHIP_COUNT.
     */
    public static final String LIFETIME_MEMBERSHIP_COUNT = "lifetimeMembershipCount";

    /**
     * NON_MEMBERSHIP_COUNT.
     */
    public static final String NON_MEMBERSHIP_COUNT = "nonMembershipCount";

    /**
     * RosterService.
     */
    private RosterService rosterService;

    /**
     * Sets RosterService.
     *
     * @param value RosterService
     */
    @Autowired
    public void setRosterService(final RosterService value) {
        rosterService = value;
    }

    /**
     * Membership Report.
     *
     * @param model Model
     * @return report
     */
    @GetMapping({"/membershipreport"})
    public String membershipReport(final Model model) {
        final MembershipReport membershipReport = rosterService.getMembershipReport();
        model.addAttribute(REGULAR_MEMBERSHIP_COUNT, membershipReport.getRegularMemberCount());
        model.addAttribute(FAMILY_MEMBERSHIP_COUNT, membershipReport.getFamilyMembershipCount());
        model.addAttribute(FAMILY_MEMBER_COUNT, membershipReport.getFamilyMemberCount());
        model.addAttribute(STUDENT_MEMBERSHIP_COUNT, membershipReport.getStudentMemberCount());
        model.addAttribute(LIFETIME_MEMBERSHIP_COUNT, membershipReport.getLifetimeMemberCount());
        return "membershipreport";
    }

    /**
     * Full Membership Report.
     *
     * @param model Model
     * @return report
     */
    @GetMapping({"/fullmembershipreport"})
    public String fullMembershipReport(final Model model) {
        final MembershipReport membershipReport = rosterService.getMembershipReport();
        model.addAttribute(REGULAR_MEMBERSHIP_COUNT, membershipReport.getRegularMemberCount());
        model.addAttribute(REGULAR_MEMBERSHIP_EXPIRED_COUNT, membershipReport.getRegularMemberExpiredCount());
        model.addAttribute(REGULAR_MEMBERSHIP_WILL_EXPIRE_30_COUNT,
                membershipReport.getRegularMemberWillExpire30DaysCount());
        model.addAttribute(REGULAR_MEMBERSHIP_WILL_EXPIRE_7_COUNT,
                membershipReport.getRegularMemberWillExpire7DaysCount());
        model.addAttribute(FAMILY_MEMBERSHIP_COUNT, membershipReport.getFamilyMembershipCount());
        model.addAttribute(FAMILY_MEMBERSHIP_EXPIRED_COUNT, membershipReport.getFamilyMembershipExpiredCount());
        model.addAttribute(FAMILY_MEMBERSHIP_WILL_EXPIRE_30_COUNT,
                membershipReport.getFamilyMembershipWillExpire30DaysCount());
        model.addAttribute(FAMILY_MEMBERSHIP_WILL_EXPIRE_7_COUNT,
                membershipReport.getFamilyMembershipWillExpire7DaysCount());
        model.addAttribute(FAMILY_MEMBER_COUNT, membershipReport.getFamilyMemberCount());
        model.addAttribute(FAMILY_MEMBER_EXPIRED_COUNT, membershipReport.getFamilyMemberExpiredCount());
        model.addAttribute(FAMILY_MEMBER_WILL_EXPIRE_30_COUNT, membershipReport.getFamilyMemberWillExpire30DaysCount());
        model.addAttribute(FAMILY_MEMBER_WILL_EXPIRE_7_COUNT, membershipReport.getFamilyMemberWillExpire7DaysCount());
        model.addAttribute(STUDENT_MEMBERSHIP_COUNT, membershipReport.getStudentMemberCount());
        model.addAttribute(STUDENT_MEMBERSHIP_EXPIRED_COUNT, membershipReport.getStudentMemberExpiredCount());
        model.addAttribute(STUDENT_MEMBERSHIP_WILL_EXPIRE_30_COUNT,
                membershipReport.getStudentMemberWillExpire30DaysCount());
        model.addAttribute(STUDENT_MEMBERSHIP_WILL_EXPIRE_7_COUNT,
                membershipReport.getStudentMemberWillExpire7DaysCount());
        model.addAttribute(LIFETIME_MEMBERSHIP_COUNT, membershipReport.getLifetimeMemberCount());
        model.addAttribute(NON_MEMBERSHIP_COUNT, membershipReport.getNonMemberCount());
        return "fullmembershipreport";
    }
}
