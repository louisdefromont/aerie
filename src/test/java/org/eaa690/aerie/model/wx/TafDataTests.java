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
 * TafDataTests.
 */
public class TafDataTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * VALUE.
     */
    public static final int VALUE = 1;

    /**
     * TafData.
     */
    private TafData tafData;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        tafData = new TafData(TEXT);
    }

    /**
     * Test setting results.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setResults() throws Exception {
        tafData.setResults(VALUE);

        Assert.assertNotNull(tafData.getResults());
    }

    /**
     * Test setting error.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setError() throws Exception {
        tafData.setError(TEXT);

        Assert.assertNotNull(tafData.getError());
    }

    /**
     * Test setting data.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setData() throws Exception {
        tafData.setData(Arrays.asList(new TAF()));

        Assert.assertNotNull(tafData.getData());
    }

}