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
 * RadiusTests.
 */
public class RadiusTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * DOUBLE_VALUE.
     */
    public static final Double DOUBLE_VALUE = Double.valueOf(1);

    /**
     * INT_VALUE.
     */
    public static final Integer INT_VALUE = Integer.valueOf(1);

    /**
     * Forecast.
     */
    private Radius radius;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        radius = new Radius();
    }

    /**
     * Test setting change indicator.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setFrom() throws Exception {
        radius.setFrom(TEXT);

        Assert.assertNotNull(radius.getFrom());
    }

    /**
     * Test setting miles.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setMiles() throws Exception {
        radius.setMiles(DOUBLE_VALUE);

        Assert.assertNotNull(radius.getMiles());
    }

    /**
     * Test setting meters.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setMeters() throws Exception {
        radius.setMeters(DOUBLE_VALUE);

        Assert.assertNotNull(radius.getMeters());
    }

    /**
     * Test setting direction.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setDirection() throws Exception {
        radius.setDirection(INT_VALUE);

        Assert.assertNotNull(radius.getDirection());
    }

    /**
     * Test setting compass.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setCompass() throws Exception {
        radius.setCompass(TEXT);

        Assert.assertNotNull(radius.getCompass());
    }

}