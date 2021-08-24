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

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

/**
 * Weather tests.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WeatherSteps {

    /**
     * Server Base URI.
     */
    private final static String BASE_URI = "http://localhost";

    /**
     * Local Server Port.
     */
    @LocalServerPort
    private int port;

    /**
     * Configure RestAssured to use server base URI and local port.
     */
    private void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    protected RequestSpecification requestSpecification() {
        configureRestAssured();
        return given();
    }

    private ValidatableResponse validatableResponse;

    /**
     * Weather service.
     */
    private String WEATHER = "weather/";

    @Given("^I am an unauthenticated user$")
    public void unauthenticatedUser() {
        requestSpecification();
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

    @And("^I want (.*) information$")
    public void iOnlyWantSpecificInformation(String field) {
        requestSpecification().param("data", field);
    }

    @When("^I request the (.*) METAR$")
    public void iRequestTheMETAR(String icao) {
        validatableResponse = requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(WEATHER + "metars/" + icao)
                .then();
    }

    @When("^I request the (.*) TAF$")
    public void iRequestTheTAF(String icao) {
        validatableResponse = requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get( WEATHER + "tafs/" + icao)
                .then();
    }

    @When("^I request the (.*) station$")
    public void iRequestTheStation(String icao) {
        validatableResponse = requestSpecification()
                .contentType(ContentType.JSON)
                .when()
                .get(WEATHER + "stations/" + icao)
                .then();
    }

    @And("^I should receive the (.*) data$")
    public void iShouldReceiveSpecificData(String field) {
        validatableResponse
                .assertThat()
                .body(field, Matchers.notNullValue());
    }

    @And("^I should receive data for multiple stations$")
    public void iShouldReceiveDataForMultipleStations() {
        validatableResponse
                .assertThat()
                .body("size()", Matchers.greaterThan(1));
    }

    @Then("^The request should be successful$")
    public void requestSuccessful() {
        validatableResponse
                .assertThat()
                .statusCode(Matchers.equalTo(HttpStatus.SC_OK));
    }

    @Then("^A (.*) exception should be thrown$")
    public void aExceptionShouldBeThrown(String exception) {
        switch (exception) {
            case "unauthorized":
                validatableResponse
                        .assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.SC_UNAUTHORIZED));
                break;
            case "forbidden":
                validatableResponse
                        .assertThat()
                        .statusCode(Matchers.equalTo(HttpStatus.SC_FORBIDDEN));
                break;
            default:
        }
    }
}
