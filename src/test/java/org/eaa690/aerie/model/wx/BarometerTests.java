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
 * BarometerTests.
 */
public class BarometerTests {

    /**
     * VALUE.
     */
    public static final Double VALUE = Double.valueOf(10);

    /**
     * Barometer.
     */
    private Barometer barometer;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        barometer = new Barometer();
    }

    /**
     * Test setting Hg.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setHg() throws Exception {
        barometer.setHg(VALUE);

        Assert.assertNotNull(barometer.getHg());
    }

    /**
     * Test setting Kpa.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setKpa() throws Exception {
        barometer.setKpa(VALUE);

        Assert.assertNotNull(barometer.getKpa());
    }

    /**
     * Test setting Mb.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setMb() throws Exception {
        barometer.setMb(VALUE);

        Assert.assertNotNull(barometer.getMb());
    }

}