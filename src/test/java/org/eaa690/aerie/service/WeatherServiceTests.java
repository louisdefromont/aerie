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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.eaa690.aerie.TestDataFactory;
import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.controller.WeatherControllerTests;
import org.eaa690.aerie.model.wx.*;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Property;
import org.eaa690.aerie.model.RateLimitRepository;
import org.eaa690.aerie.model.WeatherProduct;
import org.eaa690.aerie.model.WeatherProductRepository;
import org.eaa690.aerie.ssl.SSLUtilities;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * WeatherServiceTests.
 */
public class WeatherServiceTests {

    /**
     * Key.
     */
    public static final String KEY = "???###???#?#?#";

    /**
     * ICAO_CODE.
     */
    public static final String ICAO_CODE = "KATL";

    /**
     * INVALID_ICAO_CODE.
     */
    public static final String INVALID_ICAO_CODE = "KBSM";

    /**
     * PropertyService.
     */
    @Mock
    private PropertyService propertyService;

    /**
     * WeatherProductRepository.
     */
    @Mock
    private WeatherProductRepository wpRepository;

    /**
     * RateLimitRepository.
     */
    @Mock
    private RateLimitRepository rlRepository;

    /**
     * RestTemplate.
     */
    @Mock
    private RestTemplate restTemplate;

    /**
     * SSLUtilities.
     */
    @Mock
    private SSLUtilities sslUtilities;

    /**
     * JSON Object Serializer/Deserializer.
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Faker.
     */
    private Faker faker = new Faker();

    /**
     * WeatherService.
     */
    private WeatherService weatherService;

    /**
     * CheckWX API Key Property.
     */
    private Property checkWXApiKeyProperty;

    /**
     * CheckWX URL Base Property.
     */
    private Property checkWXUrlBaseProperty;

    /**
     * Atlanta area ICAO codes property.
     */
    private Property atlantaICAOCodesProperty;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        MockitoAnnotations.initMocks(this);

        checkWXApiKeyProperty =
                TestDataFactory.getProperty(PropertyKeyConstants.CHECK_WX_API_KEY_KEY, faker.bothify(KEY));

        Mockito
                .doReturn(checkWXApiKeyProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .doReturn(checkWXApiKeyProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .doReturn(checkWXApiKeyProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .doReturn(checkWXApiKeyProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));

        checkWXUrlBaseProperty =
                TestDataFactory.getProperty(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY, faker.internet().url());

        Mockito
                .doReturn(checkWXUrlBaseProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .doReturn(buildMetarDataResponseEntity())
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));
        Mockito
                .doReturn(buildTafDataResponseEntity())
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));
        Mockito
                .doReturn(buildStationDataResponseEntity())
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));

        weatherService = new WeatherService();
        weatherService.setRestTemplate(restTemplate);
        weatherService.setPropertyService(propertyService);
        weatherService.setSSLUtilities(sslUtilities);
        weatherService.setWeatherProductRepository(wpRepository);
        weatherService.setRateLimitRepository(rlRepository);
    }

    /**
     * Test retrieval of a METAR from cache.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getMETARFromCache() throws Exception {
        final METAR metar = new METAR();
        metar.setIcao(ICAO_CODE);
        metar.setUpdatedAt(new Date());
        WeatherProduct weatherProduct = new WeatherProduct();
        weatherProduct.setKey(CommonConstants.METAR_KEY + ICAO_CODE);
        weatherProduct.setValue(mapper.writeValueAsString(metar));
        Mockito.doReturn(Optional.of(weatherProduct)).when(wpRepository).findByKey(ArgumentMatchers.anyString());

        Assert.assertNotNull(weatherService.getMETAR(ICAO_CODE));

        Mockito.verify(wpRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verifyNoInteractions(propertyService, restTemplate, sslUtilities);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a METAR.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getMETAR() throws Exception {
        final METAR metar = weatherService.getMETAR(ICAO_CODE);

        Assert.assertNotNull(metar);
        Assert.assertEquals("ICAO codes are not the same.", ICAO_CODE, metar.getIcao());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);

        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(wpRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getMETARs() throws Exception {
        final List<METAR> metars = weatherService.getMETARs(Arrays.asList(ICAO_CODE));

        Assert.assertNotNull(metars);
        Assert.assertEquals("Unexpected number of METARs returned.", 1, metars.size());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);

        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(wpRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getMETARsNullList() throws Exception {
        final List<METAR> metars = weatherService.getMETARs(null);

        Assert.assertNotNull(metars);
        Assert.assertEquals("Unexpected number of METARs returned.", 0, metars.size());

        Mockito
                .verifyNoInteractions(
                        sslUtilities,
                        restTemplate,
                        propertyService);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getMETARsEmptyList() throws Exception {
        final List<METAR> metars = weatherService.getMETARs(new ArrayList<>());

        Assert.assertNotNull(metars);
        Assert.assertEquals("Unexpected number of METARs returned.", 0, metars.size());

        Mockito
                .verifyNoInteractions(
                        sslUtilities,
                        restTemplate,
                        propertyService);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getMETARsNotFound() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));

        final List<METAR> metars = weatherService.getMETARs(Arrays.asList(ICAO_CODE));

        Assert.assertNotNull(metars);
        Assert.assertEquals("Unexpected number of METARs returned.", 0, metars.size());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a METAR.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getMETARNotFound() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));

        weatherService.getMETAR(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a METAR.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getMETARDataInvalid() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.OK))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));

        weatherService.getMETAR(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a METAR.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getMETAREmptyList() throws Exception {
        Mockito
                .doReturn(buildMetarDataEmptyListResponseEntity())
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));

        weatherService.getMETAR(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a METAR.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getMETARRestClientException() throws Exception {
        Mockito
                .doThrow(new RestClientException(""))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));

        weatherService.getMETAR(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(MetarData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a TAF from cache.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getTAFFromCache() throws Exception {
        final TAF taf = new TAF();
        taf.setIcao(ICAO_CODE);
        taf.setUpdatedAt(new Date());
        WeatherProduct weatherProduct = new WeatherProduct();
        weatherProduct.setKey(CommonConstants.TAF_KEY + ICAO_CODE);
        weatherProduct.setValue(mapper.writeValueAsString(taf));
        Mockito.doReturn(Optional.of(weatherProduct)).when(wpRepository).findByKey(ArgumentMatchers.anyString());

        Assert.assertNotNull(weatherService.getTAF(ICAO_CODE));

        Mockito.verify(wpRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verifyNoInteractions(propertyService, restTemplate, sslUtilities);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a TAF.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getTAF() throws Exception {
        final TAF taf = weatherService.getTAF(ICAO_CODE);

        Assert.assertNotNull(taf);
        Assert.assertEquals("ICAO codes are not the same.", ICAO_CODE, taf.getIcao());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);

        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(wpRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getTAFs() throws Exception {
        final List<TAF> tafs = weatherService.getTAFs(Arrays.asList(ICAO_CODE));

        Assert.assertNotNull(tafs);
        Assert.assertEquals("Unexpected number of TAFs returned.", 1, tafs.size());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(wpRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getTAFsNullList() throws Exception {
        final List<TAF> tafs = weatherService.getTAFs(null);

        Assert.assertNotNull(tafs);
        Assert.assertEquals("Unexpected number of TAFs returned.", 0, tafs.size());

        Mockito
                .verifyNoInteractions(
                        sslUtilities,
                        restTemplate,
                        propertyService);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getTAFsEmptyList() throws Exception {
        final List<TAF> tafs = weatherService.getTAFs(new ArrayList<>());

        Assert.assertNotNull(tafs);
        Assert.assertEquals("Unexpected number of TAFs returned.", 0, tafs.size());

        Mockito
                .verifyNoInteractions(
                        sslUtilities,
                        restTemplate,
                        propertyService);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getTAFsNotFound() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));

        final List<TAF> tafs = weatherService.getTAFs(Arrays.asList(ICAO_CODE));

        Assert.assertNotNull(tafs);
        Assert.assertEquals("Unexpected number of TAFs returned.", 0, tafs.size());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a TAF.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getTAFNotFound() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));

        weatherService.getTAF(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a TAF.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getTAFDataInvalid() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.OK))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));

        weatherService.getTAF(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a TAF.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getTAFEmptyList() throws Exception {
        Mockito
                .doReturn(buildTafDataEmptyListResponseEntity())
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));

        weatherService.getTAF(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a TAF.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getTAFRestClientException() throws Exception {
        Mockito
                .doThrow(new RestClientException(""))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));

        weatherService.getTAF(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(TafData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a Station from cache.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getStationFromCache() throws Exception {
        final Station station = new Station();
        station.setIcao(ICAO_CODE);
        station.setUpdatedAt(new Date());
        WeatherProduct weatherProduct = new WeatherProduct();
        weatherProduct.setKey(CommonConstants.STATION_KEY + ICAO_CODE);
        weatherProduct.setValue(mapper.writeValueAsString(station));
        Mockito.doReturn(Optional.of(weatherProduct)).when(wpRepository).findByKey(ArgumentMatchers.anyString());

        Assert.assertNotNull(weatherService.getStation(ICAO_CODE));

        Mockito.verify(wpRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verifyNoInteractions(propertyService, restTemplate, sslUtilities);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a Station.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getStation() throws Exception {
        final Station station = weatherService.getStation(ICAO_CODE);

        Assert.assertNotNull(station);
        Assert.assertEquals("ICAO codes are not the same.", ICAO_CODE, station.getIcao());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(wpRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getStations() throws Exception {
        final List<Station> stations = weatherService.getStations(Arrays.asList(ICAO_CODE));

        Assert.assertNotNull(stations);
        Assert.assertEquals("Unexpected number of Stations returned.", 1, stations.size());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);

        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(wpRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getStationsNullList() throws Exception {
        final List<Station> stations = weatherService.getStations(null);

        Assert.assertNotNull(stations);
        Assert.assertEquals("Unexpected number of Stations returned.", 0, stations.size());

        Mockito
                .verifyNoInteractions(
                        sslUtilities,
                        restTemplate,
                        propertyService);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getStationsEmptyList() throws Exception {
        final List<Station> stations = weatherService.getStations(new ArrayList<>());

        Assert.assertNotNull(stations);
        Assert.assertEquals("Unexpected number of Stations returned.", 0, stations.size());

        Mockito
                .verifyNoInteractions(
                        sslUtilities,
                        restTemplate,
                        propertyService);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a list of Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getStationsNotFound() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));

        final List<Station> stations = weatherService.getStations(Arrays.asList(ICAO_CODE));

        Assert.assertNotNull(stations);
        Assert.assertEquals("Unexpected number of Stations returned.", 0, stations.size());

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_2_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_3_KEY));
        Mockito
                .verify(propertyService, Mockito.atMostOnce())
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_4_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);

        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verify(wpRepository, Mockito.times(1)).findByKey(ArgumentMatchers.anyString());
        Mockito.verifyNoMoreInteractions(wpRepository);

        Mockito.verify(rlRepository, Mockito.times(2)).findByKey(ArgumentMatchers.anyString());
        Mockito.verify(rlRepository, Mockito.times(1)).save(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(rlRepository);
    }

    /**
     * Test retrieval of a Station.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getStationNotFound() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));

        weatherService.getStation(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a Station.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getStationDataInvalid() throws Exception {
        Mockito
                .doReturn(new ResponseEntity<>(null, HttpStatus.OK))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));

        weatherService.getStation(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a Station.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getStationEmptyList() throws Exception {
        Mockito
                .doReturn(buildStationDataEmptyListResponseEntity())
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));

        weatherService.getStation(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test retrieval of a Station.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = ResourceNotFoundException.class)
    public void getStationRestClientException() throws Exception {
        Mockito
                .doThrow(new RestClientException(""))
                .when(restTemplate)
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));

        weatherService.getStation(ICAO_CODE);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_URL_BASE_KEY));
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.CHECK_WX_API_KEY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito
                .verify(restTemplate, Mockito.times(1))
                .exchange(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.eq(HttpMethod.GET),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.eq(StationData.class));
        Mockito.verifyNoMoreInteractions(restTemplate);
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHostnames();
        Mockito.verify(sslUtilities, Mockito.times(1)).trustAllHttpsCertificates();
        Mockito.verifyNoMoreInteractions(sslUtilities);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test isStationValid.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void isStationValid() throws Exception {
        atlantaICAOCodesProperty = TestDataFactory
                .getProperty(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY,
                        WeatherControllerTests.ATLANTA_CODES);

        Mockito
                .doReturn(atlantaICAOCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));

        final boolean valid = weatherService.isValidStation(ICAO_CODE);

        Assert.assertTrue(valid);

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito.verifyNoInteractions(sslUtilities, restTemplate);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test isStationValid.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void isStationValidNullStation() throws Exception {
        atlantaICAOCodesProperty = TestDataFactory
                .getProperty(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY,
                        WeatherControllerTests.ATLANTA_CODES);

        Mockito
                .doReturn(atlantaICAOCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));

        final boolean valid = weatherService.isValidStation(null);

        Assert.assertFalse(valid);
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito.verifyNoInteractions(sslUtilities, restTemplate);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Test isStationValid.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void isStationValidInvalidStation() throws Exception {
        atlantaICAOCodesProperty = TestDataFactory
                .getProperty(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY,
                        WeatherControllerTests.ATLANTA_CODES);

        Mockito
                .doReturn(atlantaICAOCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));

        final boolean valid = weatherService.isValidStation(INVALID_ICAO_CODE);

        Assert.assertFalse(valid);
        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito.verifyNoInteractions(sslUtilities, restTemplate);

        Mockito.verifyNoInteractions(wpRepository);

        Mockito.verifyNoInteractions(rlRepository);
    }

    /**
     * Builds a ResponseEntity<MetarData> for use in testing.
     *
     * @return ResponseEntity<MetarData>
     */
    private static ResponseEntity<MetarData> buildMetarDataResponseEntity() {
        final MetarData metarData = new MetarData();
        final METAR metar = new METAR();
        metar.setIcao(ICAO_CODE);
        metarData.setData(Arrays.asList(metar));
        metarData.setResults(Integer.valueOf(1));

        return new ResponseEntity<>(metarData, HttpStatus.OK);
    }

    /**
     * Builds a ResponseEntity<MetarData> for use in testing.
     *
     * @return ResponseEntity<MetarData>
     */
    private static ResponseEntity<MetarData> buildMetarDataEmptyListResponseEntity() {
        final MetarData metarData = new MetarData();
        metarData.setResults(Integer.valueOf(0));

        return new ResponseEntity<>(metarData, HttpStatus.OK);
    }

    /**
     * Builds a ResponseEntity<TafData> for use in testing.
     *
     * @return ResponseEntity<TafData>
     */
    private static ResponseEntity<TafData> buildTafDataResponseEntity() {
        final TafData tafData = new TafData();
        final TAF taf = new TAF();
        taf.setIcao(ICAO_CODE);
        tafData.setData(Arrays.asList(taf));
        tafData.setResults(Integer.valueOf(1));

        return new ResponseEntity<>(tafData, HttpStatus.OK);
    }

    /**
     * Builds a ResponseEntity<TafData> for use in testing.
     *
     * @return ResponseEntity<TafData>
     */
    private static ResponseEntity<TafData> buildTafDataEmptyListResponseEntity() {
        final TafData tafData = new TafData();
        tafData.setResults(Integer.valueOf(0));

        return new ResponseEntity<>(tafData, HttpStatus.OK);
    }

    /**
     * Builds a ResponseEntity<StationData> for use in testing.
     *
     * @return ResponseEntity<StationData>
     */
    private static ResponseEntity<StationData> buildStationDataResponseEntity() {
        final StationData stationData = new StationData();
        final Station station = new Station();
        station.setIcao(ICAO_CODE);
        stationData.setData(Arrays.asList(station));
        stationData.setResults(Integer.valueOf(1));

        return new ResponseEntity<>(stationData, HttpStatus.OK);
    }

    /**
     * Builds a ResponseEntity<StationData> for use in testing.
     *
     * @return ResponseEntity<StationData>
     */
    private static ResponseEntity<StationData> buildStationDataEmptyListResponseEntity() {
        final StationData stationData = new StationData();
        stationData.setResults(Integer.valueOf(0));

        return new ResponseEntity<>(stationData, HttpStatus.OK);
    }

}