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
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ForecastTests.
 */
public class ForecastTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * VALUE.
     */
    public static final int VALUE = 1;

    /**
     * Forecast.
     */
    private Forecast forecast;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        forecast = new Forecast();
    }

    /**
     * Test setting change indicator.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setChangeIndicator() throws Exception {
        forecast.setChangeIndicator(TEXT);

        Assert.assertNotNull(forecast.getChangeIndicator());
    }

    /**
     * Test setting forecast timestamp.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setForecastTimestamp() throws Exception {
        forecast.setTimestamp(new ForecastTimestamp());

        Assert.assertNotNull(forecast.getTimestamp());
    }

    /**
     * Test setting clouds.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setClouds() throws Exception {
        forecast.setClouds(Arrays.asList(new Cloud()));

        Assert.assertNotNull(forecast.getClouds());
    }

    /**
     * Test setting visibility.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setVisibility() throws Exception {
        forecast.setVisibility(new Visibility());

        Assert.assertNotNull(forecast.getVisibility());
    }

    /**
     * Test setting wind.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setWind() throws Exception {
        forecast.setWind(new Wind());

        Assert.assertNotNull(forecast.getWind());
    }

}