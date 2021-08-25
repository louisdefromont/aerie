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

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

/**
 * QueuedEmail.
 */
@Entity
@Table(name = "QUEUED_EMAIL")
@Getter
@Setter
public class QueuedEmail extends BaseEntity implements Comparable<QueuedEmail> {
    /**
     * Email template ID.
     */
    private String templateIdKey;

    /**
     * Email Subject.
     */
    private String subjectKey;

    /**
     * Member ID.
     */
    private Long memberId;

    /**
     * Default constructor.
     */
    public QueuedEmail() {
        // Do nothing
    }

    /**
     * Constructor.
     *
     * @param templateId Email template ID
     * @param subject Email Subject
     * @param member Member ID
     */
    public QueuedEmail(final String templateId, final String subject, final Long member) {
        templateIdKey = templateId;
        subjectKey = subject;
        memberId = member;
        setCreatedAt(new Date());
        setUpdatedAt(new Date());
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int compareTo(final QueuedEmail other) {
        if (equals(other)) {
            return 0;
        }
        return 1;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueuedEmail qe = (QueuedEmail) o;
        return Objects.equals(templateIdKey, qe.templateIdKey)
                && Objects.equals(subjectKey, qe.subjectKey)
                && Objects.equals(memberId, qe.memberId);
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int hashCode() {
        return Objects.hash(templateIdKey, subjectKey, memberId);
    }

}
