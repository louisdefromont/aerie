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

package org.eaa690.aerie.model;

import java.util.Optional;

import org.springframework.data.repository.Repository;

/**
 * PropertyRepository.
 */
public interface PropertyRepository extends Repository<Property, Long> {

    /**
     * Gets a property.
     *
     * @param key Key
     * @return Property
     */
    Optional<Property> findByKey(String key);

}
