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
 * Dewpoint observation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dewpoint implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Celsius.
     */
    private Integer celsius;

    /**
     * Fahrenheit.
     */
    private Integer fahrenheit;

    /**
     * Get Celsius.
     *
     * @return celsius
     */
    public Integer getCelsius() {
        return celsius;
    }

    /**
     * Set Celsius.
     *
     * @param value celsius
     */
    public void setCelsius(final Integer value) {
        celsius = value;
    }

    /**
     * Get Fahrenheit.
     *
     * @return Fahrenheit
     */
    public Integer getFahrenheit() {
        return fahrenheit;
    }

    /**
     * Set Fahrenheit.
     *
     * @param value Fahrenheit
     */
    public void setFahrenheit(final Integer value) {
        fahrenheit = value;
    }

}