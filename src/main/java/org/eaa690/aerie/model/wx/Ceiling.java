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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Ceiling.
 */
public class Ceiling implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Code.
     */
    private String code;

    /**
     * Text.
     */
    private String text;

    /**
     * Feet AGL.
     */
    @JsonProperty("feet_agl")
    private Double feetAgl;

    /**
     * Meters AGL.
     */
    @JsonProperty("meters_agl")
    private Double metersAgl;

    /**
     * Get code.
     *
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * Set code.
     *
     * @param value code
     */
    public void setCode(final String value) {
        code = value;
    }

    /**
     * Get text.
     *
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * Set text.
     *
     * @param value text
     */
    public void setText(final String value) {
        text = value;
    }

    /**
     * Get Feet AGL.
     *
     * @return Feet AGL
     */
    public Double getFeetAgl() {
        return feetAgl;
    }

    /**
     * Set Feet AGL.
     *
     * @param value Feet AGL
     */
    public void setFeetAgl(final Double value) {
        feetAgl = value;
    }

    /**
     * Get Meters AGL.
     *
     * @return Meters AGL
     */
    public Double getMetersAgl() {
        return metersAgl;
    }

    /**
     * Set Meters AGL.
     *
     * @param value Meters AGL
     */
    public void setMetersAgl(final Double value) {
        metersAgl = value;
    }

}