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

package org.eaa690.aerie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.model.wx.METAR;
import org.eaa690.aerie.model.wx.MetarData;
import org.eaa690.aerie.model.wx.Station;
import org.eaa690.aerie.model.wx.StationData;
import org.eaa690.aerie.model.wx.TAF;
import org.eaa690.aerie.model.wx.TafData;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.RateLimit;
import org.eaa690.aerie.model.RateLimitRepository;
import org.eaa690.aerie.model.WeatherProduct;
import org.eaa690.aerie.model.WeatherProductRepository;
import org.eaa690.aerie.ssl.SSLUtilities;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * WeatherService.
 */
public class WeatherService {

    /**
     * SimpleDateFormat.
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * Synchronous rest template.
     */
    private RestTemplate restTemplate;

    /**
     * PropertyService.
     */
    private PropertyService propertyService;

    /**
     * SSLUtilities.
     */
    private SSLUtilities sslUtilities;

    /**
     * JSON Object Serializer/Deserializer.
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(WeatherService.class);

    /**
     * WeatherProductRepository.
     */
    @Autowired
    private WeatherProductRepository weatherProductRepository;

    /**
     * Sets WeatherProductRepository.
     *
     * @param wpRepository WeatherProductRepository
     */
    @Autowired
    public void setWeatherProductRepository(final WeatherProductRepository wpRepository) {
        weatherProductRepository = wpRepository;
    }

    /**
     * RateLimitRepository.
     */
    @Autowired
    private RateLimitRepository rateLimitRepository;

    /**
     * Sets RateLimitRepository.
     *
     * @param rlRepository RateLimitRepository
     */
    @Autowired
    public void setRateLimitRepository(final RateLimitRepository rlRepository) {
        rateLimitRepository = rlRepository;
    }

    /**
     * Sets SSLUtilities.
     *
     * @param value SSLUtilities
     */
    @Autowired
    public void setSSLUtilities(final SSLUtilities value) {
        sslUtilities = value;
    }

    /**
     * Sets PropertyService.
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    /**
     * Sets RestTemplate.
     *
     * @param value RestTemplate
     */
    @Autowired
    public void setRestTemplate(final RestTemplate value) {
        restTemplate = value;
    }

    /**
     * Retrieves the current METAR for a given airport.
     *
     * @param icaoCodes for the METAR observation
     * @return list of {@link METAR}
     */
    public List<METAR> getMETARs(final List<String> icaoCodes) {
        final List<METAR> metars = new ArrayList<>();
        if (icaoCodes == null || icaoCodes.isEmpty()) {
            return metars;
        }
        icaoCodes.stream().forEach(icaoCode -> {
            try {
                metars.add(getMETAR(icaoCode));
            } catch (ResourceNotFoundException rnfe) {
                LOGGER.error("No METAR found for " + icaoCode, rnfe);
            }
        });
        return metars;
    }

    /**
     * Retrieves the current METAR for a given airport.
     *
     * @param icaoCode for the METAR observation
     * @return {@link METAR}
     * @throws ResourceNotFoundException when no information is found for the given ID
     */
    public METAR getMETAR(final String icaoCode) throws ResourceNotFoundException {
        final METAR cachedMetar = getCachedMETAR(icaoCode);
        if (cachedMetar != null
                && !expired(sdf.format(cachedMetar.getUpdatedAt()))) {
            return cachedMetar;
        }
        if (canCallCheckWX(CommonConstants.METAR_KEY + icaoCode)) {
            final METAR metar = getMETARFromCheckWXWeather(icaoCode);
            cacheMetar(icaoCode, metar);
            return metar;
        }
        if (cachedMetar != null) {
            return cachedMetar;
        }
        throw new ResourceNotFoundException(String.format("METAR information not found for %s", icaoCode));
    }

    /**
     * Retrieves the current TAF for a given airports.
     *
     * @param icaoCodes for the TAF prediction
     * @return {@link TAF}
     */
    public List<TAF> getTAFs(final List<String> icaoCodes) {
        final List<TAF> tafs = new ArrayList<>();
        if (icaoCodes == null || icaoCodes.isEmpty()) {
            return tafs;
        }
        icaoCodes.stream().forEach(icaoCode -> {
            try {
                tafs.add(getTAF(icaoCode));
            } catch (ResourceNotFoundException rnfe) {
                LOGGER.error("No TAF found for " + icaoCode, rnfe);
            }
        });
        return tafs;
    }

    /**
     * Retrieves the current TAF for a given airport.
     *
     * @param icaoCode for the TAF prediction
     * @return {@link TAF}
     * @throws ResourceNotFoundException when no information is found for the given ID
     */
    public TAF getTAF(final String icaoCode) throws ResourceNotFoundException {
        final TAF cachedTaf = getCachedTAF(icaoCode);
        if (cachedTaf != null
                && !expired(sdf.format(cachedTaf.getUpdatedAt()))) {
            return cachedTaf;
        }
        if (canCallCheckWX(CommonConstants.TAF_KEY + icaoCode)) {
            final TAF taf = getTAFFromCheckWXWeather(icaoCode);
            cacheTaf(icaoCode, taf);
            return taf;
        }
        if (cachedTaf != null) {
            return cachedTaf;
        }
        throw new ResourceNotFoundException(String.format("TAF information not found for %s", icaoCode));
    }

    /**
     * Retrieves the station for the provided airport(s).
     *
     * <p>
     * Max list size is 25 (comma separated)
     * </p>
     *
     * @param icaoCodes list of icaoCodes
     * @return list of {@link Station}
     */
    public List<Station> getStations(final List<String> icaoCodes) {
        List<Station> stations = new ArrayList<>();
        if (icaoCodes == null || icaoCodes.isEmpty()) {
            return stations;
        }
        icaoCodes.stream().forEach(icaoCode -> {
            try {
                stations.add(getStation(icaoCode));
            } catch (ResourceNotFoundException rnfe) {
                LOGGER.error("No Station found for " + icaoCode, rnfe);
            }
        });
        return stations;
    }

    /**
     * Retrieves the station for the provided airport(s).
     *
     * @param icaoCode for the station information desired
     * @return {@link Station}
     * @throws ResourceNotFoundException when no information is found for the given IDs
     */
    public Station getStation(final String icaoCode) throws ResourceNotFoundException {
        final Station cachedStation = getCachedStation(icaoCode);
        if (cachedStation != null
                && !expired(sdf.format(cachedStation.getUpdatedAt()))) {
            return cachedStation;
        }
        if (canCallCheckWX(CommonConstants.STATION_KEY + icaoCode)) {
            final Station station = getStationFromCheckWXWeather(icaoCode);
            cacheStation(icaoCode, station);
            return station;
        }
        if (cachedStation != null) {
            return cachedStation;
        }
        throw new ResourceNotFoundException(String.format("Station information not found for %s", icaoCode));
    }

    /**
     * Checks if provided station is valid.
     *
     * @param station to be validated
     * @return if station is valid
     * @throws ResourceNotFoundException when Atlanta ICAO codes property is not found
     */
    public boolean isValidStation(final String station) throws ResourceNotFoundException {
        boolean response = false;
        final List<String> validStationsList =
                Arrays
                        .asList(propertyService
                                .get(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY)
                                .getValue()
                                .split(","));
        if (validStationsList.contains(station)) {
            response = true;
        }
        return response;
    }

    /**
     * Determines if the provided observation time has expired.
     *
     * @param observed Time
     * @return if observation time has expired or not
     */
    private boolean expired(final String observed) {
        try {
            final Date oneHourAgo = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
            final Date observationDate = sdf.parse(observed);
            LOGGER.info(String.format("Comparing oneHourAgo [%s] to observationDate [%s]",
                    oneHourAgo, observationDate));
            if (observationDate.after(oneHourAgo)) {
                return false;
            }
        } catch (ParseException e) {
            LOGGER.error(String.format("Unable to parse observation date [%s]: %s", observed, e.getMessage()), e);
        }
        return true;
    }

    /**
     * Retrieves cached METAR information for provided ICAO facility.
     *
     * @param icaoCode ICAO code for weather facility (a.k.a. airport)
     * @return METAR
     */
    private METAR getCachedMETAR(final String icaoCode) {
        Optional<WeatherProduct> weatherProductOpt =
                weatherProductRepository.findByKey(CommonConstants.METAR_KEY + icaoCode);
        if (weatherProductOpt.isPresent()) {
            WeatherProduct weatherProduct = weatherProductOpt.get();
            try {
                return mapper.readValue(weatherProduct.getValue(), METAR.class);
            } catch (IOException e) {
                LOGGER.warn(String.format("Unable to deserialize METAR from cache: %s", e.getMessage()));
            }
        }
        return null;
    }

    /**
     * Retrieves cached TAF information for provided ICAO facility.
     *
     * @param icaoCode ICAO code for weather facility (a.k.a. airport)
     * @return TAF
     */
    private TAF getCachedTAF(final String icaoCode) {
        Optional<WeatherProduct> weatherProductOpt =
                weatherProductRepository.findByKey(CommonConstants.TAF_KEY + icaoCode);
        if (weatherProductOpt.isPresent()) {
            WeatherProduct weatherProduct = weatherProductOpt.get();
            try {
                return mapper.readValue(weatherProduct.getValue(), TAF.class);
            } catch (IOException e) {
                LOGGER.warn(String.format("Unable to deserialize TAF from cache: %s", e.getMessage()));
            }
        }
        return null;
    }

    /**
     * Retrieves cached Station information for provided ICAO facility.
     *
     * @param icaoCode ICAO code for weather facility (a.k.a. airport)
     * @return Station
     */
    private Station getCachedStation(final String icaoCode) {
        Optional<WeatherProduct> weatherProductOpt =
                weatherProductRepository.findByKey(CommonConstants.STATION_KEY + icaoCode);
        if (weatherProductOpt.isPresent()) {
            WeatherProduct weatherProduct = weatherProductOpt.get();
            try {
                return mapper.readValue(weatherProduct.getValue(), Station.class);
            } catch (IOException e) {
                LOGGER.warn(String.format("Unable to deserialize Station from cache: %s", e.getMessage()));
            }
        }
        return null;
    }

    /**
     * Queries CheckWX for Station information for provided ICAO facility.
     *
     * @param icaoCode ICAO code for weather facility (a.k.a. airport)
     * @return Station
     * @throws ResourceNotFoundException When station information is not found
     */
    private Station getStationFromCheckWXWeather(final String icaoCode) throws ResourceNotFoundException {
        LOGGER.info(String.format("Querying CheckWX for Station information for icao [%s]", icaoCode));
        final String url =
                propertyService.get(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY).getValue() + "/station/" + icaoCode;
        final HttpEntity<String> headers = buildCheckWXHeader();
        // Ignoring SSL certificate checking
        sslUtilities.trustAllHostnames();
        sslUtilities.trustAllHttpsCertificates();
        try {
            final ResponseEntity<StationData> data =
                    restTemplate.exchange(url, HttpMethod.GET, headers, StationData.class);
            updateRateLimiter(CommonConstants.STATION_KEY + icaoCode);

            if (data.getStatusCodeValue() >= HttpStatus.OK.value()
                    && data.getStatusCodeValue() < HttpStatus.MULTIPLE_CHOICES.value()) {
                final StationData stationData = data.getBody();
                if (stationData != null) {
                    final List<Station> stations = stationData.getData();
                    if (CollectionUtils.isNotEmpty(stations)) {
                        final Station station = stations.get(0);
                        station.setCreatedAt(new Date());
                        station.setUpdatedAt(new Date());
                        return station;
                    }
                }
            }
        } catch (RestClientException rce) {
            throw new ResourceNotFoundException(
                    String.format("Unable to retrieve Station for %s: %s", icaoCode, rce.getMessage()));
        }
        throw new ResourceNotFoundException(String.format("Station information not found for %s", icaoCode));
    }

    /**
     * Queries CheckWX for METAR information for provided ICAO facility.
     *
     * @param icaoCode ICAO code for weather facility (a.k.a. airport)
     * @return METAR
     * @throws ResourceNotFoundException When weather information is not found
     */
    private METAR getMETARFromCheckWXWeather(final String icaoCode) throws ResourceNotFoundException {
        LOGGER.info(String.format("Querying CheckWX for METAR information for icao [%s]", icaoCode));
        final String url = propertyService.get(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY).getValue() + "/metar/"
                + icaoCode + "/decoded";
        final HttpEntity<String> headers = buildCheckWXHeader();
        // Ignoring SSL certificate checking
        sslUtilities.trustAllHostnames();
        sslUtilities.trustAllHttpsCertificates();
        try {
            final ResponseEntity<MetarData> data =
                    restTemplate.exchange(url, HttpMethod.GET, headers, MetarData.class);
            updateRateLimiter(CommonConstants.METAR_KEY + icaoCode);

            if (data.getStatusCodeValue() >= HttpStatus.OK.value()
                    && data.getStatusCodeValue() < HttpStatus.MULTIPLE_CHOICES.value()) {
                final MetarData metarData = data.getBody();
                if (metarData != null) {
                    final List<METAR> metarList = metarData.getData();
                    if (CollectionUtils.isNotEmpty(metarList)) {
                        final METAR metar = metarList.get(0);
                        metar.setCreatedAt(new Date());
                        metar.setUpdatedAt(new Date());
                        return metar;
                    }
                }
            }
        } catch (RestClientException rce) {
            String msg = String.format("[RestClientException] Unable to retrieve METAR for %s: %s",
                    icaoCode, rce.getMessage());
            LOGGER.error(msg, rce);
            throw new ResourceNotFoundException(msg);
        }
        throw new ResourceNotFoundException(String.format("METAR information not found at CheckWX for %s", icaoCode));
    }

    /**
     * Queries CheckWX for TAF information for provided ICAO facility.
     *
     * @param icaoCode ICAO code for weather facility (a.k.a. airport)
     * @return TAF
     * @throws ResourceNotFoundException When weather information is not found
     */
    private TAF getTAFFromCheckWXWeather(final String icaoCode) throws ResourceNotFoundException {
        LOGGER.info(String.format("Querying CheckWX for TAF information for icao [%s]", icaoCode));
        final String url = propertyService.get(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY).getValue() + "/taf/"
                + icaoCode + "/decoded";
        final HttpEntity<String> headers = buildCheckWXHeader();
        // Ignoring SSL certificate checking
        sslUtilities.trustAllHostnames();
        sslUtilities.trustAllHttpsCertificates();
        try {
            final ResponseEntity<TafData> data =
                    restTemplate.exchange(url, HttpMethod.GET, headers, TafData.class);
            updateRateLimiter(CommonConstants.TAF_KEY + icaoCode);

            if (data.getStatusCodeValue() >= HttpStatus.OK.value()
                    && data.getStatusCodeValue() < HttpStatus.MULTIPLE_CHOICES.value()) {
                final TafData tafData = data.getBody();
                if (tafData != null) {
                    final List<TAF> tafs = tafData.getData();
                    if (CollectionUtils.isNotEmpty(tafs)) {
                        final TAF taf = tafs.get(0);
                        taf.setCreatedAt(new Date());
                        taf.setUpdatedAt(new Date());
                        return taf;
                    }
                }
            }
        } catch (RestClientException rce) {
            throw new ResourceNotFoundException(
                    String.format("Unable to retrieve TAF for %s: %s", icaoCode, rce.getMessage()));
        }
        throw new ResourceNotFoundException(String.format("TAF information not found for %s", icaoCode));
    }

    private void updateRateLimiter(final String key) {
        RateLimit rateLimit = new RateLimit();
        rateLimit.setKey(key);
        rateLimit.setCreatedAt(new Date());
        final Optional<RateLimit> rateLimitOpt = rateLimitRepository.findByKey(key);
        if (rateLimitOpt.isPresent()) {
            rateLimit = rateLimitOpt.get();
        }
        rateLimit.setValue(Duration.ofMinutes(CommonConstants.TEN).toString());
        rateLimit.setUpdatedAt(new Date());
        rateLimitRepository.save(rateLimit);
    }

    private boolean canCallCheckWX(final String key) {
        final Optional<RateLimit> rateLimitOpt = rateLimitRepository.findByKey(key);
        if (rateLimitOpt.isPresent()) {
            final RateLimit rateLimit = rateLimitOpt.get();
            final Date rateLimitDuration = Date.from(Instant.now().minus(Duration.parse(rateLimit.getValue())));
            final Date lastRun = rateLimit.getUpdatedAt();
            LOGGER.info(String.format("Comparing rateLimitDuration [%s] to lastRun [%s]", rateLimitDuration, lastRun));
            if (lastRun.after(rateLimitDuration)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builds CheckWX Header.
     *
     * @return HttpEntity with headers
     * @throws ResourceNotFoundException when CheckWX API key is not found
     */
    private HttpEntity<String> buildCheckWXHeader() throws ResourceNotFoundException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        final int hourOfDay = LocalDateTime.now().getHour();
        if (hourOfDay < CommonConstants.SIX) {
            headers.set("X-API-Key", propertyService.get(PropertyKeyConstants.CHECK_WX_API_KEY_KEY).getValue());
        } else if (hourOfDay < CommonConstants.TWELVE) {
            headers.set("X-API-Key", propertyService.get(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY).getValue());
        } else if (hourOfDay < CommonConstants.EIGHTEEN) {
            headers.set("X-API-Key", propertyService.get(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY).getValue());
        } else {
            headers.set("X-API-Key", propertyService.get(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY).getValue());
        }
        return new HttpEntity<>("parameters", headers);
    }

    /**
     * Caches METAR.
     *
     * @param icaoCode ICAO Code key for cached value
     * @param metar METAR to be cached
     */
    private void cacheMetar(final String icaoCode, final METAR metar) {
        try {
            WeatherProduct weatherProduct = new WeatherProduct();
            weatherProduct.setKey(CommonConstants.METAR_KEY + icaoCode);
            weatherProduct.setCreatedAt(new Date());
            final Optional<WeatherProduct> weatherProductOpt =
                    weatherProductRepository.findByKey(CommonConstants.METAR_KEY + icaoCode);
            if (weatherProductOpt.isPresent()) {
                weatherProduct = weatherProductOpt.get();
            }
            weatherProduct.setValue(mapper.writeValueAsString(metar));
            weatherProduct.setUpdatedAt(new Date());
            weatherProductRepository.save(weatherProduct);
        } catch (JsonProcessingException jpe) {
            LOGGER.warn(String.format("Unable to serialize METAR [%s]: %s", metar, jpe.getMessage()));
        }
    }

    /**
     * Caches TAF.
     *
     * @param icaoCode ICAO Code key for cached value
     * @param taf TAF to be cached
     */
    private void cacheTaf(final String icaoCode, final TAF taf) {
        try {
            WeatherProduct weatherProduct = new WeatherProduct();
            weatherProduct.setKey(CommonConstants.TAF_KEY + icaoCode);
            weatherProduct.setCreatedAt(new Date());
            final Optional<WeatherProduct> weatherProductOpt =
                    weatherProductRepository.findByKey(CommonConstants.TAF_KEY + icaoCode);
            if (weatherProductOpt.isPresent()) {
                weatherProduct = weatherProductOpt.get();
            }
            weatherProduct.setValue(mapper.writeValueAsString(taf));
            weatherProduct.setUpdatedAt(new Date());
            weatherProductRepository.save(weatherProduct);
        } catch (JsonProcessingException jpe) {
            LOGGER.warn(String.format("Unable to serialize TAF [%s]: %s", taf, jpe.getMessage()));
        }
    }

    /**
     * Caches Station.
     *
     * @param icaoCode ICAO Code key for cached value
     * @param station Station to be cached
     */
    private void cacheStation(final String icaoCode, final Station station) {
        try {
            WeatherProduct weatherProduct = new WeatherProduct();
            weatherProduct.setKey(CommonConstants.STATION_KEY + icaoCode);
            weatherProduct.setCreatedAt(new Date());
            final Optional<WeatherProduct> weatherProductOpt =
                    weatherProductRepository.findByKey(CommonConstants.STATION_KEY + icaoCode);
            if (weatherProductOpt.isPresent()) {
                weatherProduct = weatherProductOpt.get();
            }
            weatherProduct.setValue(mapper.writeValueAsString(station));
            weatherProduct.setUpdatedAt(new Date());
            weatherProductRepository.save(weatherProduct);
        } catch (JsonProcessingException jpe) {
            LOGGER.warn(String.format("Unable to serialize Station [%s]: %s", station, jpe.getMessage()));
        }
    }
}