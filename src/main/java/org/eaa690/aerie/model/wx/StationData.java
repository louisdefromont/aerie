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
 * Coordinates object received from api.checkwx.com.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StationData implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Results.
     */
    private Integer results;

    /**
     * Data.
     */
    private List<Station> data;

    /**
     * Error.
     */
    private String error;

    /**
     * Initializes an instance of <code>StationData</code> with the default data.
     */
    public StationData() {
        // Default constructor
    }

    /**
     * Initializes an instance of <code>StationData</code> with the default data.
     *
     * @param value Error
     */
    public StationData(final String value) {
        error = value;
    }

    /**
     * Get Results.
     *
     * @return Results
     */
    public Integer getResults() {
        return results;
    }

    /**
     * Set Results.
     *
     * @param value Results
     */
    public void setResults(final Integer value) {
        results = value;
    }

    /**
     * Get Data.
     *
     * @return Data
     */
    public List<Station> getData() {
        return data;
    }

    /**
     * Set Data.
     *
     * @param value Data
     */
    public void setData(final List<Station> value) {
        data = value;
    }

    /**
     * Get Error.
     *
     * @return Error
     */
    public String getError() {
        return error;
    }

    /**
     * Set Error.
     *
     * @param value Error
     */
    public void setError(final String value) {
        error = value;
    }

}
