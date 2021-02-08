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
 * TAFTests.
 */
public class TAFTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * TAF.
     */
    private TAF taf;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        taf = new TAF();
    }

    /**
     * Test setting ICAO.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setIcao() throws Exception {
        taf.setIcao(TEXT);

        Assert.assertNotNull(taf.getIcao());
    }

    /**
     * Test setting Raw Text.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setRawText() throws Exception {
        taf.setRawText(TEXT);

        Assert.assertNotNull(taf.getRawText());
    }

    /**
     * Test setting timestamp.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setTimestamp() throws Exception {
        taf.setTimestamp(new Timestamp());

        Assert.assertNotNull(taf.getTimestamp());
    }

    /**
     * Test setting forecast.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setForecast() throws Exception {
        taf.setForecast(Arrays.asList(new Forecast()));

        Assert.assertNotNull(taf.getForecast());
    }

}