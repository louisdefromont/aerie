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
 * TemperatureTests.
 */
public class TimestampTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * Forecast.
     */
    private Timestamp timestamp;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        timestamp = new Timestamp();
    }

    /**
     * Test setting issued.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setIssued() throws Exception {
        timestamp.setIssued(TEXT);

        Assert.assertNotNull(timestamp.getIssued());
    }

    /**
     * Test setting bulletin.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setBulletin() throws Exception {
        timestamp.setBulletin(TEXT);

        Assert.assertNotNull(timestamp.getBulletin());
    }

    /**
     * Test setting valid from.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setValidFrom() throws Exception {
        timestamp.setValidFrom(TEXT);

        Assert.assertNotNull(timestamp.getValidFrom());
    }

    /**
     * Test setting valid to.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setValidTo() throws Exception {
        timestamp.setValidTo(TEXT);

        Assert.assertNotNull(timestamp.getValidTo());
    }

}