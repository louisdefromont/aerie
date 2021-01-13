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
 * WindTests.
 */
public class WindTests {

    /**
     * TEXT.
     */
    public static final Integer VALUE = Integer.valueOf(1);

    /**
     * Wind.
     */
    private Wind wind;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        wind = new Wind();
    }

    /**
     * Test setting degrees.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setDegrees() throws Exception {
        wind.setDegrees(VALUE);

        Assert.assertNotNull(wind.getDegrees());
    }

    /**
     * Test setting speedKt.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setSpeedKt() throws Exception {
        wind.setSpeedKt(VALUE);

        Assert.assertNotNull(wind.getSpeedKt());
    }

    /**
     * Test setting speedKts.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setSpeedKts() throws Exception {
        wind.setSpeedKts(VALUE);

        Assert.assertNotNull(wind.getSpeedKts());
    }

    /**
     * Test setting speedMps.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setSpeedMps() throws Exception {
        wind.setSpeedMps(VALUE);

        Assert.assertNotNull(wind.getSpeedMps());
    }

    /**
     * Test setting speedMph.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setSpeedMph() throws Exception {
        wind.setSpeedMph(VALUE);

        Assert.assertNotNull(wind.getSpeedMph());
    }

    /**
     * Test setting gustKt.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setGustKt() throws Exception {
        wind.setGustKt(VALUE);

        Assert.assertNotNull(wind.getGustKt());
    }

    /**
     * Test setting gustMps.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setGustMps() throws Exception {
        wind.setGustMps(VALUE);

        Assert.assertNotNull(wind.getGustMps());
    }

    /**
     * Test setting gustMph.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setGustMph() throws Exception {
        wind.setGustMph(VALUE);

        Assert.assertNotNull(wind.getGustMph());
    }

}