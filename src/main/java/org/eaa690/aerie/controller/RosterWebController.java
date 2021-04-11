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
    public final static String REGULAR_MEMBERSHIP_COUNT = "regularMembershipCount";

    /**
     * REGULAR_MEMBERSHIP_EXPIRED_COUNT.
     */
    public final static String REGULAR_MEMBERSHIP_EXPIRED_COUNT = "regularMembershipExpiredCount";

    /**
     * REGULAR_MEMBERSHIP_WILL_EXPIRE_30_COUNT.
     */
    public final static String REGULAR_MEMBERSHIP_WILL_EXPIRE_30_COUNT = "regularMembershipWillExpire30Count";

    /**
     * REGULAR_MEMBERSHIP_WILL_EXPIRE_7_COUNT.
     */
    public final static String REGULAR_MEMBERSHIP_WILL_EXPIRE_7_COUNT = "regularMembershipWillExpire7Count";

    /**
     * FAMILY_MEMBERSHIP_COUNT.
     */
    public final static String FAMILY_MEMBERSHIP_COUNT = "familyMembershipCount";

    /**
     * FAMILY_MEMBERSHIP_EXPIRED_COUNT.
     */
    public final static String FAMILY_MEMBERSHIP_EXPIRED_COUNT = "familyMembershipExpiredCount";

    /**
     * FAMILY_MEMBERSHIP_WILL_EXPIRE_30_COUNT.
     */
    public final static String FAMILY_MEMBERSHIP_WILL_EXPIRE_30_COUNT = "familyMembershipWillExpire30Count";

    /**
     * FAMILY_MEMBERSHIP_WILL_EXPIRE_7_COUNT.
     */
    public final static String FAMILY_MEMBERSHIP_WILL_EXPIRE_7_COUNT = "familyMembershipWillExpire7Count";

    /**
     * FAMILY_MEMBER_COUNT.
     */
    public final static String FAMILY_MEMBER_COUNT = "familyMemberCount";

    /**
     * FAMILY_MEMBER_EXPIRED_COUNT.
     */
    public final static String FAMILY_MEMBER_EXPIRED_COUNT = "familyMemberExpiredCount";

    /**
     * FAMILY_MEMBER_WILL_EXPIRE_30_COUNT.
     */
    public final static String FAMILY_MEMBER_WILL_EXPIRE_30_COUNT = "familyMemberWillExpire30Count";

    /**
     * FAMILY_MEMBER_WILL_EXPIRE_7_COUNT.
     */
    public final static String FAMILY_MEMBER_WILL_EXPIRE_7_COUNT = "familyMemberWillExpire7Count";

    /**
     * STUDENT_MEMBERSHIP_COUNT.
     */
    public final static String STUDENT_MEMBERSHIP_COUNT = "studentMembershipCount";

    /**
     * STUDENT_MEMBERSHIP_EXPIRED_COUNT.
     */
    public final static String STUDENT_MEMBERSHIP_EXPIRED_COUNT = "studentMembershipExpiredCount";

    /**
     * STUDENT_MEMBERSHIP_WILL_EXPIRE_30_COUNT.
     */
    public final static String STUDENT_MEMBERSHIP_WILL_EXPIRE_30_COUNT = "studentMembershipWillExpire30Count";

    /**
     * STUDENT_MEMBERSHIP_WILL_EXPIRE_7_COUNT.
     */
    public final static String STUDENT_MEMBERSHIP_WILL_EXPIRE_7_COUNT = "studentMembershipWillExpire7Count";

    /**
     * LIFETIME_MEMBERSHIP_COUNT.
     */
    public final static String LIFETIME_MEMBERSHIP_COUNT = "lifetimeMembershipCount";

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

    @GetMapping({"/membershipreport"})
    public String membershipReport(Model model) {
        final MembershipReport membershipReport = rosterService.getMembershipReport();
        model.addAttribute(REGULAR_MEMBERSHIP_COUNT, membershipReport.getRegularMemberCount());
        model.addAttribute(REGULAR_MEMBERSHIP_EXPIRED_COUNT, membershipReport.getRegularMemberExpiredCount());
        model.addAttribute(REGULAR_MEMBERSHIP_WILL_EXPIRE_30_COUNT, membershipReport.getRegularMemberWillExpire30DaysCount());
        model.addAttribute(REGULAR_MEMBERSHIP_WILL_EXPIRE_7_COUNT, membershipReport.getRegularMemberWillExpire7DaysCount());
        model.addAttribute(FAMILY_MEMBERSHIP_COUNT, membershipReport.getFamilyMembershipCount());
        model.addAttribute(FAMILY_MEMBERSHIP_EXPIRED_COUNT, membershipReport.getFamilyMembershipExpiredCount());
        model.addAttribute(FAMILY_MEMBERSHIP_WILL_EXPIRE_30_COUNT, membershipReport.getFamilyMembershipWillExpire30DaysCount());
        model.addAttribute(FAMILY_MEMBERSHIP_WILL_EXPIRE_7_COUNT, membershipReport.getFamilyMembershipWillExpire7DaysCount());
        model.addAttribute(FAMILY_MEMBER_COUNT, membershipReport.getFamilyMemberCount());
        model.addAttribute(FAMILY_MEMBER_EXPIRED_COUNT, membershipReport.getFamilyMemberExpiredCount());
        model.addAttribute(FAMILY_MEMBER_WILL_EXPIRE_30_COUNT, membershipReport.getFamilyMemberWillExpire30DaysCount());
        model.addAttribute(FAMILY_MEMBER_WILL_EXPIRE_7_COUNT, membershipReport.getFamilyMemberWillExpire7DaysCount());
        model.addAttribute(STUDENT_MEMBERSHIP_COUNT, membershipReport.getStudentMemberCount());
        model.addAttribute(STUDENT_MEMBERSHIP_EXPIRED_COUNT, membershipReport.getStudentMemberExpiredCount());
        model.addAttribute(STUDENT_MEMBERSHIP_WILL_EXPIRE_30_COUNT, membershipReport.getStudentMemberWillExpire30DaysCount());
        model.addAttribute(STUDENT_MEMBERSHIP_WILL_EXPIRE_7_COUNT, membershipReport.getStudentMemberWillExpire7DaysCount());
        model.addAttribute(LIFETIME_MEMBERSHIP_COUNT, membershipReport.getLifetimeMemberCount());
        return "membershipreport";
    }
}
