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
 * CeilingTests.
 */
public class CeilingTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * VALUE.
     */
    public static final Double VALUE = Double.valueOf(10);

    /**
     * Ceiling.
     */
    private Ceiling ceiling;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        ceiling = new Ceiling();
    }

    /**
     * Test setting code.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setCode() throws Exception {
        ceiling.setCode(TEXT);

        Assert.assertNotNull(ceiling.getCode());
    }

    /**
     * Test setting text.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setText() throws Exception {
        ceiling.setText(TEXT);

        Assert.assertNotNull(ceiling.getText());
    }

    /**
     * Test setting feet AGL.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setFeetAGL() throws Exception {
        ceiling.setFeetAgl(VALUE);

        Assert.assertNotNull(ceiling.getFeetAgl());
    }

    /**
     * Test setting meters AGL.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setMetersAGL() throws Exception {
        ceiling.setMetersAgl(VALUE);

        Assert.assertNotNull(ceiling.getMetersAgl());
    }

}