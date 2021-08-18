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
import java.util.List;

/**
 * DecodedObservation object received from api.checkwx.com.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecodedObservation implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ICAO.
     */
    private String icao;

    /**
     * ForecastTimestamp.
     */
    private ForecastTimestamp timestamp;

    /**
     * RAW Text.
     */
    private String rawText;

    /**
     * Forecast List.
     */
    private List<Forecast> forecasts;

    /**
     * Get ICAO.
     *
     * @return icao
     */
    public String getIcao() {
        return icao;
    }

    /**
     * Set ICAO.
     *
     * @param value icao
     */
    public void setIcao(final String value) {
        icao = value;
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
     * Get Forecast List.
     *
     * @return Forecast List
     */
    public List<Forecast> getForecasts() {
        return forecasts;
    }

    /**
     * Set Forecast List.
     *
     * @param value Forecast List
     */
    public void setForecasts(final List<Forecast> value) {
        forecasts = value;
    }

}
