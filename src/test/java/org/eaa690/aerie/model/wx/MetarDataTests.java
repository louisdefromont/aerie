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
 * MetarDataTests.
 */
public class MetarDataTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * TEXT.
     */
    public static final int VALUE = 1;

    /**
     * MetarData.
     */
    private MetarData metarData;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        metarData = new MetarData(TEXT);
    }

    /**
     * Test setting results.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setResults() throws Exception {
        metarData.setResults(VALUE);

        Assert.assertNotNull(metarData.getResults());
    }

    /**
     * Test setting data.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setData() throws Exception {
        metarData.setData(Arrays.asList(new METAR()));

        Assert.assertNotNull(metarData.getData());
    }

    /**
     * Test setting error.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setError() throws Exception {
        metarData.setError(TEXT);

        Assert.assertNotNull(metarData.getError());
    }

}