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
 * Timestamp object received from api.checkwx.com.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Timestamp implements Serializable {

    /**
     * Default SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Issued.
     */
    private String issued;

    /**
     * Bulletin.
     */
    private String bulletin;

    /**
     * Valid From.
     */
    @JsonProperty("valid_from")
    private String validFrom;

    /**
     * Valid To.
     */
    @JsonProperty("valid_to")
    private String validTo;

    /**
     * Get Issued.
     *
     * @return Issued
     */
    public String getIssued() {
        return issued;
    }

    /**
     * Set Issued.
     *
     * @param value Issued
     */
    public void setIssued(final String value) {
        issued = value;
    }

    /**
     * Get Bulletin.
     *
     * @return Bulletin
     */
    public String getBulletin() {
        return bulletin;
    }

    /**
     * Set bulletin.
     *
     * @param value Bulletin
     */
    public void setBulletin(final String value) {
        bulletin = value;
    }

    /**
     * Get Valid From.
     *
     * @return Valid From
     */
    public String getValidFrom() {
        return validFrom;
    }

    /**
     * Set Valid From.
     *
     * @param value Valid From
     */
    public void setValidFrom(final String value) {
        validFrom = value;
    }

    /**
     * Get Valid To.
     *
     * @return Valid To
     */
    public String getValidTo() {
        return validTo;
    }

    /**
     * Set Valid To.
     *
     * @param value Valid To
     */
    public void setValidTo(final String value) {
        validTo = value;
    }

}
