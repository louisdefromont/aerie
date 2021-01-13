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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Cloud observation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cloud implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Base Feet AGL.
     */
    @JsonProperty("base_feet_agl")
    private Double baseFeetAgl;

    /**
     * Base Meters AGL.
     */
    @JsonProperty("base_meters_agl")
    private Double baseMetersAgl;

    /**
     * Code.
     */
    private String code;

    /**
     * Text.
     */
    private String text;

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
     * Get Code.
     *
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * Set Code.
     *
     * @param value code
     */
    public void setCode(final String value) {
        code = value;
    }

    /**
     * Get Text.
     *
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * Set Text.
     *
     * @param value text
     */
    public void setText(final String value) {
        text = value;
    }

}