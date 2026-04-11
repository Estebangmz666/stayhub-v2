package edu.uniquindio.stayhub_v2.model;

/**
 * Enumeration representing the possible states of a reservation in the StayHub platform.
 *
 * <p>This enum defines the lifecycle stages of a reservation, from creation through
 * completion or cancellation. The status determines what operations are permitted
 * on the reservation and how it is displayed to users.</p>
 *
 * <p><b>Status Lifecycle:</b></p>
 * <pre>
 *                    ┌─────────────┐
 *                    │   ACTIVE    │
 *                    └──────┬──────┘
 *                           │
 *           ┌───────────────┼───────────────┐
 *           │               │               │
 *           ▼               ▼               ▼
 *    ┌──────────┐    ┌───────────┐    ┌──────────┐
 *    │CANCELLED │    │ COMPLETED │    │ NO_SHOW  │
 *    └──────────┘    └───────────┘    └──────────┘
 *                    (future)
 * </pre>
 *
 * <p><b>State Descriptions:</b></p>
 * <ul>
 *   <li><b>ACTIVE:</b> Reservation is confirmed and upcoming/ongoing</li>
 *   <li><b>CANCELLED:</b> Reservation was cancelled before completion</li>
 *   <li><b>COMPLETED:</b> Guest has completed their stay</li>
 * </ul>
 *
 * <p><b>Business Rules by Status:</b></p>
 * <table border="1">
 *   <tr><th>Status</th><th>Can Cancel?</th><th>Can Modify?</th><th>Can Review?</th></tr>
 *   <tr><td>ACTIVE</td><td>✅ Yes</td><td>✅ Yes*</td><td>❌ No</td></tr>
 *   <tr><td>CANCELLED</td><td>❌ No</td><td>❌ No</td><td>❌ No</td></tr>
 *   <tr><td>COMPLETED</td><td>❌ No</td><td>❌ No</td><td>✅ Yes</td></tr>
 * </table>
 * <p><i>*Subject to accommodation's modification policy</i></p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * Reservation reservation = new Reservation();
 * reservation.setStatus(ReservationStatus.ACTIVE);
 *
 * // Later, after check-out
 * reservation.setStatus(ReservationStatus.COMPLETED);
 *
 * // Or if cancelled
 * reservation.setStatus(ReservationStatus.CANCELLED);
 * }</pre>
 *
 * <p><b>Database Storage:</b></p>
 * Stored as a string ({@code EnumType.STRING}) in the database to maintain
 * readability and prevent issues if enum order changes in future versions.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see Reservation
 */
public enum ReservationStatus {

    /**
     * The reservation is currently active and confirmed.
     *
     * <p>This is the initial state for all new reservations. An active reservation
     * represents a confirmed booking that is either upcoming or currently ongoing.</p>
     *
     * <p><b>Characteristics:</b></p>
     * <ul>
     *   <li>Guest can cancel (subject to cancellation policy)</li>
     *   <li>Host can view and manage the reservation</li>
     *   <li>Dates are blocked from other bookings</li>
     *   <li>Counts toward occupancy and revenue metrics</li>
     * </ul>
     *
     * <p><b>Transitions To:</b></p>
     * <ul>
     *   <li>{@link #CANCELLED} - Guest or host cancels</li>
     *   <li>{@link #COMPLETED} - Stay finishes successfully</li>
     * </ul>
     */
    ACTIVE,

    /**
     * The reservation has been cancelled.
     *
     * <p>This status indicates that the reservation was terminated before the
     * stay was completed. Cancellation can be initiated by either the guest
     * or the host, subject to the platform's cancellation policy.</p>
     *
     * <p><b>Characteristics:</b></p>
     * <ul>
     *   <li>Dates become available for new bookings</li>
     *   <li>Refund may be processed based on cancellation policy</li>
     *   <li>No further modifications allowed</li>
     *   <li>Reservation remains in system for audit purposes</li>
     * </ul>
     *
     * <p><b>Cancellation Policies:</b></p>
     * The specific refund amount depends on the cancellation policy associated
     * with the accommodation and the timing of the cancellation relative to
     * the check-in date.</p>
     *
     * <p><b>Transitions From:</b> Only from {@link #ACTIVE}</p>
     * <p><b>Transitions To:</b> None (terminal state)</p>
     */
    CANCELLED,

    /**
     * The reservation has been successfully completed.
     *
     * <p>This status indicates that the guest has completed their stay and
     * checked out. The reservation is considered successfully fulfilled.</p>
     *
     * <p><b>Characteristics:</b></p>
     * <ul>
     *   <li>Guest can now leave a review for the accommodation</li>
     *   <li>Host can leave a review for the guest</li>
     *   <li>Payment is finalized (if not already processed)</li>
     *   <li>No further modifications allowed</li>
     *   <li>Counts toward completed stay metrics</li>
     * </ul>
     *
     * <p><b>Automatic Transition:</b> This status is typically set automatically
     * by a scheduled job after the {@code endDate} has passed, though manual
     * confirmation by the host may be required in some cases.</p>
     *
     * <p><b>Transitions From:</b> Only from {@link #ACTIVE}</p>
     * <p><b>Transitions To:</b> None (terminal state)</p>
     */
    COMPLETED

    // Future statuses that could be added:
    // NO_SHOW - Guest didn't arrive without cancelling
    // PENDING - Awaiting host confirmation
    // REJECTED - Host declined the reservation request
}