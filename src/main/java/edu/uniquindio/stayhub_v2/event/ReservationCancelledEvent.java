package edu.uniquindio.stayhub_v2.event;

import edu.uniquindio.stayhub_v2.model.Reservation;

/**
 * Domain event published after a reservation is cancelled.
 *
 * @param reservation Cancelled reservation.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public record ReservationCancelledEvent(Reservation reservation) {
}
