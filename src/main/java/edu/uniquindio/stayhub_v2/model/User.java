package edu.uniquindio.stayhub_v2.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the application.
 * This class stores the user's personal data and authentication information
 * @author Esteban Gómez León
 * @version 1.0
 */
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true)
        })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable{

    /**
     * The unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user's email address. It must be unique and in a valid format.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * The user's password. It must meet complexity requirements: at least 8 characters,
     * including an uppercase letter and a number.
     */
    @Column(nullable = false)
    private String password;

    /**
     * The user's role within the application (e.g., GUEST, HOST).
     */
    @ElementCollection(targetClass = Role.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name ="role", nullable = false)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * The user's full name.
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * The user's phone number, validated to match the Colombian format (+57 followed by 10 digits).
     */
    @Column(nullable = false)
    private String phoneNumber;

    /**
     * The user's date of birth. It must be a date in the past.
     */
    @Column(nullable = false)
    private LocalDate birthDate;


    /**
     * A flag to indicate if the user has been soft-deleted.
     * Defaults to false.
     */
    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean deleted = false;

    /**
     * An optional URL for the user's profile picture.
     */
    @Column
    private String profilePicture;

    /**
     * Code for recovering the password
     */
    @Column(name = "password_recovery_code", length = 6)
    private String passwordRecoveryCode;

    /**
     * Expiration date and time for the recovery code
     */
    @Column(name = "password_recovery_expiration")
    private java.time.LocalDateTime passwordRecoveryExpiration;

    /*
    TO-DO: CAMBIAR RELACIÓN: USUARIO NO TIENE PROPIEDAD, PROPIEDAD ES DE USUARIO
    private Set<Accommodation> accommodations = new HashSet<>();
     */
}