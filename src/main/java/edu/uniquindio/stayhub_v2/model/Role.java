package edu.uniquindio.stayhub_v2.model;

/**
 * Enum to define the different roles of a user within the application.
 * @author Esteban Gómez León
 * @version 1.0
 */
public enum Role {
    /**
     * A user who can book accommodations.
     */
    GUEST,
    /**
     * A user who can list and manage their own accommodations.
     */
    HOST
}