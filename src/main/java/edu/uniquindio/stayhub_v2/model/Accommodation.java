package edu.uniquindio.stayhub_v2.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;

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
public class Accommodation extends Auditable{

    /**
     * The unique identifier for the accommodation.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The host (owner) of the accommodation.
     * This is a many-to-one relationship with the User entity.
     */
    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    /**
     * The title of the accommodation listing.
     */
    @Column(nullable = false)
    private String title;

    /**
     * A detailed description of the accommodation.
     */
    @Column(nullable = false)
    private String description;

    /**
     * The maximum number of guests the accommodation can host.
     */
    @Column(nullable = false)
    private Integer capacity;

    /**
     * The price per night for the accommodation.
     */
    @Column(nullable = false)
    private BigDecimal pricePerNight;

    /**
     * The URL of the main image for the accommodation listing.
     */
    @Column(name = "main_image")
    private String mainImage;

    /**
     * The longitude coordinate of the accommodation's location.
     */
    @Column(nullable = false)
//    @NotNull(message = "La longitud es obligatoria")
    private Double longitude;

    /**
     * The latitude coordinate of the accommodation's location.
     */
    @Column(nullable = false)
//    @NotNull(message = "La latitud es obligatoria")
    private Double latitude;

    /**
     * A description of the location, such as neighborhood or landmarks.
     */
    @Column(name = "location_description", nullable = false)
    private String locationDescription;

    /**
     * The city where the accommodation is located.
     */
    @Column(nullable = false)
    private String city;

    /**
     * A list of URLs for additional images of the accommodation.
     */
    @ElementCollection
    @CollectionTable(name = "accommodation_images", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "image_url")
    private List<@URL(message = "Cada imagen debe ser una URL válida") String> images;

    /**
     * A list of reservations made for this accommodation.
     * This is a one-to-many relationship with the Reservation entity.
     */
//    @OneToMany(mappedBy = "accommodation")
//    private List<Reservation> reservations;

    /**
     * A list of comments made about this accommodation.
     * This is a one-to-many relationship with the Comment entity.
     */
//    @OneToMany(mappedBy = "accommodation")
//    private List<Comment> comments;

    /**
     * A soft-delete flag. If true, the accommodation is considered deleted.
     */
    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "available", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean available = true;
}