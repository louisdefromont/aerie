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
     * FAMILY_MEMBERSHIP_COUNT.
     */
    public final static String FAMILY_MEMBERSHIP_COUNT = "familyMembershipCount";

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
        model.addAttribute(FAMILY_MEMBERSHIP_COUNT, membershipReport.getFamilyMembershipCount());
        return "membershipreport";
    }
}
