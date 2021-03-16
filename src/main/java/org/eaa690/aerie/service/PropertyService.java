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
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Property;
import org.eaa690.aerie.model.PropertyRepository;

/**
 * PropertyService.
 */
@Service
public class PropertyService {

    /**
     * NO_PROPERTY_FOUND.
     */
    public static final String NO_PROPERTY_FOUND = "No property found for Key [%s]";

    /**
     * PropertyRepository.
     */
    @Autowired
    private PropertyRepository propertyRepository;

    /**
     * Sets PropertyRepository.
     *
     * @param pRepository PropertyRepository
     */
    @Autowired
    public void setPropertyRepository(final PropertyRepository pRepository) {
        propertyRepository = pRepository;
    }

    static Cache<String, Property> propertyCache =
            CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    /**
     * Gets a property.
     *
     * @param key Key
     * @return Property
     * @throws ResourceNotFoundException when no property is found
     */
    public Property get(final String key) throws ResourceNotFoundException {
        if (propertyCache.getIfPresent(key) != null) {
            return propertyCache.getIfPresent(key);
        }
        final Optional<Property> propertyOpt = propertyRepository.findByKey(key);
        if (propertyOpt.isPresent()) {
            final Property property = propertyOpt.get();
            propertyCache.put(key, property);
            return property;
        }
        throw new ResourceNotFoundException(String.format(NO_PROPERTY_FOUND, key));
    }

}