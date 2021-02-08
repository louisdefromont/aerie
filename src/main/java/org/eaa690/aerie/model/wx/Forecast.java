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
import java.util.List;

/**
 * Forecast object received from api.checkwx.com.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Forecast implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ForecastTimestamp.
     */
    private ForecastTimestamp timestamp;

    /**
     * Cloud List.
     */
    private List<Cloud> clouds;

    /**
     * Visibility.
     */
    private Visibility visibility;

    /**
     * Wind.
     */
    private Wind wind;

    /**
     * Change Indicator.
     */
    @JsonProperty("change_indicator")
    private String changeIndicator;

    /**
     * Get Clouds.
     *
     * @return Clouds
     */
    public List<Cloud> getClouds() {
        return clouds;
    }

    /**
     * Set Clouds.
     *
     * @param value Clouds
     */
    public void setClouds(final List<Cloud> value) {
        clouds = value;
    }

    /**
     * Get Visibility.
     *
     * @return Visibility
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Set Visibility.
     *
     * @param value Visibility
     */
    public void setVisibility(final Visibility value) {
        visibility = value;
    }

    /**
     * Get Wind.
     *
     * @return Wind
     */
    public Wind getWind() {
        return wind;
    }

    /**
     * Set Wind.
     *
     * @param value Wind
     */
    public void setWind(final Wind value) {
        wind = value;
    }

    /**
     * Get ForecastTimestamp.
     *
     * @return ForecastTimestamp
     */
    public ForecastTimestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Set ForecastTimestamp.
     *
     * @param value ForecastTimestamp
     */
    public void setTimestamp(final ForecastTimestamp value) {
        timestamp = value;
    }

    /**
     * Get Change Indicator.
     *
     * @return Change Indicator
     */
    public String getChangeIndicator() {
        return changeIndicator;
    }

    /**
     * Set Change Indicator.
     *
     * @param value Change Indicator
     */
    public void setChangeIndicator(final String value) {
        changeIndicator = value;
    }

}