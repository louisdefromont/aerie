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

package org.eaa690.aerie;

import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.model.Property;
import org.eaa690.aerie.model.WeatherProduct;

/**
 * TestDataFactory.
 */
public class TestDataFactory {

    /**
     * Initializes an instance of <code>TestDataFactory</code> with the default data.
     */
    private TestDataFactory() {
        // private constructor
    }

    /**
     * Builds a Property for testing.
     *
     * @param key Key
     * @param value Value
     * @return Property
     */
    public static Property getProperty(final String key, final String value) {
        final Property property = new Property();
        property.setId(Long.valueOf(1L));
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    /**
     * Builds a WeatherProduct for testing.
     *
     * @return WeatherProduct
     */
    public static WeatherProduct getWeatherProduct() {
        final WeatherProduct weatherProduct = new WeatherProduct();
        weatherProduct.setId(Long.valueOf(1L));
        weatherProduct.setKey(CommonConstants.ID);
        weatherProduct.setValue(CommonConstants.ID);
        return weatherProduct;
    }

}