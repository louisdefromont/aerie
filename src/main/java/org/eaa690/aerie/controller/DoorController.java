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

import io.jsonwebtoken.lang.Collections;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.InvalidPayloadException;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Avea;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.wx.METAR;
import org.eaa690.aerie.model.wx.Station;
import org.eaa690.aerie.model.wx.TAF;
import org.eaa690.aerie.service.PropertyService;
import org.eaa690.aerie.service.RosterService;
import org.eaa690.aerie.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DoorController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/door"
})
public class DoorController {

    /**
     * INVALID_DOOR_MSG.
     */
    public static final String INVALID_DOOR_MSG =
            "Provided door [%s] is not valid.  "
                    + "Please provide an acceptable door identifier";

    /**
     * NO_RFID_MSG.
     */
    public static final String NO_RFID_MSG = "No RFID was provided";

    /**
     * RosterService.
     */
    private RosterService rosterService;

    /**
     * Sets RosterService.
     *
     * @param value RosterService
     */
    @Autowired
    public void setRosterService(final RosterService value) {
        rosterService = value;
    }

    /**
     * Determine is access is granted to the door.
     *
     * @param cmds command from door reader
     * @param modes modes
     * @param uids uids
     * @return response to door reader
     */
    @GetMapping(path = {
            "/avea.php"
    })
    public String accessGranted(@RequestParam(required = false, value = "cmd") final List<String> cmds,
                                @RequestParam(required = false, value = "mode") final List<String> modes,
                                @RequestParam(required = false, value = "uid") final List<String> uids) {
        Avea avea = new Avea();
        try {
            if (!Collections.isEmpty(uids)) {
                Member member = rosterService.getMemberByRFID(uids.get(0));
                avea.setGranted(true);
                avea.setMsg(member.getRfid());
                return avea.toString();
            }
        } catch (ResourceNotFoundException e) {
            return avea.toString();
        }
        return avea.toString();
    }

}