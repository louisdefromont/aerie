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
import org.eaa690.aerie.model.BaseEntity;

/**
 * Station object received from api.checkwx.com.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Station extends BaseEntity {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ICAO.
     */
    private String icao;

    /**
     * Name.
     */
    private String name;

    /**
     * Activated.
     */
    private String activated;

    /**
     * City.
     */
    private String city;

    /**
     * Country.
     */
    private String country;

    /**
     * Radius.
     */
    private Radius radius;

    /**
     * Elevation.
     */
    private Elevation elevation;

    /**
     * IATA.
     */
    private String iata;

    /**
     * Latitude.
     */
    private Coordinates latitude;

    /**
     * Longitude.
     */
    private Coordinates longitude;

    /**
     * Magnetic.
     */
    private String magnetic;

    /**
     * Sectional.
     */
    private String sectional;

    /**
     * State.
     */
    private String state;

    /**
     * Status.
     */
    private String status;

    /**
     * Timezone.
     */
    private Timezone timezone;

    /**
     * Type.
     */
    private String type;

    /**
     * Usage.
     */
    private String useage;

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
     * Get Activated.
     *
     * @return Activated
     */
    public String getActivated() {
        return activated;
    }

    /**
     * Set Activated.
     *
     * @param value Activated
     */
    public void setActivated(final String value) {
        activated = value;
    }

    /**
     * Get City.
     *
     * @return City
     */
    public String getCity() {
        return city;
    }

    /**
     * Set City.
     *
     * @param value City
     */
    public void setCity(final String value) {
        city = value;
    }

    /**
     * Get Country.
     *
     * @return Country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Set Country.
     *
     * @param value Country
     */
    public void setCountry(final String value) {
        country = value;
    }

    /**
     * Get Radius.
     *
     * @return Radius
     */
    public Radius getRadius() {
        return radius;
    }

    /**
     * Set Radius.
     *
     * @param value Radius
     */
    public void setRadius(final Radius value) {
        radius = value;
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
     * Get IATA.
     *
     * @return IATA
     */
    public String getIata() {
        return iata;
    }

    /**
     * Set IATA.
     *
     * @param value IATA
     */
    public void setIata(final String value) {
        iata = value;
    }

    /**
     * Get Latitude.
     *
     * @return Latitude
     */
    public Coordinates getLatitude() {
        return latitude;
    }

    /**
     * Set Latitude.
     *
     * @param value Latitude
     */
    public void setLatitude(final Coordinates value) {
        latitude = value;
    }

    /**
     * Get Longitude.
     *
     * @return Longitude
     */
    public Coordinates getLongitude() {
        return longitude;
    }

    /**
     * Set Longitude.
     *
     * @param value Longitude
     */
    public void setLongitude(final Coordinates value) {
        longitude = value;
    }

    /**
     * Get Magnetic.
     *
     * @return Magnetic
     */
    public String getMagnetic() {
        return magnetic;
    }

    /**
     * Set Magnetic.
     *
     * @param value Magnetic
     */
    public void setMagnetic(final String value) {
        magnetic = value;
    }

    /**
     * Get Sectional.
     *
     * @return Sectional
     */
    public String getSectional() {
        return sectional;
    }

    /**
     * Set Sectional.
     *
     * @param value Sectional
     */
    public void setSectional(final String value) {
        sectional = value;
    }

    /**
     * Get State.
     *
     * @return State
     */
    public String getState() {
        return state;
    }

    /**
     * Set State.
     *
     * @param value State
     */
    public void setState(final String value) {
        state = value;
    }

    /**
     * Get Status.
     *
     * @return Status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set Status.
     *
     * @param value Status
     */
    public void setStatus(final String value) {
        status = value;
    }

    /**
     * Get Timezone.
     *
     * @return Timezone
     */
    public Timezone getTimezone() {
        return timezone;
    }

    /**
     * Set Timezone.
     *
     * @param value Timezone
     */
    public void setTimezone(final Timezone value) {
        timezone = value;
    }

    /**
     * Get Type.
     *
     * @return Type
     */
    public String getType() {
        return type;
    }

    /**
     * Set Type.
     *
     * @param value Type
     */
    public void setType(final String value) {
        type = value;
    }

    /**
     * Get Useage.
     *
     * @return Useage
     */
    public String getUseage() {
        return useage;
    }

    /**
     * Set Useage.
     *
     * @param value Useage
     */
    public void setUseage(final String value) {
        useage = value;
    }

}
