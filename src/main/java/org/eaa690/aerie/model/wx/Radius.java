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
 * Radius object received from api.checkwx.com.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Radius implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * From.
     */
    private String from;

    /**
     * Miles.
     */
    private Double miles;

    /**
     * Meters.
     */
    private Double meters;

    /**
     * Direction.
     */
    private Integer direction;

    /**
     * Compass.
     */
    private String compass;

    /**
     * Get From.
     *
     * @return From
     */
    public String getFrom() {
        return from;
    }

    /**
     * Set From.
     *
     * @param value From
     */
    public void setFrom(final String value) {
        from = value;
    }

    /**
     * Get Miles.
     *
     * @return Miles
     */
    public Double getMiles() {
        return miles;
    }

    /**
     * Set Miles.
     *
     * @param value Miles
     */
    public void setMiles(final Double value) {
        miles = value;
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

    /**
     * Get Direction.
     *
     * @return Direction
     */
    public Integer getDirection() {
        return direction;
    }

    /**
     * Set Direction.
     *
     * @param value Direction
     */
    public void setDirection(final Integer value) {
        direction = value;
    }

    /**
     * Get Compass.
     *
     * @return Compass
     */
    public String getCompass() {
        return compass;
    }

    /**
     * Set Compass.
     *
     * @param value Compass
     */
    public void setCompass(final String value) {
        compass = value;
    }

}
