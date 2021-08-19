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

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

/**
 * TestContext.
 */
public class TestContext {

    /**
     * TestContext instance.
     */
    private static TestContext instance = null;

    /**
     * RestAssured Response.
     */
    protected Response response = null;

    /**
     * RestAssured ValidatableResponse.
     */
    protected ValidatableResponse json = null;

    /**
     * RestAssured RequestSpecification.
     */
    protected RequestSpecification request = null;

    /**
     * Private constructor
     */
    private TestContext() {
        // private constructor
    }

    /**
     * Gets singleton instance.
     *
     * @return TestContext
     */
    public static TestContext getInstance() {
        if (instance == null) {
            instance = new TestContext();
        }
        return instance;
    }

    /**
     * Retrieves previous step response.
     *
     * @return Response
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Sets step response.
     *
     * @param response Response
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * Retrieves step json.
     *
     * @return ValidatableResponse
     */
    public ValidatableResponse getJson() {
        return json;
    }

    /**
     * Sets step json.
     *
     * @param json ValidatableResponse
     */
    public void setJson(ValidatableResponse json) {
        this.json = json;
    }

    /**
     * Retrieves request.
     *
     * @return RequestSpecification
     */
    public RequestSpecification getRequest() {
        return request;
    }

    /**
     * Sets request.
     *
     * @param request RequestSpecification
     */
    public void setRequest(RequestSpecification request) {
        this.request = request;
    }

    /**
     * Resets TestContext variables.
     */
    public void reset() {
        response = null;
        json = null;
        request = null;
    }

}
