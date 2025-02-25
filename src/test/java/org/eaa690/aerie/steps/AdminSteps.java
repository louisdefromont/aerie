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

package org.eaa690.aerie.steps;

import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.eaa690.aerie.TestContext;

/**
 * Admin test steps.
 */
public class AdminSteps extends BaseSteps {

    /**
     * Admin service.
     */
    private final String ADMIN = "admin/";

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public AdminSteps(final TestContext testContext) {
        super(testContext);
    }

    @When("^I request all Slack users$")
    public void iRequestAllSlackUsers() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ADMIN + "slack/users")
                .then());
    }

    @When("^I request the email queue count$")
    public void iRequestEmailQueueCount() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ADMIN + "email/queue-count")
                .then());
    }

    @When("^I request an email be sent to new member (.*)$")
    public void iRequestEmailToNewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ADMIN + "email/" + rosterId + "/new-membership")
                .then());
    }

    @When("^I request a (.*) email be sent to member (.*) to renew their membership$")
    public void iRequestEmailToRenewMember(final String order, final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ADMIN + "email/" + rosterId + "/renew-membership/" + order)
                .then());
    }

    @When("^I request a text be sent to new member (.*)$")
    public void iRequestTextToNewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ADMIN + "sms/" + rosterId + "/new-membership")
                .then());
    }

    @When("^I request a text be sent to member (.*) to renew their membership$")
    public void iRequestTextToRenewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ADMIN + "sms/" + rosterId + "/renew-membership")
                .then());
    }

    @When("^I request a Slack message be sent to new member (.*)$")
    public void iRequestSlackToNewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ADMIN + "slack/" + rosterId + "/new-membership")
                .then());
    }

    @When("^I request a Slack message be sent to member (.*) to renew their membership$")
    public void iRequestSlackToRenewMember(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ADMIN + "slack/" + rosterId + "/renew-membership")
                .then());
    }

    @When("^I request member (.*) be added to the MailChimp member list$")
    public void iRequestMemberAddedToMailChimpMemberList(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ADMIN + "mailchimp/" + rosterId + "/add-member")
                .then());
    }

    @When("^I request non-member (.*) be added to the MailChimp non-member list$")
    public void iRequestNonMemberAddedToMailChimpNonMemberList(final String rosterId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ADMIN + "mailchimp/" + rosterId + "/add-non-member")
                .then());
    }

}
