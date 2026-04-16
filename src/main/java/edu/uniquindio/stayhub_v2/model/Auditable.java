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
 * Abstract base class providing automatic auditing capabilities to JPA entities.
 *
 * <p>This class serves as a foundation for all entities that require automatic
 * tracking of creation and modification timestamps. It leverages Spring Data JPA's
 * auditing infrastructure to automatically populate these fields without manual
 * intervention.</p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Automatic {@code createdAt} population on entity persistence</li>
 *   <li>Automatic {@code updatedAt} refresh on entity updates</li>
 *   <li>Integration with Spring Security for {@code @CreatedBy} and {@code @LastModifiedBy}
 *       (can be added by extending classes)</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * @Entity
 * public class Product extends Auditable {
 *     @Id
 *     private Long id;
 *     private String name;
 *     // createdAt and updatedAt are inherited automatically
 * }
 * }</pre>
 *
 * <p><b>Configuration Required:</b></p>
 * Remember to enable JPA auditing in your Spring configuration:
 * <pre>{@code
 * @Configuration
 * @EnableJpaAuditing
 * public class JpaConfig {
 * }
 * }</pre>
 *
 * <p><b>Inheritance Strategy:</b></p>
 * Uses {@code @MappedSuperclass} to indicate that this class is not an entity
 * itself, but its fields are mapped to columns in the tables of inheriting
 * entity classes. Each child entity will have its own {@code created_at} and
 * {@code updated_at} columns.</p>
 *
 * <p><b>Lombok Integration:</b></p>
 * Uses {@code @SuperBuilder} to support builder pattern inheritance,
 * {@code @Getter} and {@code @Setter} for accessors, and {@code @NoArgsConstructor}
 * for JPA compatibility.</p>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see AuditingEntityListener
 * @see CreatedDate
 * @see LastModifiedDate
 * @see MappedSuperclass
 */
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    /*
     * Date and time the record was created.
     *
     * <p>This field is automatically populated by Spring Data JPA when the
     * entity is persisted for the first time. The {@code updatable = false}
     * attribute ensures that once set, this value cannot be modified through
     * standard JPA update operations.</p>
     *
     * <p><b>Database Column:</b> {@code created_at} (NOT NULL, non-updatable)</p>
     *
     * <p><b>Typical Values:</b></p>
     * <ul>
     *   <li>ISO 8601 format: {@code 2025-04-10T14:30:00}</li>
     *   <li>Stored in database server timezone</li>
     * </ul>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>Audit trails and compliance reporting</li>
     *   <li>Sorting records by creation order</li>
     *   <li>Age analysis of data</li>
     * </ul>
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /*
     * Date and time of the last record update.
     *
     * <p>This field is automatically refreshed by Spring Data JPA every time
     * the entity is updated and flushed to the database. Unlike {@code createdAt},
     * this field is updatable and will reflect the most recent modification.</p>
     *
     * <p><b>Database Column:</b> {@code updated_at} (NOT NULL, updatable)</p>
     *
     * <p><b>Update Triggers:</b></p>
     * <ul>
     *   <li>Explicit entity modifications via setters</li>
     *   <li>JPA dirty checking during a flush</li>
     *   <li>Manual invocation of {@link #touch()}</li>
     * </ul>
     *
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>Tracking last activity on a record</li>
     *   <li>Cache invalidation strategies</li>
     *   <li>Incremental data synchronization</li>
     *   <li>Finding stale or abandoned records</li>
     * </ul>
     *
     * <p><b>Initial Value:</b> On the first creation, this field is set to the same
     * value as {@code createdAt}.</p>
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}