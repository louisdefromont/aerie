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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Coordinates object received from api.checkwx.com.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coordinates implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Decimal.
     */
    private String decimal;

    /**
     * Degrees.
     */
    private String degrees;

    /**
     * Get Decimal.
     *
     * @return decimal
     */
    public String getDecimal() {
        return decimal;
    }

    /**
     * Set Decimal.
     *
     * @param value decimal
     */
    public void setDecimal(final String value) {
        decimal = value;
    }

    /**
     * Get Degrees.
     *
     * @return degrees
     */
    public String getDegrees() {
        return degrees;
    }

    /**
     * Set Degrees.
     *
     * @param value degrees
     */
    public void setDegrees(final String value) {
        degrees = value;
    }

}