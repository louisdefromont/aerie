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
import org.eaa690.aerie.model.RFIDRequest;

/**
 * Roster test steps.
 */
public class RosterSteps extends BaseSteps {

    /**
     * Roster service.
     */
    private final String ROSTER = "roster/";

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public RosterSteps(final TestContext testContext) {
        super(testContext);
    }

    @When("^I request the roster membership report$")
    public void iRequestTheReport() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + "report")
                .then());
    }

    @When("^I request an update of the roster data$")
    public void iRequestUpdateRosterData() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .post(ROSTER + "update")
                .then());
    }

    @When("^I update member (.*)'s RFID with (.*)$")
    public void iUpdateMemberRFID(final String memberId, final String rfid) {
        final RFIDRequest rfidRequest = new RFIDRequest();
        rfidRequest.setRfid(rfid);
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(rfidRequest)
                .put(ROSTER + memberId + "/rfid")
                .then());
    }

    @When("^I find a member by their RFID (.*)$")
    public void iFindMemberByRFID(final String rfid) {
        final RFIDRequest rfidRequest = new RFIDRequest();
        rfidRequest.setRfid(rfid);
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .body(rfidRequest)
                .post(ROSTER + "find-by-rfid")
                .then());
    }

    @When("^I request the expiration data for member with ID (.*)$")
    public void iRequestExpirationData(final String memberId) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + memberId + "/expiration")
                .then());
    }

    @When("^I request RFID data for all members$")
    public void iRequestAllRFIDData() {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(ROSTER + "all-rfid")
                .then());
    }

}
