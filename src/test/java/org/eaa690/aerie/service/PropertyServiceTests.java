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

package org.eaa690.aerie.service;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.eaa690.aerie.TestDataFactory;
import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Property;
import org.eaa690.aerie.model.PropertyRepository;

/**
 * PropertyServiceTests.
 */
public class PropertyServiceTests {

    /**
     * PropertyRepository.
     */
    @Mock
    private PropertyRepository propertyRepository;

    /**
     * PropertyService.
     */
    private PropertyService propertyService;

    /**
     * Property.
     */
    private Property notificationGCPStorageProperty;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        MockitoAnnotations.initMocks(this);

        notificationGCPStorageProperty = TestDataFactory
                .getProperty(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY, CommonConstants.ID);

        Mockito
                .doReturn(Optional.of(notificationGCPStorageProperty))
                .when(propertyRepository)
                .findByKey(ArgumentMatchers.anyString());

        propertyService = new PropertyService();
        propertyService.setPropertyRepository(propertyRepository);
    }

    /**
     * Test get.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void get() throws Exception {
        Assert.assertNotNull(propertyService.get(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));

        Mockito.verify(propertyRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(propertyRepository);
    }

    /**
     * Test get.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getNotCached() throws Exception {
        Assert.assertNotNull(propertyService.get(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));

        Mockito.verify(propertyRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(propertyRepository);
    }

    /**
     * Test get.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getNotFound() throws Exception {
        Mockito.doReturn(Optional.empty()).when(propertyRepository).findByKey(ArgumentMatchers.anyString());

        propertyService.get(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY);

        Mockito.verify(propertyRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(propertyRepository);
    }

}