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

package org.eaa690.aerie.config;

import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.model.RateLimitRepository;
import org.eaa690.aerie.model.WeatherProductRepository;
import org.eaa690.aerie.service.PropertyService;
import org.eaa690.aerie.service.WeatherService;
import org.eaa690.aerie.ssl.SSLUtilities;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * ServiceConfig.
 */
@Configuration
public class ServiceConfig {

    /**
     * Creates a rest template with default timeout settings. The bean definition will be updated to accept timeout
     * parameters once those are part of the Customer settings.
     *
     * @param restTemplateBuilder RestTemplateBuilder
     *
     * @return Rest Template with request, read, and connection timeouts set
     */
    @Bean
    public RestTemplate restTemplate(
            final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(CommonConstants.ONE_THOUSAND))
                .setReadTimeout(Duration.ofMillis(CommonConstants.TEN_THOUSAND))
                .additionalMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    /**
     * WeatherService.
     *
     * @param restTemplate RestTemplate
     * @param propertyService PropertyService
     * @param sslUtilities SSLUtilities
     * @param wpRepository WeatherProductRepository
     * @param rlRepository RateLimitRepository
     * @return WeatherService
     */
    @Bean
    public WeatherService weatherService(
            final RestTemplate restTemplate,
            final PropertyService propertyService,
            final SSLUtilities sslUtilities,
            final WeatherProductRepository wpRepository,
            final RateLimitRepository rlRepository) {
        final WeatherService weatherService = new WeatherService();
        weatherService.setRestTemplate(restTemplate);
        weatherService.setPropertyService(propertyService);
        weatherService.setSSLUtilities(sslUtilities);
        weatherService.setWeatherProductRepository(wpRepository);
        weatherService.setRateLimitRepository(rlRepository);
        return weatherService;
    }

    /**
     * SSLUtilities.
     *
     * @return SSLUtilities
     */
    @Bean
    public SSLUtilities sslUtilities() {
        return new SSLUtilities();
    }

}