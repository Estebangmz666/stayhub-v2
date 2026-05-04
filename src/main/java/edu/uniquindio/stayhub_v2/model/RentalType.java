package edu.uniquindio.stayhub_v2.model;

/**
 * Enum representing the type of rental offered in a {@link RentalPackage}.
 *
 * <ul>
 *   <li>{@link #CASA_ENTERA} – The entire property is rented as a whole unit.</li>
 *   <li>{@link #POR_HABITACIONES} – Individual rooms are rented separately.</li>
 *   <li>{@link #AMBAS} – Both rental modes are available for the same date range.</li>
 * </ul>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public enum RentalType {

    /** Entire house/property rented as a single unit. */
    CASA_ENTERA,

    /** Rooms rented individually within the property. */
    POR_HABITACIONES,

    /** Both rental modes are offered simultaneously. */
    AMBAS
}
