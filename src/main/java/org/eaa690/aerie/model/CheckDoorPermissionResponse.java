package org.eaa690.aerie.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * CheckRFIDPermissionResponse.
 */
@Getter
@Setter
public class CheckDoorPermissionResponse {
    private String name;
    private boolean isMembershipCurrentlyActive;
    private String expirationDate;
    private boolean hasPermissionToOpenDoor;
}
