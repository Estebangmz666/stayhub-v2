package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.rental.CreateRentalPackageRequestDTO;
import edu.uniquindio.stayhub_v2.dto.rental.RentalPackageResponseDTO;
import edu.uniquindio.stayhub_v2.dto.rental.UpdateRentalPackageRequestDTO;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.RentalPackageNotFoundException;
import edu.uniquindio.stayhub_v2.exception.UnauthorizedHostException;
import edu.uniquindio.stayhub_v2.mapper.RentalPackageMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.RentalPackage;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.RentalPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing rental packages associated with accommodations.
 *
 * <p><b>Business rules enforced:</b></p>
 * <ol>
 *   <li>Only users with the {@code HOST} role may create/update/delete packages.</li>
 *   <li>The authenticated HOST must own the target accommodation.</li>
 *   <li>No two packages for the same accommodation may have overlapping date ranges.</li>
 *   <li>The package {@code pricePerNight} replaces the accommodation's base {@code pricePerNight}
 *       for the entire package period.</li>
 * </ol>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RentalPackageService {

    /** Sentinel value used on creation to exclude no existing package from overlap check. */
    private static final long NO_EXCLUDE_ID = -1L;

    private final RentalPackageRepository rentalPackageRepository;
    private final AccommodationRepository accommodationRepository;
    private final RentalPackageMapper rentalPackageMapper;
    private final UserService userService;
    private final AuthorizationService authorizationService;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Creates a new rental package for the given accommodation.
     *
     * @param accommodationId target accommodation
     * @param requestDTO      package data
     * @return the persisted package as a response DTO
     * @throws AccommodationNotFoundException if the accommodation does not exist or is deleted
     * @throws UnauthorizedHostException      if the current user is not HOST or not the owner
     * @throws IllegalStateException          if the date range overlaps with an existing package
     */
    @Transactional
    public RentalPackageResponseDTO createPackage(Long accommodationId,
                                                  CreateRentalPackageRequestDTO requestDTO) {
        User currentUser = userService.getCurrentUser();
        authorizationService.requireHostRole(currentUser);

        Accommodation accommodation = findActiveAccommodation(accommodationId);
        validateOwnership(currentUser, accommodation);

        validateNoOverlap(accommodationId, NO_EXCLUDE_ID,
                requestDTO.startDate(), requestDTO.endDate());

        RentalPackage rentalPackage = rentalPackageMapper.toEntity(requestDTO);
        rentalPackage.setAccommodation(accommodation);

        RentalPackage saved = rentalPackageRepository.save(rentalPackage);
        log.info("Rental package {} created for accommodation {} by host {}",
                saved.getId(), accommodationId, currentUser.getEmail());

        return rentalPackageMapper.toResponseDTO(saved);
    }

    /**
     * Partially updates an existing rental package.
     *
     * <p>Only non-null fields in {@code requestDTO} are applied.
     * When dates change, overlap is re-validated excluding the package being updated.</p>
     *
     * @param accommodationId target accommodation
     * @param packageId       package to update
     * @param requestDTO      fields to update
     * @return the updated package as a response DTO
     * @throws AccommodationNotFoundException  if the accommodation does not exist or is deleted
     * @throws RentalPackageNotFoundException  if the package does not belong to the accommodation
     * @throws UnauthorizedHostException       if the current user is not HOST or not the owner
     * @throws IllegalStateException           if the new date range overlaps with another package
     */
    @Transactional
    public RentalPackageResponseDTO updatePackage(Long accommodationId,
                                                  Long packageId,
                                                  UpdateRentalPackageRequestDTO requestDTO) {
        User currentUser = userService.getCurrentUser();
        authorizationService.requireHostRole(currentUser);

        Accommodation accommodation = findActiveAccommodation(accommodationId);
        validateOwnership(currentUser, accommodation);

        RentalPackage rentalPackage = findPackageInAccommodation(packageId, accommodationId);

        // Determine effective dates after update (fall back to current values)
        LocalDate effectiveStart = requestDTO.startDate() != null
                ? requestDTO.startDate() : rentalPackage.getStartDate();
        LocalDate effectiveEnd = requestDTO.endDate() != null
                ? requestDTO.endDate() : rentalPackage.getEndDate();

        // Cross-field validation when either date changes
        if (requestDTO.startDate() != null || requestDTO.endDate() != null) {
            if (!effectiveEnd.isAfter(effectiveStart)) {
                throw new IllegalArgumentException("End date must be after start date");
            }
            validateNoOverlap(accommodationId, packageId, effectiveStart, effectiveEnd);
        }

        // Apply non-null fields
        if (requestDTO.startDate() != null)     rentalPackage.setStartDate(requestDTO.startDate());
        if (requestDTO.endDate() != null)       rentalPackage.setEndDate(requestDTO.endDate());
        if (requestDTO.pricePerNight() != null) rentalPackage.setPricePerNight(requestDTO.pricePerNight());

        RentalPackage saved = rentalPackageRepository.save(rentalPackage);
        log.info("Rental package {} updated for accommodation {} by host {}",
                packageId, accommodationId, currentUser.getEmail());

        return rentalPackageMapper.toResponseDTO(saved);
    }

    /**
     * Permanently deletes a rental package.
     *
     * @param accommodationId target accommodation
     * @param packageId       package to delete
     * @throws AccommodationNotFoundException if the accommodation does not exist or is deleted
     * @throws RentalPackageNotFoundException if the package does not belong to the accommodation
     * @throws UnauthorizedHostException      if the current user is not HOST or not the owner
     */
    @Transactional
    public void deletePackage(Long accommodationId, Long packageId) {
        User currentUser = userService.getCurrentUser();
        authorizationService.requireHostRole(currentUser);

        Accommodation accommodation = findActiveAccommodation(accommodationId);
        validateOwnership(currentUser, accommodation);

        RentalPackage rentalPackage = findPackageInAccommodation(packageId, accommodationId);
        rentalPackageRepository.delete(rentalPackage);

        log.info("Rental package {} deleted from accommodation {} by host {}",
                packageId, accommodationId, currentUser.getEmail());
    }

    /**
     * Returns all rental packages for an accommodation, ordered by start date.
     *
     * <p>This is a read-only operation available to any authenticated user.</p>
     *
     * @param accommodationId target accommodation
     * @return list of package response DTOs (maybe empty)
     * @throws AccommodationNotFoundException if the accommodation does not exist or is deleted
     */
    @Transactional(readOnly = true)
    public List<RentalPackageResponseDTO> getPackagesByAccommodation(Long accommodationId) {
        findActiveAccommodation(accommodationId); // ensures accommodation exists

        return rentalPackageRepository
                .findByAccommodationIdOrderByStartDateAsc(accommodationId)
                .stream()
                .map(rentalPackageMapper::toResponseDTO)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateOwnership(User user, Accommodation accommodation) {
        if (!accommodation.getHost().getId().equals(user.getId())) {
            log.warn("User {} attempted to manage packages of accommodation {} owned by {}",
                    user.getEmail(), accommodation.getId(),
                    accommodation.getHost().getEmail());
            throw new UnauthorizedHostException(
                    "You do not have permission to manage packages for this accommodation.");
        }
    }

    private Accommodation findActiveAccommodation(Long accommodationId) {
        return accommodationRepository
                .findByIdAndDeletedFalse(accommodationId)
                .orElseThrow(() -> {
                    log.warn("Accommodation not found or deleted: {}", accommodationId);
                    return new AccommodationNotFoundException(
                            "Accommodation not found with id: " + accommodationId);
                });
    }

    private RentalPackage findPackageInAccommodation(Long packageId, Long accommodationId) {
        return rentalPackageRepository
                .findByIdAndAccommodationId(packageId, accommodationId)
                .orElseThrow(() -> {
                    log.warn("Rental package {} not found for accommodation {}", packageId, accommodationId);
                    return new RentalPackageNotFoundException(
                            "Rental package not found with id: " + packageId
                                    + " for accommodation: " + accommodationId);
                });
    }

    private void validateNoOverlap(Long accommodationId, Long excludeId,
                                   LocalDate startDate, LocalDate endDate) {
        boolean overlaps = rentalPackageRepository
                .existsOverlappingPackage(accommodationId, excludeId, startDate, endDate);
        if (overlaps) {
            throw new IllegalStateException(
                    "The date range [" + startDate + ", " + endDate + "] overlaps "
                            + "with an existing rental package for this accommodation.");
        }
    }
}
