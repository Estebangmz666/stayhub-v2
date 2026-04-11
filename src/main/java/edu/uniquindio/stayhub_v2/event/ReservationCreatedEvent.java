package edu.uniquindio.stayhub_v2.event;

import edu.uniquindio.stayhub_v2.model.Reservation;

/**
 * Event fired when a new reservation is successfully created in the system.
 *
 * <p>This event is published after a reservation has been validated,
 * persisted, and all business rules have been applied.</p>
 *
 * <p>Typical consumers of this event include:
 * <ul>
 *   <li>Notification service (send confirmation emails/SMS)</li>
 *   <li>Audit logging system</li>
 *   <li>Analytics/monitoring services</li>
 *   <li>Cache invalidation handlers</li>
 * </ul>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * @EventListener
 * public void handleReservationCreated(ReservationCreatedEvent event) {
 *     Reservation reservation = event.reservation();
 *     notificationService.sendConfirmation(reservation);
 * }
 * }</pre>
 *
 * @param reservation The newly created reservation entity with all its details
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see Reservation
 * @see edu.uniquindio.stayhub_v2.service.ReservationService
 */
public record ReservationCreatedEvent(Reservation reservation) {}