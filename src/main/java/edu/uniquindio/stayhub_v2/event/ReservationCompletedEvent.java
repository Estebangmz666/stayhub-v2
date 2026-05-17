package edu.uniquindio.stayhub_v2.event;

import edu.uniquindio.stayhub_v2.model.Reservation;

/**
 * Represents an event indicating the successful completion of a reservation.
 * <p>
 * This event encapsulates the completed reservation details. It can be utilized
 * in scenarios like triggering post-completion actions (e.g., sending confirmation emails,
 * updating analytics, notifying stakeholders, etc.).
 * <p>
 * Key Characteristics:
 * - Encapsulates the completed {@link Reservation}.
 * - Signals the transition of a reservation to a "COMPLETED" state.
 * <p>
 * Typical Use Cases:
 * - Event-driven systems to react to reservation completion.
 * - External integrations that need to consume reservation completion information.
 * <p>
 * Thread Safety:
 * This record is immutable and therefore inherently thread-safe.
 * <p>
 * See Also:
 * - {@link Reservation} for detailed reservation attributes.
 */
public record ReservationCompletedEvent (Reservation reservation) {}