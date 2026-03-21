package edu.uniquindio.stayhub_v2.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Abstract base class to provide automatic auditing capabilities to entities.
 * <p>
 * Uses {@link AuditingEntityListener} to automatically capture the creation and modification dates of records in the database.
 * </p>
 * @author Esteban Gómez León
 * @version 1.0
 */
@Getter @Setter @NoArgsConstructor @MappedSuperclass @SuperBuilder @EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    /*
    Date and time the record was created.
    Does not allow null values and is not updated after the initial insertion.
     */
    @CreatedDate
    @Column(nullable= false, updatable = false)
    private LocalDateTime createdAt;

    /*
     * Date and time of the last record update.
     * It is automatically updated whenever the entity persists changes.
     */
    @LastModifiedDate
    @Column(nullable= false)
    private LocalDateTime updatedAt;

    /**
     * Manually updates the object's modification date to the current time.
     * Useful for forcing an audit update on specific business logic.
     */
    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}