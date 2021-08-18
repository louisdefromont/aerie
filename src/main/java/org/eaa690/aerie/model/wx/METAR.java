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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eaa690.aerie.model.BaseEntity;

import java.util.List;

/**
 * METAR observation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class METAR extends BaseEntity {

    /**
     * ICAO.
     */
    public static final String ICAO = "icao";

    /**
     * NAME.
     */
    public static final String NAME = "name";

    /**
     * OBSERVED.
     */
    public static final String OBSERVED = "observed";

    /**
     * RAW_TEXT.
     */
    public static final String RAW_TEXT = "raw_text";

    /**
     * BAROMETER.
     */
    public static final String BAROMETER = "barometer";

    /**
     * CEILING.
     */
    public static final String CEILING = "ceiling";

    /**
     * CLOUDS.
     */
    public static final String CLOUDS = "clouds";

    /**
     * DEWPOINT.
     */
    public static final String DEWPOINT = "dewpoint";

    /**
     * ELEVATION.
     */
    public static final String ELEVATION = "elevation";

    /**
     * FLIGHT_CATEGORY.
     */
    public static final String FLIGHT_CATEGORY = "flight_category";

    /**
     * HUMIDITY_PERCENT.
     */
    public static final String HUMIDITY_PERCENT = "humidity_percent";

    /**
     * TEMPERATURE.
     */
    public static final String TEMPERATURE = "temperature";

    /**
     * VISIBILITY.
     */
    public static final String VISIBILITY = "visibility";

    /**
     * WIND.
     */
    public static final String WIND = "wind";

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ICAO Code.
     */
    private String icao;

    /**
     * Name.
     */
    private String name;

    /**
     * Observed.
     */
    private String observed;

    /**
     * Raw Text.
     */
    @JsonProperty("raw_text")
    private String rawText;

    /**
     * Barometer.
     */
    private Barometer barometer;

    /**
     * Ceiling.
     */
    private Ceiling ceiling;

    /**
     * list of Clouds.
     */
    private List<Cloud> clouds;

    /**
     * Dewpoint.
     */
    private Dewpoint dewpoint;

    /**
     * Elevation.
     */
    private Elevation elevation;

    /**
     * Flight Category.
     */
    @JsonProperty("flight_category")
    private String flightCategory;

    /**
     * Humidity Percent.
     */
    @JsonProperty("humidity_percent")
    private String humidityPercent;

    /**
     * Temperature.
     */
    private Temperature temperature;

    /**
     * Visibility.
     */
    private Visibility visibility;

    /**
     * Wind.
     */
    private Wind wind;

    /**
     * Get ICAO Code.
     *
     * @return ICAO Code
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
     * Get Name.
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set Name.
     *
     * @param value Name
     */
    public void setName(final String value) {
        name = value;
    }

    /**
     * Get Observed.
     *
     * @return Observed
     */
    public String getObserved() {
        return observed;
    }

    /**
     * Set Observed.
     *
     * @param value Observed
     */
    public void setObserved(final String value) {
        observed = value;
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
     * Get Barometer.
     *
     * @return Barometer
     */
    public Barometer getBarometer() {
        return barometer;
    }

    /**
     * Set Barometer.
     *
     * @param value Barometer
     */
    public void setBarometer(final Barometer value) {
        barometer = value;
    }

    /**
     * Get Ceiling.
     *
     * @return Ceiling
     */
    public Ceiling getCeiling() {
        return ceiling;
    }

    /**
     * Set Ceiling.
     *
     * @param value Ceiling
     */
    public void setCeiling(final Ceiling value) {
        ceiling = value;
    }

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
     * Dewpoint.
     *
     * @return Dewpoint
     */
    public Dewpoint getDewpoint() {
        return dewpoint;
    }

    /**
     * Set Dewpoint.
     *
     * @param value Dewpoint
     */
    public void setDewpoint(final Dewpoint value) {
        dewpoint = value;
    }

    /**
     * Get Elevation.
     *
     * @return Elevation
     */
    public Elevation getElevation() {
        return elevation;
    }

    /**
     * Set Elevation.
     *
     * @param value Elevation
     */
    public void setElevation(final Elevation value) {
        elevation = value;
    }

    /**
     * Get Flight Category.
     *
     * @return Flight Category
     */
    public String getFlightCategory() {
        return flightCategory;
    }

    /**
     * Set Flight Category.
     *
     * @param value Flight Category
     */
    public void setFlightCategory(final String value) {
        flightCategory = value;
    }

    /**
     * Get Humidity Percent.
     *
     * @return Humidity Percent
     */
    public String getHumidityPercent() {
        return humidityPercent;
    }

    /**
     * Set Humidity Percent.
     *
     * @param value Humidity Percent
     */
    public void setHumidityPercent(final String value) {
        humidityPercent = value;
    }

    /**
     * Set Humidity Percent from integer.
     *
     * @param value Humidity Percent
     */
    public void setHumidityPercent(final int value) {
        humidityPercent = String.valueOf(value);
    }

    /**
     * Get Temperature.
     *
     * @return Temperature
     */
    public Temperature getTemperature() {
        return temperature;
    }

    /**
     * Set Temperature.
     *
     * @param value Temperature
     */
    public void setTemperature(final Temperature value) {
        temperature = value;
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

}
