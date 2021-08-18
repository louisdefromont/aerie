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
 * Wind observation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Wind implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Degrees.
     */
    private Integer degrees;

    /**
     * Speed Kt.
     */
    @JsonProperty("speed_kt")
    private Integer speedKt;

    /**
     * Speed Kts.
     */
    @JsonProperty("speed_kts")
    private Integer speedKts;

    /**
     * Speed MPH.
     */
    @JsonProperty("speed_mph")
    private Integer speedMph;

    /**
     * Speed MPS.
     */
    @JsonProperty("speed_mps")
    private Integer speedMps;

    /**
     * Gust Kt.
     */
    @JsonProperty("gust_kt")
    private Integer gustKt;

    /**
     * Gust MPH.
     */
    @JsonProperty("gust_mph")
    private Integer gustMph;

    /**
     * Gust MPS.
     */
    @JsonProperty("gust_mps")
    private Integer gustMps;

    /**
     * Get Degrees.
     *
     * @return Degrees
     */
    public Integer getDegrees() {
        return degrees;
    }

    /**
     * Set Degrees.
     *
     * @param value Degrees
     */
    public void setDegrees(final Integer value) {
        degrees = value;
    }

    /**
     * Get Speed Kt.
     *
     * @return Speed Kt
     */
    public Integer getSpeedKt() {
        return speedKt;
    }

    /**
     * Set Speed Kt.
     *
     * @param value Speed Kt
     */
    public void setSpeedKt(final Integer value) {
        speedKt = value;
    }

    /**
     * Get Speed Kts.
     *
     * @return Speed Kts
     */
    public Integer getSpeedKts() {
        return speedKts;
    }

    /**
     * Set Speed Kts.
     *
     * @param value Speed Kts
     */
    public void setSpeedKts(final Integer value) {
        speedKts = value;
    }

    /**
     * Get Speed MPH.
     *
     * @return Speed MPH
     */
    public Integer getSpeedMph() {
        return speedMph;
    }

    /**
     * Set Speed MPH.
     *
     * @param value Speed MPH
     */
    public void setSpeedMph(final Integer value) {
        speedMph = value;
    }

    /**
     * Get Speed MPS.
     *
     * @return Speed MPS
     */
    public Integer getSpeedMps() {
        return speedMps;
    }

    /**
     * Set Speed MPS.
     *
     * @param value Speed MPS
     */
    public void setSpeedMps(final Integer value) {
        speedMps = value;
    }

    /**
     * Get Gust Kt.
     *
     * @return Gust Kt
     */
    public Integer getGustKt() {
        return gustKt;
    }

    /**
     * Set Gust Kt.
     *
     * @param value Gust Kt
     */
    public void setGustKt(final Integer value) {
        gustKt = value;
    }

    /**
     * Get Gust MPH.
     *
     * @return Gust MPH
     */
    public Integer getGustMph() {
        return gustMph;
    }

    /**
     * Set Gust MPH.
     *
     * @param value Gust MPH
     */
    public void setGustMph(final Integer value) {
        gustMph = value;
    }

    /**
     * Get Gust MPS.
     *
     * @return Gust MPS
     */
    public Integer getGustMps() {
        return gustMps;
    }

    /**
     * Set Gust MPS.
     *
     * @param value Gust MPS
     */
    public void setGustMps(final Integer value) {
        gustMps = value;
    }

}
