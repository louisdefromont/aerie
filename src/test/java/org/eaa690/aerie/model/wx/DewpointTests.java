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

package org.eaa690.aerie.model.wx;

import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DewpointTests.
 */
public class DewpointTests {

    /**
     * VALUE.
     */
    public static final Integer VALUE = Integer.valueOf(1);

    /**
     * Dewpoint.
     */
    private Dewpoint dewpoint;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        dewpoint = new Dewpoint();
    }

    /**
     * Test setting celsius.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setCelsius() throws Exception {
        dewpoint.setCelsius(VALUE);

        Assert.assertNotNull(dewpoint.getCelsius());
    }

    /**
     * Test setting fahrenheit.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setFarenheit() throws Exception {
        dewpoint.setFahrenheit(VALUE);

        Assert.assertNotNull(dewpoint.getFahrenheit());
    }

}