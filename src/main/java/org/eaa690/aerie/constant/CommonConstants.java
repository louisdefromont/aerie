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

import java.awt.Color;
import java.util.regex.Pattern;

/**
 * Common Application Constants.
 *
 * @author brianmichael
 */
public class CommonConstants {

    /**
     * ID.
     */
    public static final String ID = "ID";

    /**
     * CFI extension.
     */
    public static final String CFI = "CFI";

    /**
     * ONE_MINUTE in seconds.
     */
    public static final int ONE_MINUTE = 60;

    /**
     * ONE.
     */
    public static final int ONE = 1;

    /**
     * TWO.
     */
    public static final int TWO = 2;

    /**
     * THREE.
     */
    public static final int THREE = 3;

    /**
     * FOUR.
     */
    public static final int FOUR = 4;

    /**
     * FIVE.
     */
    public static final int FIVE = 5;

    /**
     * SIX.
     */
    public static final int SIX = 6;

    /**
     * SEVEN.
     */
    public static final int SEVEN = 7;

    /**
     * EIGHT.
     */
    public static final int EIGHT = 8;

    /**
     * NINE.
     */
    public static final int NINE = 9;

    /**
     * TEN.
     */
    public static final int TEN = 10;

    /**
     * ELEVEN.
     */
    public static final int ELEVEN = 11;

    /**
     * TWELVE.
     */
    public static final int TWELVE = 12;

    /**
     * FIFTEEN.
     */
    public static final int FIFTEEN = 15;

    /**
     * SIXTEEN.
     */
    public static final int SIXTEEN = 16;

    /**
     * EIGHTEEN.
     */
    public static final int EIGHTEEN = 18;

    /**
     * TWENTY.
     */
    public static final int TWENTY = 20;

    /**
     * TWENTY_FIVE.
     */
    public static final int TWENTY_FIVE = 25;

    /**
     * TWENTY_SEVEN.
     */
    public static final int TWENTY_SEVEN = 27;

    /**
     * THIRTY.
     */
    public static final int THIRTY = 30;

    /**
     * THIRTY_TWO.
     */
    public static final int THIRTY_TWO = 32;

    /**
     * THIRTY_SEVEN.
     */
    public static final int THIRTY_SEVEN = 37;

    /**
     * FIFTY.
     */
    public static final int FIFTY = 50;

    /**
     * NINETY.
     */
    public static final int NINETY = 90;

    /**
     * ONE_HUNDRED.
     */
    public static final int ONE_HUNDRED = 100;

    /**
     * TWO_HUNDRED.
     */
    public static final int TWO_HUNDRED = 200;

    /**
     * TWO_HUNDRED_FIFTY_FIVE.
     */
    public static final int TWO_HUNDRED_FIFTY_FIVE = 255;

    /**
     * FOUR_HUNDRED_EIGHTY.
     */
    public static final int FOUR_HUNDRED_EIGHTY = 480;

    /**
     * FIVE_HUNDRED.
     */
    public static final int FIVE_HUNDRED = 500;

    /**
     * SIX_HUNDRED_FORTY.
     */
    public static final int SIX_HUNDRED_FORTY = 640;

    /**
     * ONE_THOUSAND.
     */
    public static final int ONE_THOUSAND = 1000;

    /**
     * THREE_THOUSAND_SIX_HUNDRED.
     */
    public static final int THREE_THOUSAND_SIX_HUNDRED = 3600;

    /**
     * FOUR_THOUSAND.
     */
    public static final int FOUR_THOUSAND = 4000;

    /**
     * TEN_THOUSAND.
     */
    public static final int TEN_THOUSAND = 10000;

    /**
     * OPEN_TABLE_RECORD_TAG.
     */
    public static final String OPEN_TABLE_RECORD_TAG = "<tr>";

    /**
     * OPEN_TABLE_DATA_TAG.
     */
    public static final String OPEN_TABLE_DATA_TAG = "<td>";

    /**
     * CLOSE_TABLE_RECORD_TAG.
     */
    public static final String CLOSE_TABLE_RECORD_TAG = "</tr>";

    /**
     * CLOSE_TABLE_DATA_TAG.
     */
    public static final String CLOSE_TABLE_DATA_TAG = "</td>";

    /**
     * GREY_BACKGROUND.
     */
    public static final String GREY_BACKGROUND = "bgcolor=\"white\"";

    /**
     * NEW_LINE.
     */
    public static final String NEW_LINE = "\n";

    /**
     * TAB.
     */
    public static final String TAB = "    ";

    /**
     * TN_PATTERN.
     */
    public static final Pattern TN_PATTERN = Pattern.compile(".*\\+1(\\d{10}).*");

    /**
     * DASHBOARD.
     */
    public static final String DASHBOARD = "dashboard";

    /**
     * NOT_ANSWERED.
     */
    public static final String NOT_ANSWERED = "Not Answered";

    /**
     * ANSWERED_CORRECTLY.
     */
    public static final String ANSWERED_CORRECTLY = "Answered Correctly";

    /**
     * ANSWERED_INCORRECTLY.
     */
    public static final String ANSWERED_INCORRECTLY = "Answered Incorrectly";

    /**
     * QUESTION_NUM.
     */
    public static final String QUESTION_NUM = "Question #";

    /**
     * SANS_SERIF Font.
     */
    public static final String SANS_SERIF = "Sans-serif";

    /**
     * GREEN.
     */
    public static final Color GREEN = new Color(0, 255, 0);

    /**
     * BLUE.
     */
    public static final Color BLUE = new Color(0, 0, 255);

    /**
     * RED.
     */
    public static final Color RED = new Color(255, 0, 0);

    /**
     * TOKEN_PREFIX.
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * HEADER_STRING.
     */
    public static final String HEADER_STRING = "Authorization";

    /**
     * WEB_SOCKET_MAP.
     */
    public static final String MESSAGE_MAP = "messagemap";

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