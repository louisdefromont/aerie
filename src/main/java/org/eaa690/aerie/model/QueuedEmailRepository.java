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

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * QueuedEmailRepository.
 */
public interface QueuedEmailRepository extends Repository<QueuedEmail, Long> {

    /**
     * Gets all QueuedEmail.
     *
     * @return all QueuedEmail
     */
    Optional<List<QueuedEmail>> findAll();

    /**
     * Saves a QueuedEmail.
     *
     * @param queuedEmail QueuedEmail
     * @return QueuedEmail
     */
    QueuedEmail save(QueuedEmail queuedEmail);

    /**
     * Saves a QueuedEmail.
     *
     * @param queuedEmail QueuedEmail
     * @return QueuedEmail
     */
    QueuedEmail delete(QueuedEmail queuedEmail);

}
