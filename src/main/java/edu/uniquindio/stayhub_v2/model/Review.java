package edu.uniquindio.stayhub_v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity that represents a review made by a guest for a specific accommodation.
 *
 * <p>This class stores the rating and comment provided by a guest after completing
 * a reservation. Each review is uniquely associated with a reservation, ensuring
 * that only one review can be created per reservation.</p>
 *
 * <p>Relationships:</p>
 * <ul>
 *     <li>Many reviews can belong to one accommodation</li>
 *     <li>Many reviews can be created by one guest</li>
 *     <li>Each review is linked to exactly one reservation (one-to-one)</li>
 * </ul>
 *
 * <p>Indexes are defined to optimize queries by accommodation, guest, and reservation.</p>
 *
 * @author Stayhub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_review_accommodation", columnList = "accommodation_id"),
                @Index(name = "idx_review_guest", columnList = "guest_id"),
                @Index(name = "idx_review_reservation", columnList = "reservation_id", unique = true)
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Review extends Auditable {

    /**
     * Unique identifier of the review.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The accommodation being reviewed.
     */
    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    /**
     * The guest who created the review.
     */
    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    /**
     * The reservation associated with this review.
     * Each reservation can have only one review.
     */
    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    /**
     * Rating given by the guest (typically from 1 to 5).
     */
    @Column(nullable = false)
    private Integer rating;

    /**
     * Textual comment provided by the guest about the accommodation.
     */
    @Column(nullable = false, length = 1000)
    private String comment;

    /**
     * Optional response written by the accommodation host for this review.
     */
    @Column(length = 1000)
    private String hostResponse;

    /**
     * Date and time when the host response was registered.
     */
    @Column
    private java.time.LocalDateTime hostRespondedAt;
}
