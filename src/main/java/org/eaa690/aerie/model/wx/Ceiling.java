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
     * Feet.
     */
    @JsonProperty("feet")
    private Double feet;

    /**
     * Feet AGL.
     */
    @JsonProperty("feet_agl")
    private Double feetAgl;

    /**
     * Base Feet AGL.
     */
    @JsonProperty("base_feet_agl")
    private Double baseFeetAgl;

    /**
     * Meters AGL.
     */
    @JsonProperty("meters_agl")
    private Double metersAgl;

    /**
     * Base Meters AGL.
     */
    @JsonProperty("base_meters_agl")
    private Double baseMetersAgl;

    /**
     * Meters.
     */
    @JsonProperty("meters")
    private Double meters;

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
     * Get Base Feet AGL.
     *
     * @return Base Feet AGL
     */
    public Double getBaseFeetAgl() {
        return baseFeetAgl;
    }

    /**
     * Set Base Feet AGL.
     *
     * @param value Base Feet AGL
     */
    public void setBaseFeetAgl(final Double value) {
        baseFeetAgl = value;
    }

    /**
     * Get Feet.
     *
     * @return Feet
     */
    public Double getFeet() {
        return feet;
    }

    /**
     * Set Feet.
     *
     * @param value Feet
     */
    public void setFeet(Double value) {
        this.feet = feet;
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

    /**
     * Get Base Meters AGL.
     *
     * @return Base Meters AGL
     */
    public Double getBaseMetersAgl() {
        return baseMetersAgl;
    }

    /**
     * Set Base Meters AGL.
     *
     * @param value Base Meters AGL
     */
    public void setBaseMetersAgl(final Double value) {
        baseMetersAgl = value;
    }

    /**
     * Get Meters.
     *
     * @return Meters
     */
    public Double getMeters() {
        return meters;
    }

    /**
     * Set Meters.
     *
     * @param value Meters
     */
    public void setMeters(final Double value) {
        meters = value;
    }

}