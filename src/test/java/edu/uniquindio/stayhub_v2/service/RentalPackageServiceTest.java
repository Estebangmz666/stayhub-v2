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
import edu.uniquindio.stayhub_v2.model.RentalType;
import edu.uniquindio.stayhub_v2.model.Role;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.RentalPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RentalPackageService}.
 *
 * <p>
 * All dependencies are mocked with Mockito. Tests cover happy paths and all
 * documented exception scenarios.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class RentalPackageServiceTest {

        @Mock
        private RentalPackageRepository rentalPackageRepository;
        @Mock
        private AccommodationRepository accommodationRepository;
        @Mock
        private RentalPackageMapper rentalPackageMapper;
        @Mock
        private UserService userService;

        @InjectMocks
        private RentalPackageService rentalPackageService;

        // ── fixtures ────────────────────────────────────────────────────────────

        private User hostUser;
        private User otherHost;
        private User guestUser;
        private Accommodation accommodation;
        private RentalPackage existingPackage;
        private CreateRentalPackageRequestDTO createRequest;
        private RentalPackageResponseDTO responseDTO;

        @BeforeEach
        void setUp() {
                hostUser = User.builder()
                                .id(1L).email("host@example.com").roles(Set.of(Role.HOST)).build();

                otherHost = User.builder()
                                .id(2L).email("other@example.com").roles(Set.of(Role.HOST)).build();

                guestUser = User.builder()
                                .id(3L).email("guest@example.com").roles(Set.of(Role.GUEST)).build();

                accommodation = Accommodation.builder()
                                .id(10L).host(hostUser).deleted(false).available(true).build();

                existingPackage = RentalPackage.builder()
                                .id(100L)
                                .accommodation(accommodation)
                                .type(RentalType.CASA_ENTERA)
                                .startDate(LocalDate.of(2026, 7, 1))
                                .endDate(LocalDate.of(2026, 7, 31))
                                .price(new BigDecimal("150000.00"))
                                .build();

                createRequest = new CreateRentalPackageRequestDTO(
                                RentalType.CASA_ENTERA,
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 7, 31),
                                new BigDecimal("150000.00"));

                responseDTO = new RentalPackageResponseDTO(
                                100L, 10L, RentalType.CASA_ENTERA,
                                LocalDate.of(2026, 7, 1),
                                LocalDate.of(2026, 7, 31),
                                new BigDecimal("150000.00"),
                                LocalDateTime.now(), LocalDateTime.now());
        }

        // ── createPackage ────────────────────────────────────────────────────────

        @Test
        void createPackage_HostOwner_Success() {
                when(userService.getCurrentUser()).thenReturn(hostUser);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));
                when(rentalPackageRepository.existsOverlappingPackage(
                                eq(10L), eq(-1L),
                                eq(createRequest.startDate()), eq(createRequest.endDate())))
                                .thenReturn(false);
                when(rentalPackageMapper.toEntity(createRequest)).thenReturn(existingPackage);
                when(rentalPackageRepository.save(any())).thenReturn(existingPackage);
                when(rentalPackageMapper.toResponseDTO(existingPackage)).thenReturn(responseDTO);

                RentalPackageResponseDTO result = rentalPackageService.createPackage(10L, createRequest);

                assertThat(result.id()).isEqualTo(100L);
                assertThat(result.accommodationId()).isEqualTo(10L);
                assertThat(result.price()).isEqualByComparingTo("150000.00");

                verify(rentalPackageRepository).save(any(RentalPackage.class));
        }

        @Test
        void createPackage_NonHostUser_ThrowsUnauthorizedHostException() {
                when(userService.getCurrentUser()).thenReturn(guestUser);

                assertThatThrownBy(() -> rentalPackageService.createPackage(10L, createRequest))
                                .isInstanceOf(UnauthorizedHostException.class)
                                .hasMessageContaining("HOST role");

                verify(rentalPackageRepository, never()).save(any());
        }

        @Test
        void createPackage_NotOwner_ThrowsUnauthorizedHostException() {
                when(userService.getCurrentUser()).thenReturn(otherHost);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));

                assertThatThrownBy(() -> rentalPackageService.createPackage(10L, createRequest))
                                .isInstanceOf(UnauthorizedHostException.class)
                                .hasMessageContaining("permission");

                verify(rentalPackageRepository, never()).save(any());
        }

        @Test
        void createPackage_AccommodationNotFound_ThrowsAccommodationNotFoundException() {
                when(userService.getCurrentUser()).thenReturn(hostUser);
                when(accommodationRepository.findByIdAndDeletedFalse(99L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> rentalPackageService.createPackage(99L, createRequest))
                                .isInstanceOf(AccommodationNotFoundException.class)
                                .hasMessageContaining("99");

                verify(rentalPackageRepository, never()).save(any());
        }

        @Test
        void createPackage_OverlappingDates_ThrowsIllegalStateException() {
                when(userService.getCurrentUser()).thenReturn(hostUser);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));
                when(rentalPackageRepository.existsOverlappingPackage(
                                eq(10L), eq(-1L),
                                any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(true);

                assertThatThrownBy(() -> rentalPackageService.createPackage(10L, createRequest))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("overlaps");

                verify(rentalPackageRepository, never()).save(any());
        }

        @Test
        void createPackage_InvalidDateRange_ThrowsIllegalArgumentException() {
                // endDate is not after startDate → constructor throws
                assertThatThrownBy(() -> new CreateRentalPackageRequestDTO(
                                RentalType.CASA_ENTERA,
                                LocalDate.of(2026, 7, 31),
                                LocalDate.of(2026, 7, 1), // endDate before startDate
                                new BigDecimal("100000")))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("End date must be after start date");
        }

        // ── updatePackage ────────────────────────────────────────────────────────

        @Test
        void updatePackage_Success() {
                UpdateRentalPackageRequestDTO updateRequest = new UpdateRentalPackageRequestDTO(
                                RentalType.POR_HABITACIONES, null, null, new BigDecimal("160000.00"));

                when(userService.getCurrentUser()).thenReturn(hostUser);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));
                when(rentalPackageRepository.findByIdAndAccommodationId(100L, 10L))
                                .thenReturn(Optional.of(existingPackage));
                when(rentalPackageRepository.save(any())).thenReturn(existingPackage);

                RentalPackageResponseDTO updated = new RentalPackageResponseDTO(
                                100L, 10L, RentalType.POR_HABITACIONES,
                                existingPackage.getStartDate(), existingPackage.getEndDate(),
                                new BigDecimal("160000.00"),
                                LocalDateTime.now(), LocalDateTime.now());
                when(rentalPackageMapper.toResponseDTO(existingPackage)).thenReturn(updated);

                RentalPackageResponseDTO result = rentalPackageService.updatePackage(10L, 100L, updateRequest);

                assertThat(result.type()).isEqualTo(RentalType.POR_HABITACIONES);
                assertThat(result.price()).isEqualByComparingTo("160000.00");

                verify(rentalPackageRepository).save(existingPackage);
        }

        @Test
        void updatePackage_OverlappingNewDates_ThrowsIllegalStateException() {
                UpdateRentalPackageRequestDTO updateRequest = new UpdateRentalPackageRequestDTO(
                                null,
                                LocalDate.of(2026, 8, 1),
                                LocalDate.of(2026, 8, 31),
                                null);

                when(userService.getCurrentUser()).thenReturn(hostUser);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));
                when(rentalPackageRepository.findByIdAndAccommodationId(100L, 10L))
                                .thenReturn(Optional.of(existingPackage));
                when(rentalPackageRepository.existsOverlappingPackage(
                                eq(10L), eq(100L),
                                eq(LocalDate.of(2026, 8, 1)),
                                eq(LocalDate.of(2026, 8, 31))))
                                .thenReturn(true);

                assertThatThrownBy(() -> rentalPackageService.updatePackage(10L, 100L, updateRequest))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("overlaps");

                verify(rentalPackageRepository, never()).save(any());
        }

        @Test
        void updatePackage_PackageNotFound_ThrowsRentalPackageNotFoundException() {
                UpdateRentalPackageRequestDTO updateRequest = new UpdateRentalPackageRequestDTO(null, null, null,
                                new BigDecimal("100"));

                when(userService.getCurrentUser()).thenReturn(hostUser);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));
                when(rentalPackageRepository.findByIdAndAccommodationId(999L, 10L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> rentalPackageService.updatePackage(10L, 999L, updateRequest))
                                .isInstanceOf(RentalPackageNotFoundException.class)
                                .hasMessageContaining("999");
        }

        @Test
        void updatePackage_NotOwner_ThrowsUnauthorizedHostException() {
                UpdateRentalPackageRequestDTO updateRequest = new UpdateRentalPackageRequestDTO(null, null, null,
                                new BigDecimal("100"));

                when(userService.getCurrentUser()).thenReturn(otherHost);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));

                assertThatThrownBy(() -> rentalPackageService.updatePackage(10L, 100L, updateRequest))
                                .isInstanceOf(UnauthorizedHostException.class)
                                .hasMessageContaining("permission");
        }

        // ── deletePackage ────────────────────────────────────────────────────────

        @Test
        void deletePackage_Success() {
                when(userService.getCurrentUser()).thenReturn(hostUser);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));
                when(rentalPackageRepository.findByIdAndAccommodationId(100L, 10L))
                                .thenReturn(Optional.of(existingPackage));

                rentalPackageService.deletePackage(10L, 100L);

                verify(rentalPackageRepository).delete(existingPackage);
        }

        @Test
        void deletePackage_PackageNotFound_ThrowsRentalPackageNotFoundException() {
                when(userService.getCurrentUser()).thenReturn(hostUser);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));
                when(rentalPackageRepository.findByIdAndAccommodationId(999L, 10L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> rentalPackageService.deletePackage(10L, 999L))
                                .isInstanceOf(RentalPackageNotFoundException.class)
                                .hasMessageContaining("999");

                verify(rentalPackageRepository, never()).delete(any());
        }

        @Test
        void deletePackage_NotOwner_ThrowsUnauthorizedHostException() {
                when(userService.getCurrentUser()).thenReturn(otherHost);
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));

                assertThatThrownBy(() -> rentalPackageService.deletePackage(10L, 100L))
                                .isInstanceOf(UnauthorizedHostException.class)
                                .hasMessageContaining("permission");

                verify(rentalPackageRepository, never()).delete(any());
        }

        @Test
        void deletePackage_NonHostUser_ThrowsUnauthorizedHostException() {
                when(userService.getCurrentUser()).thenReturn(guestUser);

                assertThatThrownBy(() -> rentalPackageService.deletePackage(10L, 100L))
                                .isInstanceOf(UnauthorizedHostException.class)
                                .hasMessageContaining("HOST role");
        }

        // ── getPackagesByAccommodation ───────────────────────────────────────────

        @Test
        void getPackagesByAccommodation_ReturnsOrderedList() {
                when(accommodationRepository.findByIdAndDeletedFalse(10L))
                                .thenReturn(Optional.of(accommodation));
                when(rentalPackageRepository.findByAccommodationIdOrderByStartDateAsc(10L))
                                .thenReturn(List.of(existingPackage));
                when(rentalPackageMapper.toResponseDTO(existingPackage)).thenReturn(responseDTO);

                List<RentalPackageResponseDTO> result = rentalPackageService.getPackagesByAccommodation(10L);

                assertThat(result).hasSize(1);
                assertThat(result.getFirst().id()).isEqualTo(100L);
        }

        @Test
        void getPackagesByAccommodation_AccommodationNotFound_ThrowsAccommodationNotFoundException() {
                when(accommodationRepository.findByIdAndDeletedFalse(99L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> rentalPackageService.getPackagesByAccommodation(99L))
                                .isInstanceOf(AccommodationNotFoundException.class)
                                .hasMessageContaining("99");
        }
}
