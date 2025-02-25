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
import org.eaa690.aerie.constant.CommonConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

/**
 * Property.
 */
@Entity
@Table(name = "PROPERTY")
@Getter
@Setter
public class Property extends BaseEntity implements Comparable<Property> {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Key.
     */
    @Column(name = "k", length = CommonConstants.ONE_HUNDRED)
    private String key;

    /**
     * Value.
     */
    @Column(name = "v", length = CommonConstants.ONE_THOUSAND)
    private String value;

    /**
     * Initializes an instance of <code>Address</code> with the default data.
     */
    public Property() {
        super.setCreatedAt(new Date());
        super.setUpdatedAt(new Date());
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int compareTo(final Property other) {
        if (equals(other)) {
            return 0;
        }
        return 1;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Property other = (Property) obj;
        return Objects.equals(key, other.key) && Objects.equals(value, other.value);
    }

}
