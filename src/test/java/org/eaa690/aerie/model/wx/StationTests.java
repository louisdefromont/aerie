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
 * StationTests.
 */
public class StationTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * Station.
     */
    private Station station;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        station = new Station();
    }

    /**
     * Test setting ICAO.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setIcao() throws Exception {
        station.setIcao(TEXT);

        Assert.assertNotNull(station.getIcao());
    }

    /**
     * Test setting name.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setName() throws Exception {
        station.setName(TEXT);

        Assert.assertNotNull(station.getName());
    }

    /**
     * Test setting activated.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setActivated() throws Exception {
        station.setActivated(TEXT);

        Assert.assertNotNull(station.getActivated());
    }

    /**
     * Test setting city.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setCity() throws Exception {
        station.setCity(TEXT);

        Assert.assertNotNull(station.getCity());
    }

    /**
     * Test setting country.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setCountry() throws Exception {
        station.setCountry(TEXT);

        Assert.assertNotNull(station.getCountry());
    }

    /**
     * Test setting IATA.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setIata() throws Exception {
        station.setIata(TEXT);

        Assert.assertNotNull(station.getIata());
    }

    /**
     * Test setting magnetic.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setMagnetic() throws Exception {
        station.setMagnetic(TEXT);

        Assert.assertNotNull(station.getMagnetic());
    }

    /**
     * Test setting sectional.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setSectional() throws Exception {
        station.setSectional(TEXT);

        Assert.assertNotNull(station.getSectional());
    }

    /**
     * Test setting state.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setState() throws Exception {
        station.setState(TEXT);

        Assert.assertNotNull(station.getState());
    }

    /**
     * Test setting status.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setStatus() throws Exception {
        station.setStatus(TEXT);

        Assert.assertNotNull(station.getStatus());
    }

    /**
     * Test setting type.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setType() throws Exception {
        station.setType(TEXT);

        Assert.assertNotNull(station.getType());
    }

    /**
     * Test setting useage.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setUseage() throws Exception {
        station.setUseage(TEXT);

        Assert.assertNotNull(station.getUseage());
    }

    /**
     * Test setting radius.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setRadius() throws Exception {
        station.setRadius(new Radius());

        Assert.assertNotNull(station.getRadius());
    }

    /**
     * Test setting elevation.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setElevation() throws Exception {
        station.setElevation(new Elevation());

        Assert.assertNotNull(station.getElevation());
    }

    /**
     * Test setting latitude.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setLatitude() throws Exception {
        station.setLatitude(new Coordinates());

        Assert.assertNotNull(station.getLatitude());
    }

    /**
     * Test setting longitude.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setLongitude() throws Exception {
        station.setLongitude(new Coordinates());

        Assert.assertNotNull(station.getLongitude());
    }

    /**
     * Test setting timezone.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setTimezone() throws Exception {
        station.setTimezone(new Timezone());

        Assert.assertNotNull(station.getTimezone());
    }

}