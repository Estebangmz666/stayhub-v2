package edu.uniquindio.stayhub_v2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

/**
 * Entity representing an accommodation listing in the StayHub platform.
 *
 * <p>
 * This entity stores all information related to a property listed for rent,
 * including its location, pricing, capacity, and associated media.
 * Accommodations
 * are owned by hosts and can be booked by guests through reservations.
 * </p>
 *
 * <p>
 * <b>Key Features:</b>
 * </p>
 * <ul>
 * <li>Soft delete support via {@code deleted} flag</li>
 * <li>Geolocation support with latitude/longitude coordinates</li>
 * <li>Multiple images support via {@code @ElementCollection}</li>
 * <li>Audit trail inherited from {@link Auditable}</li>
 * <li>Indexed fields for optimized queries</li>
 * </ul>
 *
 * <p>
 * <b>Database Indexes:</b>
 * </p>
 * <ul>
 * <li>{@code idx_accommodation_deleted} - Optimizes soft-delete filtering</li>
 * <li>{@code idx_accommodation_host} - Optimizes queries by host</li>
 * <li>{@code idx_accommodation_lat_lon} - Optimizes geospatial searches</li>
 * </ul>
 *
 * <p>
 * <b>Relationships:</b>
 * </p>
 * <ul>
 * <li><b>Host:</b> Many-to-One with {@link User} (owner of the
 * accommodation)</li>
 * <li><b>Reservations:</b> One-to-Many with {@link Reservation} (bookings
 * made)</li>
 * <li><b>Images:</b> ElementCollection of image URLs</li>
 * </ul>
 *
 * <p>
 * <b>Lifecycle:</b>
 * </p>
 * Accommodations support soft deletion. When {@code deleted = true}, the entity
 * remains in the database but should be filtered out from all user-facing
 * queries.
 * Use the {@code available} flag to temporarily hide listings without deleting
 * them.
 * </p>
 *
 * @author Stayhub Dev Team
 * @version 1.0
 * @since 1.0
 * @see Auditable
 * @see User
 * @see Reservation
 */
@Entity
@Table(name = "accommodations", indexes = {
                @Index(name = "idx_accommodation_deleted", columnList = "deleted"),
                @Index(name = "idx_accommodation_host", columnList = "host_id"),
                @Index(name = "idx_accommodation_lat_lon", columnList = "latitude, longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Accommodation extends Auditable {

        /**
         * The unique identifier for the accommodation.
         * Generated automatically using database identity strategy.
         */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /**
         * The host (owner) of the accommodation.
         *
         * <p>
         * This is a required many-to-one relationship. Every accommodation
         * must belong to a registered host user. The host has full management
         * rights over this accommodation.
         * </p>
         */
        @ManyToOne
        @JoinColumn(name = "host_id", nullable = false)
        private User host;

        /**
         * The title or name of the accommodation listing.
         *
         * <p>
         * Displayed prominently in search results and listing pages.
         * Should be concise yet descriptive (e.g., "Cozy Beachfront Villa").
         * </p>
         */
        @Column(nullable = false)
        private String title;

        /**
         * A detailed description of the accommodation.
         *
         * <p>
         * Includes information about amenities, house rules, nearby attractions,
         * and any special features. Supports multi-line text and rich formatting
         * in the frontend.
         * </p>
         */
        @Column(nullable = false)
        private String description;

        /**
         * The maximum number of guests the accommodation can host.
         *
         * <p>
         * Used to validate reservation requests and filter search results.
         * Must be greater than zero.
         * </p>
         */
        @Column(nullable = false)
        private Integer capacity;

        /**
         * The currency used for pricing this accommodation.
         *
         * <p>
         * Stored as a {@link Currency} object. All monetary values for this
         * accommodation are expressed in this currency. Examples: USD, EUR, COP.
         * </p>
         */
        @Column(nullable = false)
        private Currency currency;

        /**
         * The base price per night for the accommodation.
         *
         * <p>
         * This is the standard rate before any seasonal adjustments,
         * discounts, or additional fees. The total reservation price is
         * calculated based on this value multiplied by the number of nights.
         * </p>
         */
        @Column(nullable = false)
        private BigDecimal pricePerNight;

        /**
         * The URL of the main (cover) image for the accommodation listing.
         *
         * <p>
         * This image is displayed as the primary thumbnail in search results
         * and as the hero image on the listing detail page. Additional images
         * can be stored in the {@link #images} collection.
         * </p>
         */
        @Column(name = "main_image")
        private String mainImage;

        /**
         * List of reviews written by guests about this accommodation.
         *
         * <p>
         * This bidirectional relationship allows the platform to expose
         * ratings, written feedback, and host responses associated with a
         * completed stay at this accommodation.
         * </p>
         */
        @OneToMany(mappedBy = "accommodation")
        private List<Review> reviews;

        /**
         * The longitude coordinate of the accommodation's location.
         *
         * <p>
         * Used for map display and geospatial searches. Valid values range
         * from -180 to 180 degrees. Combined with {@link #latitude} to form
         * a complete geographic coordinate.
         * </p>
         */
        @Column(nullable = false)
        private Double longitude;

        /**
         * The latitude coordinate of the accommodation's location.
         *
         * <p>
         * Used for map display and geospatial searches. Valid values range
         * from -90 to 90 degrees. Combined with {@link #longitude} to form
         * a complete geographic coordinate.
         * </p>
         */
        @Column(nullable = false)
        private Double latitude;

        /**
         * A human-readable description of the accommodation's location.
         *
         * <p>
         * Describes the neighborhood, nearby landmarks, transportation options,
         * and other contextual information that helps guests understand the area.
         * Example: "Located in the historic district, 5-min walk to Central Park."
         * </p>
         */
        @Column(name = "location_description", nullable = false)
        private String locationDescription;

        /**
         * The city where the accommodation is located.
         *
         * <p>
         * Used for search filtering and display purposes. Consider adding
         * country/state fields for more granular location data in the future.
         * </p>
         */
        @Column(nullable = false)
        private String city;

        /**
         * A collection of additional image URLs for the accommodation.
         *
         * <p>
         * Stored in a separate table {@code accommodation_images} using
         * {@code @ElementCollection}. Each URL must be valid. These images
         * are displayed in a gallery on the listing detail page.
         * </p>
         *
         * <p>
         * <b>Validation:</b> Each URL in the list is validated against
         * the {@code @URL} constraint. Consider adding {@code @Valid} on the
         * field if automatic validation of collection elements is needed.
         * </p>
         */
        @ElementCollection
        @CollectionTable(name = "accommodation_images", joinColumns = @JoinColumn(name = "accommodation_id"))
        @Column(name = "image_url")
        private List<@URL(message = "Each image must be a valid URL") String> images;

        /**
         * List of reservations made for this accommodation.
         *
         * <p>
         * Bidirectional one-to-many relationship with {@link Reservation}.
         * The accommodation is the "inverse" side of the relationship, with
         * the reservation being the owning side (contains the foreign key).
         * </p>
         *
         * <p>
         * <b>Usage:</b> Used to check availability for given dates and to
         * enforce business rules (e.g., preventing deletion if active
         * reservations exist).
         * </p>
         */
        @jakarta.persistence.OneToMany(mappedBy = "accommodation")
        private List<Reservation> reservations;

        /**
         * Rental packages defined by the host for specific date ranges.
         *
         * <p>
         * Each package overrides the base {@code pricePerNight} for its period
         * and defines which rental modality is offered.
         * Managed via {@code RentalPackageService}.
         * </p>
         */
        @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<RentalPackage> rentalPackages;

        /**
         * Soft-delete flag indicating whether this accommodation is deleted.
         *
         * <p>
         * When {@code true}, the accommodation should be excluded from all
         * user-facing queries and operations. This preserves historical data
         * (reservations, reviews) while removing the listing from active use.
         * </p>
         *
         * <p>
         * <b>Default:</b> {@code false} (not deleted)
         * </p>
         *
         * <p>
         * <b>Usage in Queries:</b>
         * </p>
         * 
         * <pre>{@code
         * // Always filter out deleted accommodations
         * List<Accommodation> activeListings = repository.findByDeletedFalse();
         * }</pre>
         */
        @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
        @Builder.Default
        private boolean deleted = false;

        /**
         * Availability flag indicating whether the accommodation is currently
         * available for booking.
         *
         * <p>
         * Unlike {@link #deleted}, this is a reversible status change.
         * Hosts can temporarily hide their listing without deleting it.
         * When {@code false}, the accommodation will not appear in search
         * results but remains fully accessible to the host for management.
         * </p>
         *
         * <p>
         * <b>Default:</b> {@code true} (available)
         * </p>
         *
         * <p>
         * <b>Use Cases:</b>
         * </p>
         * <ul>
         * <li>Seasonal unavailability</li>
         * <li>Maintenance periods</li>
         * <li>Temporary pause of listings</li>
         * </ul>
         */
        @Column(name = "available", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
        @Builder.Default
        private boolean available = true;
}
