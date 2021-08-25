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

import org.eaa690.aerie.constant.CommonConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

/**
 * Rate Limit.
 */
@Entity
@Table(name = "RATE_LIMIT")
public class RateLimit extends BaseEntity implements Comparable<RateLimit> {

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
    @Column(name = "v", length = CommonConstants.TWENTY_FIVE)
    private String value;

    /**
     * Initializes an instance of <code>WeatherProduct</code> with the default data.
     */
    public RateLimit() {
        super.setCreatedAt(new Date());
        super.setUpdatedAt(new Date());
    }

    /**
     * Retrieves the value for {@link #key}.
     *
     * @return the current value
     */
    public String getKey() {
        return key;
    }

    /**
     * Provides a value for {@link #value}.
     *
     * @param k the new value to set
     */
    public void setKey(final String k) {
        key = k;
    }

    /**
     * Retrieves the value for {@link #value}.
     *
     * @return the current value
     */
    public String getValue() {
        return value;
    }

    /**
     * Provides a value for {@link #value}.
     *
     * @param v the new value to set
     */
    public void setValue(final String v) {
        value = v;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int compareTo(final RateLimit other) {
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
        final RateLimit other = (RateLimit) obj;
        return Objects.equals(key, other.key)
                && Objects.equals(value, other.value);
    }

}
