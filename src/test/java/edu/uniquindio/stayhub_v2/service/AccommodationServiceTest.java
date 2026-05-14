package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ActiveReservationsException;
import edu.uniquindio.stayhub_v2.exception.UnauthorizedHostException;
import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.dto.accommodation.CreateAccommodationRequestDTO;
import edu.uniquindio.stayhub_v2.dto.accommodation.CreateAccommodationResponseDTO;
import edu.uniquindio.stayhub_v2.dto.accommodation.UnavailableAccommodationDateRangeResponseDTO;
import edu.uniquindio.stayhub_v2.mapper.AccommodationMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.Role;
import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccommodationServiceTest {

        @Mock
        private AccommodationRepository accommodationRepository;

        @Mock
        private ReservationRepository reservationRepository;

        @Mock
        private AccommodationMapper accommodationMapper;

        @Mock
        private UserService userService;

        @InjectMocks
        private AccommodationService accommodationService;

        private Accommodation testAccommodation;

        private User hostUser;

        @BeforeEach
        void setUp() {
                hostUser = User.builder()
                        .id(1L)
                        .email("host@example.com")
                        .roles(Set.of(Role.HOST))
                        .build();

                testAccommodation = Accommodation.builder()
                        .id(100L)
                        .host(hostUser)
                        .deleted(false)
                        .available(true)
                        .build();
        }

        @Test
        void createAccommodation_HostUser_PersistsAccommodationAndReturnsResponse() {
                CreateAccommodationRequestDTO requestDTO = new CreateAccommodationRequestDTO(
                                "Cabana familiar con vista al valle",
                                "Cabana campestre equipada para familias.",
                                6,
                                "COP",
                                new BigDecimal("180000.00"),
                                "https://images.example.com/accommodations/main/cabana-valle.jpg",
                                -75.6811,
                                4.5339,
                                "A 10 minutos del Parque del Cafe.",
                                "Armenia",
                                List.of("https://images.example.com/accommodations/gallery/cabana-valle-sala.jpg"));

                Accommodation mappedAccommodation = Accommodation.builder()
                                .title(requestDTO.title())
                                .description(requestDTO.description())
                                .capacity(requestDTO.capacity())
                                .currency(Currency.getInstance("COP"))
                                .pricePerNight(requestDTO.pricePerNight())
                                .mainImage(requestDTO.mainImage())
                                .longitude(requestDTO.longitude())
                                .latitude(requestDTO.latitude())
                                .locationDescription(requestDTO.locationDescription())
                                .city(requestDTO.city())
                                .images(requestDTO.images())
                                .build();

                Accommodation savedAccommodation = Accommodation.builder()
                                .id(15L)
                                .host(testAccommodation.getHost())
                                .title(requestDTO.title())
                                .description(requestDTO.description())
                                .capacity(requestDTO.capacity())
                                .currency(Currency.getInstance("COP"))
                                .pricePerNight(requestDTO.pricePerNight())
                                .mainImage(requestDTO.mainImage())
                                .longitude(requestDTO.longitude())
                                .latitude(requestDTO.latitude())
                                .locationDescription(requestDTO.locationDescription())
                                .city(requestDTO.city())
                                .images(requestDTO.images())
                                .available(true)
                                .deleted(false)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                CreateAccommodationResponseDTO responseDTO = new CreateAccommodationResponseDTO(
                                15L,
                                1L,
                                "host@example.com",
                                requestDTO.title(),
                                requestDTO.description(),
                                requestDTO.capacity(),
                                "COP",
                                requestDTO.pricePerNight(),
                                requestDTO.mainImage(),
                                requestDTO.longitude(),
                                requestDTO.latitude(),
                                requestDTO.locationDescription(),
                                requestDTO.city(),
                                requestDTO.images(),
                                true,
                                savedAccommodation.getCreatedAt(),
                                savedAccommodation.getUpdatedAt());

                when(userService.getCurrentUser()).thenReturn(testAccommodation.getHost());
                when(accommodationMapper.toEntity(requestDTO)).thenReturn(mappedAccommodation);
                when(accommodationRepository.save(any(Accommodation.class))).thenReturn(savedAccommodation);
                when(accommodationMapper.toCreateAccommodationResponseDTO(savedAccommodation)).thenReturn(responseDTO);

                CreateAccommodationResponseDTO response = accommodationService.createAccommodation(requestDTO);

                assertThat(response.id()).isEqualTo(15L);
                assertThat(response.hostEmail()).isEqualTo("host@example.com");
                assertThat(response.available()).isTrue();

                verify(accommodationRepository).save(argThat(accommodation ->
                                accommodation.getHost().equals(testAccommodation.getHost())
                                                && accommodation.isAvailable()
                                                && !accommodation.isDeleted()));
        }

        @Test
        void createAccommodation_NonHostUser_ThrowsUnauthorizedHostException() {
                User guestUser = User.builder()
                                .id(2L)
                                .email("guest@example.com")
                                .roles(Set.of(Role.GUEST))
                                .build();

                CreateAccommodationRequestDTO requestDTO = new CreateAccommodationRequestDTO(
                                "Cabana familiar con vista al valle",
                                "Cabana campestre equipada para familias.",
                                6,
                                "COP",
                                new BigDecimal("180000.00"),
                                "https://images.example.com/accommodations/main/cabana-valle.jpg",
                                -75.6811,
                                4.5339,
                                "A 10 minutos del Parque del Cafe.",
                                "Armenia",
                                List.of("https://images.example.com/accommodations/gallery/cabana-valle-sala.jpg"));

                when(userService.getCurrentUser()).thenReturn(guestUser);

                assertThatThrownBy(() -> accommodationService.createAccommodation(requestDTO))
                                .isInstanceOf(UnauthorizedHostException.class)
                                .hasMessageContaining("HOST role");

                verify(accommodationMapper, never()).toEntity(any());
                verify(accommodationRepository, never()).save(any());
        }

        @Test
        void deactivateAccommodation_Successful() {
                when(userService.getCurrentUser()).thenReturn(hostUser);

                when(accommodationRepository.findByIdAndDeletedFalse(100L))
                        .thenReturn(Optional.of(testAccommodation));

                when(reservationRepository.existsByAccommodationIdAndStartDateAfterAndStatus(
                        eq(100L),
                        any(LocalDateTime.class),
                        eq(ReservationStatus.ACTIVE)
                )).thenReturn(false);

                accommodationService.deactivateAccommodation(100L);

                assertThat(testAccommodation.isDeleted()).isTrue();
                assertThat(testAccommodation.isAvailable()).isFalse();

                verify(userService).getCurrentUser();
                verify(accommodationRepository).findByIdAndDeletedFalse(100L);
                verify(reservationRepository).existsByAccommodationIdAndStartDateAfterAndStatus(
                        eq(100L),
                        any(LocalDateTime.class),
                        eq(ReservationStatus.ACTIVE)
                );
                verify(accommodationRepository).save(testAccommodation);
        }

        @Test
        void deactivateAccommodation_UnauthorizedHost_ThrowsException() {
                User otherUser = new User();
                otherUser.setId(999L);
                otherUser.setEmail("otheruser@example.com");

                when(userService.getCurrentUser()).thenReturn(otherUser);

                when(accommodationRepository.findByIdAndDeletedFalse(100L))
                        .thenReturn(Optional.of(testAccommodation));

                assertThatThrownBy(() -> accommodationService.deactivateAccommodation(100L))
                        .isInstanceOf(UnauthorizedHostException.class)
                        .hasMessageContaining("You do not have permissions to deactivate this rural house.");

                assertThat(testAccommodation.isDeleted()).isFalse();

                verify(userService).getCurrentUser();
                verify(accommodationRepository).findByIdAndDeletedFalse(100L);
                verify(reservationRepository, never()).existsByAccommodationIdAndStartDateAfterAndStatus(
                        any(),
                        any(),
                        any()
                );
                verify(accommodationRepository, never()).save(any());
        }

        @Test
        void deactivateAccommodation_ActiveReservationsExist_ThrowsException() {
                when(userService.getCurrentUser()).thenReturn(hostUser);

                when(accommodationRepository.findByIdAndDeletedFalse(100L))
                        .thenReturn(Optional.of(testAccommodation));

                when(reservationRepository.existsByAccommodationIdAndStartDateAfterAndStatus(
                        eq(100L),
                        any(LocalDateTime.class),
                        eq(ReservationStatus.ACTIVE)
                )).thenReturn(true);

                assertThatThrownBy(() -> accommodationService.deactivateAccommodation(100L))
                        .isInstanceOf(ActiveReservationsException.class)
                        .hasMessageContaining("future reservations");

                assertThat(testAccommodation.isDeleted()).isFalse();
                assertThat(testAccommodation.isAvailable()).isTrue();

                verify(userService).getCurrentUser();
                verify(accommodationRepository).findByIdAndDeletedFalse(100L);
                verify(reservationRepository).existsByAccommodationIdAndStartDateAfterAndStatus(
                        eq(100L),
                        any(LocalDateTime.class),
                        eq(ReservationStatus.ACTIVE)
                );
                verify(accommodationRepository, never()).save(any());
        }

        @Test
        void deactivateAccommodation_NotFound_ThrowsException() {
                when(userService.getCurrentUser()).thenReturn(hostUser);

                when(accommodationRepository.findByIdAndDeletedFalse(999L))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> accommodationService.deactivateAccommodation(999L))
                        .isInstanceOf(AccommodationNotFoundException.class)
                        .hasMessageContaining("Accommodation not found with id: 999");

                verify(userService).getCurrentUser();
                verify(accommodationRepository).findByIdAndDeletedFalse(999L);
                verify(reservationRepository, never()).existsByAccommodationIdAndStartDateAfterAndStatus(
                        any(),
                        any(),
                        any()
                );
                verify(accommodationRepository, never()).save(any());
        }

        @Test
        void getAccommodation_Successful() {
                AccommodationGetByIdResponseDTO mockResponse = new AccommodationGetByIdResponseDTO(
                                null, "Title", "Desc", 4, new java.math.BigDecimal("100"), "main.jpg", "loc", "city",
                                java.util.List.of(), true);
                when(accommodationRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(testAccommodation));
                when(accommodationMapper.toAccommodationGetByIdResponseDTO(testAccommodation)).thenReturn(mockResponse);

                AccommodationGetByIdResponseDTO response = accommodationService.getAccommodation(100L);

                assertThat(response).isNotNull();
                assertThat(response.title()).isEqualTo("Title");
        }

        @Test
        void getAccommodation_NotFound_ThrowsException() {
                when(accommodationRepository.findByIdAndDeletedFalse(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> accommodationService.getAccommodation(999L))
                                .isInstanceOf(AccommodationNotFoundException.class)
                                .hasMessageContaining("Accommodation not found with id");
        }

        @Test
        void searchAccommodationsByCity_ValidCity_ReturnsBasicAccommodationData() {
                testAccommodation.setTitle("Cabana en Armenia");
                testAccommodation.setCity("Armenia");
                testAccommodation.setCapacity(4);
                testAccommodation.setPricePerNight(new BigDecimal("180000.00"));
                testAccommodation.setCurrency(Currency.getInstance("COP"));
                testAccommodation.setMainImage("https://images.example.com/cabana.jpg");

                when(accommodationRepository.findByCityContainingIgnoreCaseAndDeletedFalseAndAvailableTrue(
                                eq("Armenia"),
                                any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(testAccommodation)));

                Page<?> response = accommodationService.searchAccommodationsByCity(" Armenia ", 0, 10);

                assertThat(response.getContent()).hasSize(1);
                Object firstResult = response.getContent().getFirst();
                assertThat(firstResult)
                                .hasFieldOrPropertyWithValue("accommodationCode", 100L)
                                .hasFieldOrPropertyWithValue("title", "Cabana en Armenia")
                                .hasFieldOrPropertyWithValue("city", "Armenia")
                                .hasFieldOrPropertyWithValue("capacity", 4)
                                .hasFieldOrPropertyWithValue("currency", "COP");

                verify(accommodationRepository)
                                .findByCityContainingIgnoreCaseAndDeletedFalseAndAvailableTrue(
                                                eq("Armenia"),
                                                any(Pageable.class));
        }

        @Test
        void searchAccommodationsByCity_BlankCity_ThrowsIllegalArgumentException() {
                assertThatThrownBy(() -> accommodationService.searchAccommodationsByCity("   ", 0, 10))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("City is required");
        }

        @Test
        void searchAccommodationsByCity_InvalidPage_ThrowsIllegalArgumentException() {
                assertThatThrownBy(() -> accommodationService.searchAccommodationsByCity("Armenia", -1, 10))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Page must be zero or greater");
        }

        @Test
        void searchAccommodationsByCity_InvalidSize_ThrowsIllegalArgumentException() {
                assertThatThrownBy(() -> accommodationService.searchAccommodationsByCity("Armenia", 0, 51))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Size must be between 1 and 50");
        }

        @Test
        void getUnavailableDateRanges_ExistingAccommodation_ReturnsActiveBlockedRanges() {
                UnavailableAccommodationDateRangeResponseDTO blockedRange =
                                new UnavailableAccommodationDateRangeResponseDTO(
                                                LocalDateTime.of(2026, 6, 10, 15, 0),
                                                LocalDateTime.of(2026, 6, 13, 11, 0)
                                );

                when(accommodationRepository.findByIdAndDeletedFalse(100L))
                                .thenReturn(Optional.of(testAccommodation));
                when(reservationRepository.findUnavailableDateRangesByAccommodationId(
                                eq(100L),
                                any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(blockedRange)));

                Page<UnavailableAccommodationDateRangeResponseDTO> response =
                                accommodationService.getUnavailableDateRanges(100L, 0, 10);

                assertThat(response.getContent()).hasSize(1);
                assertThat(response.getContent().getFirst().startDate())
                                .isEqualTo(LocalDateTime.of(2026, 6, 10, 15, 0));
                assertThat(response.getContent().getFirst().endDate())
                                .isEqualTo(LocalDateTime.of(2026, 6, 13, 11, 0));

                verify(reservationRepository).findUnavailableDateRangesByAccommodationId(
                                eq(100L),
                                any(Pageable.class));
        }

        @Test
        void getUnavailableDateRanges_AccommodationNotFound_ThrowsException() {
                when(accommodationRepository.findByIdAndDeletedFalse(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> accommodationService.getUnavailableDateRanges(999L, 0, 10))
                                .isInstanceOf(AccommodationNotFoundException.class)
                                .hasMessageContaining("Accommodation not found with id: 999");

                verify(reservationRepository, never())
                                .findUnavailableDateRangesByAccommodationId(any(), any(Pageable.class));
        }

        @Test
        void getUnavailableDateRanges_InvalidAccommodationId_ThrowsIllegalArgumentException() {
                assertThatThrownBy(() -> accommodationService.getUnavailableDateRanges(0L, 0, 10))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Accommodation ID must be positive");
        }
}
