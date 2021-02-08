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
 * TimezoneTests.
 */
public class TimezoneTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * Timezone.
     */
    private Timezone timezone;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        timezone = new Timezone();
    }

    /**
     * Test setting gmt.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setGmt() throws Exception {
        timezone.setGmt(TEXT);

        Assert.assertNotNull(timezone.getGmt());
    }

    /**
     * Test setting dst.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setDst() throws Exception {
        timezone.setDst(TEXT);

        Assert.assertNotNull(timezone.getDst());
    }

    /**
     * Test setting Tzid.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setTzid() throws Exception {
        timezone.setTzid(TEXT);

        Assert.assertNotNull(timezone.getTzid());
    }

}