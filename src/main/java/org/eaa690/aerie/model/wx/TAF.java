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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eaa690.aerie.model.BaseEntity;

/**
 * TAF prediction.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TAF extends BaseEntity {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ICAO.
     */
    private String icao;

    /**
     * RAW Text.
     */
    @JsonProperty("raw_text")
    private String rawText;

    /**
     * Timestamp.
     */
    private Timestamp timestamp;

    /**
     * Forecast.
     */
    private List<Forecast> forecast;

    /**
     * Get ICAO.
     *
     * @return ICAO
     */
    public String getIcao() {
        return icao;
    }

    /**
     * Set ICAO.
     *
     * @param value ICAO
     */
    public void setIcao(final String value) {
        icao = value;
    }

    /**
     * Get RAW Text.
     *
     * @return RAW Text
     */
    public String getRawText() {
        return rawText;
    }

    /**
     * Set RAW Text.
     *
     * @param value RAW Text
     */
    public void setRawText(final String value) {
        rawText = value;
    }

    /**
     * Get Timestamp.
     *
     * @return Timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Set Timestamp.
     *
     * @param value Timestamp
     */
    public void setTimestamp(final Timestamp value) {
        timestamp = value;
    }

    /**
     * Get Forecast.
     *
     * @return Forecast
     */
    public List<Forecast> getForecast() {
        return forecast;
    }

    /**
     * Set Forecast.
     *
     * @param value Forecast
     */
    public void setForecast(final List<Forecast> value) {
        forecast = value;
    }

}