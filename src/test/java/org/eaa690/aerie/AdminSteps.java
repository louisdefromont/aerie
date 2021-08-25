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

package org.eaa690.aerie;

import io.cucumber.java.en.When;
import io.restassured.http.ContentType;

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

}
