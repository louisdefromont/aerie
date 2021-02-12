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

package org.eaa690.aerie.constant;

import java.util.regex.Pattern;

/**
 * Common Application Constants.
 */
public class CommonConstants {

    /**
     * ID.
     */
    public static final String ID = "ID";

    /**
     * TN_PATTERN.
     */
    public static final Pattern TN_PATTERN = Pattern.compile(".*\\+1(\\d{10}).*");

    /**
     * TEN.
     */
    public static final int TEN = 10;

    /**
     * TWENTY_FIVE.
     */
    public static final int TWENTY_FIVE = 25;

    /**
     * ONE_HUNDRED.
     */
    public static final int ONE_HUNDRED = 100;

    /**
     * ONE_THOUSAND.
     */
    public static final int ONE_THOUSAND = 1000;

    /**
     * TEN_THOUSAND.
     */
    public static final int TEN_THOUSAND = 10000;

    /**
     * METAR Key.
     */
    public static final String METAR_KEY = "METAR_";

    /**
     * TAF Key.
     */
    public static final String TAF_KEY = "TAF_";

    /**
     * Station Key.
     */
    public static final String STATION_KEY = "STATION_";
}