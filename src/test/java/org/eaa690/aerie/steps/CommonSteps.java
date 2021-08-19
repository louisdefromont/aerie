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

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;

/**
 * Common cucumber test steps.
 */
public class CommonSteps extends BaseSteps {

    @Given("^I am an unauthenticated user$")
    public void unauthenticatedUser() {
        testContext.setRequest(RestAssured.given());
    }

    @Given("^I am an authenticated user$")
    public void authenticatedUser() {
        throw new PendingException();
    }

    @Given("^My username is (.*)$")
    public void myUsernameIs(String username) {
        throw new PendingException();
    }

    @Given("^I have been assigned the (.*) role$")
    public void iHaveBeenAssignedRole(String role) {
        throw new PendingException();
    }

    @Given("^A user with username (.*) exists$")
    public void userExists(String username) {
        // TODO: Do something
    }

    @Then("^The request should be successful$")
    public void requestSuccessful() {
        testContext.setJson(testContext.getResponse().then().statusCode(HttpStatus.SC_OK));
    }

    @Then("^A (.*) exception should be thrown$")
    public void aExceptionShouldBeThrown(String exception) {
        switch (exception) {
            case "unauthorized":
                testContext.getResponse().then().statusCode(HttpStatus.SC_UNAUTHORIZED);
                break;
            case "forbidden":
                testContext.getResponse().then().statusCode(HttpStatus.SC_FORBIDDEN);
                break;
            default:
        }
    }
}
