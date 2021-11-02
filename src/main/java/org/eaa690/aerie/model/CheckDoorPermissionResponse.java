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

package org.eaa690.aerie.model;

import lombok.Getter;
import lombok.Setter;

/**
 * CheckRFIDPermissionResponse.
 */
@Getter
@Setter
public class CheckDoorPermissionResponse {
    /**
     * Name.
     */
    private String name;
    /**
     * Is Membership Currently Active.
     */
    private boolean isMembershipCurrentlyActive;
    /**
     * Expiration Date.
     */
    private String expirationDate;
    /**
     * Has Permission To Open Door.
     */
    private boolean hasPermissionToOpenDoor;
}
