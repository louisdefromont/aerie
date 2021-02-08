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

import java.util.Arrays;

/**
 * METARTests.
 */
public class METARTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * VALUE.
     */
    public static final int VALUE = 1;

    /**
     * METAR.
     */
    private METAR metar;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        metar = new METAR();
    }

    /**
     * Test setting ICAO.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setIcao() throws Exception {
        metar.setIcao(TEXT);

        Assert.assertNotNull(metar.getIcao());
    }

    /**
     * Test setting Raw Text.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setRawText() throws Exception {
        metar.setRawText(TEXT);

        Assert.assertNotNull(metar.getRawText());
    }

    /**
     * Test setting name.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setName() throws Exception {
        metar.setName(TEXT);

        Assert.assertNotNull(metar.getName());
    }

    /**
     * Test setting observed.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setObserved() throws Exception {
        metar.setObserved(TEXT);

        Assert.assertNotNull(metar.getObserved());
    }

    /**
     * Test setting barometer.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setBarometer() throws Exception {
        metar.setBarometer(new Barometer());

        Assert.assertNotNull(metar.getBarometer());
    }

    /**
     * Test setting ceiling.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setCeiling() throws Exception {
        metar.setCeiling(new Ceiling());

        Assert.assertNotNull(metar.getCeiling());
    }

    /**
     * Test setting dewpoint.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setDewpoint() throws Exception {
        metar.setDewpoint(new Dewpoint());

        Assert.assertNotNull(metar.getDewpoint());
    }

    /**
     * Test setting elevation.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setElevation() throws Exception {
        metar.setElevation(new Elevation());

        Assert.assertNotNull(metar.getElevation());
    }

    /**
     * Test setting flight category.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setFlightCategory() throws Exception {
        metar.setFlightCategory(TEXT);

        Assert.assertNotNull(metar.getFlightCategory());
    }

    /**
     * Test setting temperature.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setTemperature() throws Exception {
        metar.setTemperature(new Temperature());

        Assert.assertNotNull(metar.getTemperature());
    }

    /**
     * Test setting visibility.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setVisibility() throws Exception {
        metar.setVisibility(new Visibility());

        Assert.assertNotNull(metar.getVisibility());
    }

    /**
     * Test setting wind.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setWind() throws Exception {
        metar.setWind(new Wind());

        Assert.assertNotNull(metar.getWind());
    }

    /**
     * Test setting clouds.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setClouds() throws Exception {
        metar.setClouds(Arrays.asList(new Cloud()));

        Assert.assertNotNull(metar.getClouds());
    }

    /**
     * Test setting humidity percent.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setHumidityPercent() throws Exception {
        metar.setHumidityPercent(String.valueOf(VALUE));

        Assert.assertNotNull(metar.getHumidityPercent());
    }

    /**
     * Test setting humidity percent.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setHumidityPercentInt() throws Exception {
        metar.setHumidityPercent(VALUE);

        Assert.assertNotNull(metar.getHumidityPercent());
    }

}