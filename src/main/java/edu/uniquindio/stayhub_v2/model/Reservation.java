package edu.uniquindio.stayhub_v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

/**
 * Entity representing a reservation (booking) in the StayHub platform.
 *
 * <p>
 * This entity captures all details of a booking made by a guest for a specific
 * accommodation. It tracks the reservation period, pricing, and current status.
 * Reservations are the core transactional entity connecting guests with
 * accommodations.
 * </p>
 *
 * <p>
 * <b>Key Features:</b>
 * </p>
 * <ul>
 * <li>Tracks start and end dates of the stay</li>
 * <li>Stores the total price in the accommodation's currency</li>
 * <li>Maintains reservation status through {@link ReservationStatus} enum</li>
 * <li>Inherits audit timestamps from {@link Auditable}</li>
 * <li>Links guest and accommodation through many-to-one relationships</li>
 * </ul>
 *
 * <p>
 * <b>Business Rules:</b>
 * </p>
 * <ul>
 * <li>End date must be strictly after start date</li>
 * <li>A reservation cannot overlap with existing active reservations for the
 * same accommodation</li>
 * <li>Total price is calculated based on nights × price per night</li>
 * <li>Status transitions follow a defined lifecycle (ACTIVE →
 * COMPLETED/CANCELLED)</li>
 * </ul>
 *
 * <p>
 * <b>Relationships:</b>
 * </p>
 * <ul>
 * <li><b>Accommodation:</b> Many-to-One - The property being booked</li>
 * <li><b>Guest:</b> Many-to-One - The user making the reservation</li>
 * </ul>
 *
 * <p>
 * <b>Status Lifecycle:</b>
 * </p>
 * 
 * <pre>
 * ACTIVE → COMPLETED (after end date passes)
 * ACTIVE → CANCELLED (guest or host cancellation)
 * ACTIVE → NO_SHOW (guest doesn't arrive)
 * </pre>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see Auditable
 * @see Accommodation
 * @see User
 * @see ReservationStatus
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation extends Auditable {

    /**
     * The unique identifier for the reservation.
     * Generated automatically using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The accommodation being reserved.
     *
     * <p>
     * This is a required many-to-one relationship. Each reservation is
     * associated with exactly one accommodation. The accommodation entity
     * maintains the inverse relationship through its {@code reservations} list.
     * </p>
     *
     * <p>
     * <b>Availability Validation:</b> Before creating a reservation, the
     * system must verify that the accommodation is available for the requested
     * dates by checking existing reservations for this accommodation.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    /**
     * The guest user making the reservation.
     *
     * <p>
     * This is a required many-to-one relationship. The guest is the user
     * who will be staying at the accommodation. A guest can have multiple
     * reservations across different accommodations.
     * </p>
     *
     * <p>
     * <b>Authorization:</b> Guests can only view and manage their own
     * reservations. Hosts can view reservations for accommodations they own.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    /**
     * The start date and time of the reservation (check-in).
     *
     * <p>
     * Represents when the guest can access the accommodation. Typically,
     * check-in times are in the afternoon (e.g., 3:00 PM local time).
     * </p>
     *
     * <p>
     * <b>Validation Rules:</b>
     * </p>
     * <ul>
     * <li>Must be in the future when creating the reservation</li>
     * <li>Must be strictly before {@link #endDate}</li>
     * <li>Cannot overlap with existing reservations for the same accommodation</li>
     * </ul>
     */
    @Column(nullable = false)
    private LocalDateTime startDate;

    /**
     * The end date and time of the reservation (check-out).
     *
     * <p>
     * Represents when the guest must vacate the accommodation. Typically,
     * check-out times are in the morning (e.g., 11:00 AM local time).
     * </p>
     *
     * <p>
     * <b>Validation Rules:</b>
     * </p>
     * <ul>
     * <li>Must be strictly after {@link #startDate}</li>
     * <li>Cannot overlap with existing reservations for the same accommodation</li>
     * <li>Minimum stay duration may apply based on accommodation policies</li>
     * </ul>
     */
    @Column(nullable = false)
    private LocalDateTime endDate;

    /**
     * The current status of the reservation.
     *
     * <p>
     * Indicates the state of the reservation in its lifecycle. The status
     * determines what actions are available (e.g., cancellation, modification).
     * </p>
     *
     * <p>
     * <b>Default Value:</b> {@link ReservationStatus#ACTIVE}
     * </p>
     *
     * <p>
     * <b>Status Transitions:</b>
     * </p>
     * <ul>
     * <li>{@code ACTIVE} → {@code COMPLETED} (automatic after end date)</li>
     * <li>{@code ACTIVE} → {@code CANCELLED} (guest/host cancellation)</li>
     * <li>{@code ACTIVE} → {@code NO_SHOW} (guest doesn't check in)</li>
     * </ul>
     *
     * @see ReservationStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.ACTIVE;

    /**
     * The total price for the entire reservation period.
     *
     * <p>
     * Calculated as: {@code pricePerNight × numberOfNights}. This value is
     * frozen at the time of booking to prevent changes if the accommodation's
     * base price is later modified.
     * </p>
     *
     * <p>
     * <b>Calculation:</b> {@code numberOfNights = endDate - startDate}
     * (partial days count as full nights depending on business rules).
     * </p>
     *
     * <p>
     * <b>Immutability:</b> Once a reservation is created, the total price
     * should not change, even if the accommodation's {@code pricePerNight} is
     * updated later. This ensures billing consistency.
     * </p>
     */
    @Column(nullable = false)
    private BigDecimal totalPrice;

    /**
     * The currency in which the {@link #totalPrice} is expressed.
     *
     * <p>
     * This is typically inherited from the accommodation's currency at the
     * time of booking. Storing the currency with the reservation ensures that
     * the monetary value is always interpreted correctly, even if the
     * accommodation's currency is later changed.
     * </p>
     *
     * <p>
     * <b>Examples:</b> USD, EUR, COP, MXN
     * </p>
     */
    @Column(nullable = false)
    private Currency currency;

    /**
     * The deposit amount required as advance payment (20% of totalPrice).
     *
     * <p>
     * Upon confirmation, the guest must pay this amount within the
     * {@link #paymentDeadline} to secure the reservation.
     * </p>
     */
    @Column(nullable = false)
    private BigDecimal depositAmount;

    /**
     * The deadline by which the guest must pay the deposit.
     *
     * <p>
     * Calculated as: {@code createdAt + 3 days}. If no payment is registered
     * before this date the reservation may be cancelled automatically.
     * </p>
     */
    @Column(nullable = false)
    private LocalDateTime paymentDeadline;

    /**
     * Whether the required deposit has been paid by the guest.
     *
     * <p>
     * Defaults to {@code false} upon reservation creation. This flag
     * is used by the payment reminder scheduler to filter pending payments.
     * </p>
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean depositPaid = false;
}