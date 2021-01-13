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

package org.eaa690.aerie.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * ResourceNotFoundExceptionTests.
 */
public class ResourceNotFoundExceptionTests {

    /**
     * TEXT.
     */
    public static final String TEXT = "ABC123";

    /**
     * Test empty constructor.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void empty() throws Exception {
        Assert.assertNotNull(new ResourceNotFoundException());
    }

    /**
     * Test message only constructor.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void messageOnly() throws Exception {
        Assert.assertNotNull(new ResourceNotFoundException(TEXT));
    }

    /**
     * Test message with throwable constructor.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void messageWithThrowable() throws Exception {
        Assert.assertNotNull(new ResourceNotFoundException(TEXT, new Exception()));
    }

}