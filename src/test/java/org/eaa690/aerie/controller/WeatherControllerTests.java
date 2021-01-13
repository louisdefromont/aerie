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

package org.eaa690.aerie.controller;

import org.eaa690.aerie.TestDataFactory;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.model.wx.METAR;
import org.eaa690.aerie.model.wx.Station;
import org.eaa690.aerie.model.wx.TAF;
import org.eaa690.aerie.exception.InvalidPayloadException;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Property;
import org.eaa690.aerie.service.PropertyService;
import org.eaa690.aerie.service.WeatherService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * WeatherControllerTests.
 */
public class WeatherControllerTests {

    /**
     * Atlanta ICAO Codes.
     */
    public static final String ATLANTA_CODES = "KATL,KLZU,KRYY,KFTY";

    /**
     * ICAO Code.
     */
    public static final String ICAO_CODE = "KATL";

    /**
     * Raw Text.
     */
    public static final String RAW_TEXT = "KATL 252055Z AUTO 26003KT 10SM CLR 16/08 A2986 RMK AO2 T01640082";

    /**
     * Flight Category.
     */
    public static final String FLIGHT_CATEGORY = "VFR";

    /**
     * Invalid ICAO Code.
     */
    public static final String INVALID_ICAO_CODE = "KBSM";

    /**
     * PropertyService.
     */
    @Mock
    private PropertyService propertyService;

    /**
     * WeatherService.
     */
    @Mock
    private WeatherService weatherService;

    /**
     * WeatherController.
     */
    private WeatherController weatherController;

    /**
     * Atlanta ICO Codes Property.
     */
    private Property atlantaIcaoCodesProperty;

    /**
     * Test setup.
     *
     * @throws ResourceNotFoundException when a test error occurs
     */
    @Before
    public void before() throws ResourceNotFoundException {
        MockitoAnnotations.initMocks(this);

        atlantaIcaoCodesProperty =
                TestDataFactory.getProperty(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY, ATLANTA_CODES);

        weatherController = new WeatherController();
        weatherController.setWeatherService(weatherService);
        weatherController.setPropertyService(propertyService);
    }

    /**
     * Test empty constructor.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void emptyConstructor() throws Exception {
        Mockito
                .doReturn(atlantaIcaoCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));
        Mockito.doReturn(new ArrayList<METAR>()).when(weatherService).getMETARs(ArgumentMatchers.any());

        Assert.assertNotNull(new WeatherController());
    }

    /**
     * Test retrieval of Atlanta area METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getAtlantaMETARs() throws Exception {
        Mockito
                .doReturn(atlantaIcaoCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));
        final METAR metar = new METAR();
        metar.setIcao(ICAO_CODE);
        metar.setRawText(RAW_TEXT);
        metar.setFlightCategory(FLIGHT_CATEGORY);
        Mockito.doReturn(Arrays.asList(metar)).when(weatherService).getMETARs(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.metar(WeatherController.ATLANTA, null));

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito.verify(weatherService, Mockito.times(1)).getMETARs(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
    }

    /**
     * Test retrieval of Atlanta area METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getAtlantaSectionalMETARs() throws Exception {
        Mockito
                .doReturn(atlantaIcaoCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        final METAR metar = new METAR();
        metar.setIcao(ICAO_CODE);
        metar.setRawText(RAW_TEXT);
        metar.setFlightCategory(FLIGHT_CATEGORY);
        Mockito.doReturn(Arrays.asList(metar)).when(weatherService).getMETARs(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.metar(WeatherController.ATLANTA_SECTIONAL, null));

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito.verify(weatherService, Mockito.times(1)).getMETARs(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
    }

    /**
     * Test retrieval of Atlanta area METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getMETAR() throws Exception {
        final METAR metar = new METAR();
        metar.setIcao(ICAO_CODE);
        Mockito.doReturn(metar).when(weatherService).getMETAR(ArgumentMatchers.any());
        Mockito.doReturn(Boolean.TRUE).when(weatherService).isValidStation(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.metar(ICAO_CODE, null));

        Mockito.verify(weatherService, Mockito.times(1)).getMETAR(ArgumentMatchers.any());
        Mockito.verify(weatherService, Mockito.times(1)).isValidStation(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
        Mockito.verifyNoInteractions(propertyService);
    }

    /**
     * Test retrieval of Atlanta area METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getMETARWithDataSpecified() throws Exception {
        final METAR metar = new METAR();
        metar.setIcao(ICAO_CODE);
        Mockito.doReturn(metar).when(weatherService).getMETAR(ArgumentMatchers.any());
        Mockito.doReturn(Boolean.TRUE).when(weatherService).isValidStation(ArgumentMatchers.any());

        final List<String> dataList = new ArrayList<>();
        dataList.add(METAR.BAROMETER);
        dataList.add(METAR.CEILING);
        dataList.add(METAR.CLOUDS);
        dataList.add(METAR.DEWPOINT);
        dataList.add(METAR.ELEVATION);
        dataList.add(METAR.FLIGHT_CATEGORY);
        dataList.add(METAR.HUMIDITY_PERCENT);
        dataList.add(METAR.ICAO);
        dataList.add(METAR.NAME);
        dataList.add(METAR.OBSERVED);
        dataList.add(METAR.RAW_TEXT);
        dataList.add(METAR.TEMPERATURE);
        dataList.add(METAR.VISIBILITY);
        dataList.add(METAR.WIND);
        Assert.assertNotNull(weatherController.metar(ICAO_CODE, dataList));

        Mockito.verify(weatherService, Mockito.times(1)).getMETAR(ArgumentMatchers.any());
        Mockito.verify(weatherService, Mockito.times(1)).isValidStation(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
        Mockito.verifyNoInteractions(propertyService);
    }

    /**
     * Test retrieval of Atlanta area METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = InvalidPayloadException.class)
    public void getMETARInvalidStation() throws Exception {
        Mockito.doReturn(Boolean.FALSE).when(weatherService).isValidStation(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.metar(INVALID_ICAO_CODE, null));

        Mockito.verify(weatherService, Mockito.times(1)).isValidStation(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
        Mockito.verifyNoInteractions(propertyService);
    }

    /**
     * Test retrieval of Atlanta area METARs.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = InvalidPayloadException.class)
    public void getMETARNullStation() throws Exception {
        Assert.assertNotNull(weatherController.metar(null, null));

        Mockito.verifyNoInteractions(propertyService, weatherService);
    }

    /**
     * Test retrieval of Atlanta area TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getAtlantaTAFs() throws Exception {
        Mockito
                .doReturn(atlantaIcaoCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));
        Mockito.doReturn(new ArrayList<TAF>()).when(weatherService).getTAFs(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.taf(WeatherController.ATLANTA));

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito.verify(weatherService, Mockito.times(1)).getTAFs(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
    }

    /**
     * Test retrieval of Atlanta area TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getAtlantaSectionalTAFs() throws Exception {
        Mockito
                .doReturn(atlantaIcaoCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        Mockito.doReturn(new ArrayList<TAF>()).when(weatherService).getTAFs(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.taf(WeatherController.ATLANTA_SECTIONAL));

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);

        Mockito.verify(weatherService, Mockito.times(1)).getTAFs(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
    }

    /**
     * Test retrieval of Atlanta area TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getTAF() throws Exception {
        final TAF taf = new TAF();
        taf.setIcao(ICAO_CODE);
        Mockito.doReturn(taf).when(weatherService).getTAF(ArgumentMatchers.any());
        Mockito.doReturn(Boolean.TRUE).when(weatherService).isValidStation(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.taf(ICAO_CODE));

        Mockito.verify(weatherService, Mockito.times(1)).getTAF(ArgumentMatchers.any());
        Mockito.verify(weatherService, Mockito.times(1)).isValidStation(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
        Mockito.verifyNoInteractions(propertyService);
    }

    /**
     * Test retrieval of Atlanta area TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = InvalidPayloadException.class)
    public void getTAFInvalidStation() throws Exception {
        Mockito.doReturn(Boolean.FALSE).when(weatherService).isValidStation(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.taf(INVALID_ICAO_CODE));

        Mockito.verify(weatherService, Mockito.times(1)).isValidStation(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
        Mockito.verifyNoInteractions(propertyService);
    }

    /**
     * Test retrieval of Atlanta area TAFs.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = InvalidPayloadException.class)
    public void getTAFNullStation() throws Exception {
        Assert.assertNotNull(weatherController.taf(null));

        Mockito.verifyNoInteractions(propertyService, weatherService);
    }

    /**
     * Test retrieval of Atlanta area Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getAtlantaStation() throws Exception {
        Mockito
                .doReturn(atlantaIcaoCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));
        Mockito.doReturn(new ArrayList<Station>()).when(weatherService).getStations(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.station(WeatherController.ATLANTA));

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito.verify(weatherService, Mockito.times(1)).getStations(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
    }

    /**
     * Test retrieval of Atlanta area Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getAtlantaSectionalStation() throws Exception {
        Mockito
                .doReturn(atlantaIcaoCodesProperty)
                .when(propertyService)
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        Mockito.doReturn(new ArrayList<Station>()).when(weatherService).getStations(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.station(WeatherController.ATLANTA_SECTIONAL));

        Mockito
                .verify(propertyService, Mockito.times(1))
                .get(ArgumentMatchers.eq(PropertyKeyConstants.ATLANTA_SECTIONAL_ICAO_CODES_PROPERTY_KEY));
        Mockito.verifyNoMoreInteractions(propertyService);
        Mockito.verify(weatherService, Mockito.times(1)).getStations(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
    }

    /**
     * Test retrieval of Atlanta area Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getStation() throws Exception {
        final Station station = new Station();
        station.setIcao(ICAO_CODE);
        Mockito.doReturn(station).when(weatherService).getStation(ArgumentMatchers.any());
        Mockito.doReturn(Boolean.TRUE).when(weatherService).isValidStation(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.station(ICAO_CODE));

        Mockito.verify(weatherService, Mockito.times(1)).getStation(ArgumentMatchers.any());
        Mockito.verify(weatherService, Mockito.times(1)).isValidStation(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
        Mockito.verifyNoInteractions(propertyService);
    }

    /**
     * Test retrieval of Atlanta area Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = InvalidPayloadException.class)
    public void getStationInvalidStation() throws Exception {
        Mockito.doReturn(Boolean.FALSE).when(weatherService).isValidStation(ArgumentMatchers.any());

        Assert.assertNotNull(weatherController.station(INVALID_ICAO_CODE));

        Mockito.verify(weatherService, Mockito.times(1)).isValidStation(ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(weatherService);
        Mockito.verifyNoInteractions(propertyService);
    }

    /**
     * Test retrieval of Atlanta area Stations.
     *
     * @throws Exception when a test error occurs
     */
    @Test(expected = InvalidPayloadException.class)
    public void getStationNullStation() throws Exception {
        Assert.assertNotNull(weatherController.station(null));

        Mockito.verifyNoInteractions(propertyService, weatherService);
    }

}