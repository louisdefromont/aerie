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

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.eaa690.aerie.TestContext;
import org.hamcrest.Matchers;

/**
 * Weather test steps.
 */
public class WeatherSteps extends BaseSteps {

    /**
     * Weather service.
     */
    private final String WEATHER = "weather/";

    /**
     * Constructor.
     *
     * @param testContext TestContext
     */
    public WeatherSteps(final TestContext testContext) {
        super(testContext);
    }

    @Given("^I want (.*) information$")
    public void iOnlyWantSpecificInformation(String field) {
        requestSpecification().param("data", field);
    }

    @When("^I request the (.*) METAR$")
    public void iRequestTheMETAR(String icao) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(WEATHER + "metars/" + icao)
                .then());
    }

    @When("^I request the (.*) TAF$")
    public void iRequestTheTAF(String icao) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( WEATHER + "tafs/" + icao)
                .then());
    }

    @When("^I request the (.*) station$")
    public void iRequestTheStation(String icao) {
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(WEATHER + "stations/" + icao)
                .then());
    }

    @When("^I request a (.*) for an unprovided station$")
    public void iRequestDataForAnInvalidStation(String weatherProduct) {
        String product;
        switch (weatherProduct) {
            case "TAF":
                product = "tafs/";
                break;
            case "station":
                product = "stations/";
                break;
            default:
                product = "metars/";
        }
        testContext.setValidatableResponse(requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(WEATHER + product)
                .then());
    }

    @Then("^I should receive the (.*) data$")
    public void iShouldReceiveSpecificData(String field) {
        testContext.getValidatableResponse()
                .assertThat()
                .body(field, Matchers.notNullValue());
    }

    @Then("^I should receive data for multiple stations$")
    public void iShouldReceiveDataForMultipleStations() {
        testContext.getValidatableResponse()
                .assertThat()
                .body("size()", Matchers.greaterThan(1));
    }

}
