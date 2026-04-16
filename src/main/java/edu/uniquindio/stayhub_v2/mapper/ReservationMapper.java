package edu.uniquindio.stayhub_v2.mapper;

import edu.uniquindio.stayhub_v2.dto.reservation.CreateReservationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.reservation.RetrieveReservationSummaryResponseDTO;
import edu.uniquindio.stayhub_v2.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting Reservation entities to DTOs.
 *
 * <p>This mapper uses MapStruct to generate type-safe mapping code at compile time.
 * The {@code componentModel = "spring"} configuration makes the generated
 * implementation a Spring bean that can be injected via {@code @Autowired}.</p>
 *
 * <p>This mapper includes custom field mappings for traversing nested objects:
 * <ul>
 *   <li>{@code accommodationId} ← {@code accommodation.id}</li>
 *   <li>{@code accommodationTitle} ← {@code accommodation.title}</li>
 *   <li>{@code userId} ← {@code guest.id}</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class ReservationService {
 *
 *     private final ReservationMapper reservationMapper;
 *
 *     public CreateReservationResponseDTO createReservation(CreateReservationRequestDTO request) {
 *         Reservation reservation = buildAndSaveReservation(request);
 *         return reservationMapper.toDTO(reservation);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Field Mapping Details:</b></p>
 * <table border="1">
 *   <tr><th>Source Field</th><th>Target Field</th><th>Description</th></tr>
 *   <tr><td>{@code accommodation.id}</td><td>{@code accommodationId}</td><td>ID of the booked accommodation</td></tr>
 *   <tr><td>{@code accommodation.title}</td><td>{@code accommodationTitle}</td><td>Title/name of the accommodation</td></tr>
 *   <tr><td>{@code guest.id}</td><td>{@code userId}</td><td>ID of the guest who made the reservation</td></tr>
 *   <tr><td>{@code id}, {@code startDate}, etc.</td><td><i>Same name</i></td><td>Automatically mapped fields</td></tr>
 * </table>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see org.mapstruct.Mapper
 * @see org.mapstruct.Mapping
 * @see Reservation
 * @see CreateReservationResponseDTO
 */
@Mapper(componentModel = "spring")
public interface ReservationMapper {

    /**
     * Maps a Reservation entity to a CreateReservationResponseDTO.
     *
     * <p>This method flattens nested object references into primitive fields
     * for a cleaner API response. The accommodation and guest information
     * is extracted from their respective nested objects.</p>
     *
     * <p><b>Automatically Mapped Fields (by name match):</b></p>
     * <ul>
     *   <li>{@code id} → {@code id}</li>
     *   <li>{@code startDate} → {@code startDate}</li>
     *   <li>{@code endDate} → {@code endDate}</li>
     *   <li>{@code totalPrice} → {@code totalPrice}</li>
     *   <li>{@code currency} → {@code currency}</li>
     *   <li>{@code status} → {@code status}</li>
     * </ul>
     *
     * <p><b>Explicitly Mapped Fields:</b></p>
     * <ul>
     *   <li>{@code accommodation.id} → {@code accommodationId}</li>
     *   <li>{@code accommodation.title} → {@code accommodationTitle}</li>
     *   <li>{@code guest.id} → {@code userId}</li>
     * </ul>
     *
     * @param reservation The Reservation entity to be mapped (must not be null,
     *                    must have associated accommodation and guest)
     * @return CreateReservationResponseDTO containing the flattened reservation data
     * @throws NullPointerException if reservation, accommodation, or guest is null
     */
    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "accommodationTitle", source = "accommodation.title")
    @Mapping(target = "userId", source = "guest.id")
    @Mapping(target = "depositAmount", ignore = true)
    @Mapping(target = "bankAccountNumber", ignore = true)
    @Mapping(target = "paymentDeadline", ignore = true)
    CreateReservationResponseDTO toDTO(Reservation reservation);

    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "accommodationTitle", source = "accommodation.title")
    @Mapping(target = "accommodationCity", source = "accommodation.city")
    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "guestEmail", source = "guest.email")
    @Mapping(target = "currencyCode", source = "currency.currencyCode")
    RetrieveReservationResponseDTO toRetrieveDTO(Reservation reservation);

    @Mapping(target = "accommodationId", source = "accommodation.id")
    @Mapping(target = "accommodationTitle", source = "accommodation.title")
    @Mapping(target = "currencyCode", source = "currency.currencyCode")
    RetrieveReservationSummaryResponseDTO toSummaryDTO(Reservation reservation);
}