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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * BaseEntity.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {
                "createdAt", "updatedAt"
        },
        allowGetters = true)
public class BaseEntity implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Created At.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Date createdAt;

    /**
     * Updated At.
     */
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private Date updatedAt;

    /**
     * Retrieves the value for {@link #id}.
     *
     * @return the current value
     */
    public Long getId() {
        return id;
    }

    /**
     * Provides a value for {@link #id}.
     *
     * @param value the new value to set
     */
    public void setId(final Long value) {
        id = value;
    }

    /**
     * Retrieves the value for {@link #createdAt}.
     *
     * @return the current value
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Provides a value for {@link #createdAt}.
     *
     * @param value the new value to set
     */
    public void setCreatedAt(final Date value) {
        createdAt = value;
    }

    /**
     * Retrieves the value for {@link #updatedAt}.
     *
     * @return the current value
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Provides a value for {@link #updatedAt}.
     *
     * @param value the new value to set
     */
    public void setUpdatedAt(final Date value) {
        updatedAt = value;
    }

}