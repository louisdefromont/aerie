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
 * VisibilityTests.
 */
public class VisibilityTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * Visibility.
     */
    private Visibility visibility;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        visibility = new Visibility();
    }

    /**
     * Test setting miles.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setMiles() throws Exception {
        visibility.setMiles(TEXT);

        Assert.assertNotNull(visibility.getMiles());
    }

    /**
     * Test setting meters.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setMeters() throws Exception {
        visibility.setMeters(TEXT);

        Assert.assertNotNull(visibility.getMeters());
    }

}