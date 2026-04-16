package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ActiveReservationsException;
import edu.uniquindio.stayhub_v2.exception.UnauthorizedHostException;
import edu.uniquindio.stayhub_v2.dto.accommodation.AccommodationGetByIdResponseDTO;
import edu.uniquindio.stayhub_v2.mapper.AccommodationMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        @InjectMocks
        private AccommodationService accommodationService;

        private Accommodation testAccommodation;

        @BeforeEach
        void setUp() {
                User hostUser = User.builder()
                                .id(1L)
                                .email("host@example.com")
                                .build();

                testAccommodation = Accommodation.builder()
                                .id(100L)
                                .host(hostUser)
                                .deleted(false)
                                .available(true)
                                .build();
        }

        @Test
        void deactivateAccommodation_Successful() {
                when(accommodationRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(testAccommodation));
                when(reservationRepository.existsByAccommodationIdAndStartDateAfterAndStatus(
                                eq(100L), any(LocalDateTime.class), eq(ReservationStatus.ACTIVE))).thenReturn(false);

                accommodationService.deactivateAccommodation(100L, "host@example.com");

                assertThat(testAccommodation.isDeleted()).isTrue();
                assertThat(testAccommodation.isAvailable()).isFalse();
                verify(accommodationRepository).save(testAccommodation);
        }

        @Test
        void deactivateAccommodation_UnauthorizedHost_ThrowsException() {
                when(accommodationRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(testAccommodation));

                assertThatThrownBy(() -> accommodationService.deactivateAccommodation(100L, "otheruser@example.com"))
                                .isInstanceOf(UnauthorizedHostException.class)
                                .hasMessageContaining("No tienes permisos para dar de baja esta casa rural.");

                assertThat(testAccommodation.isDeleted()).isFalse();
                verify(reservationRepository, never()).existsByAccommodationIdAndStartDateAfterAndStatus(any(), any(),
                                any());
                verify(accommodationRepository, never()).save(any());
        }

        @Test
        void deactivateAccommodation_ActiveReservationsExist_ThrowsException() {
                when(accommodationRepository.findByIdAndDeletedFalse(100L)).thenReturn(Optional.of(testAccommodation));
                when(reservationRepository.existsByAccommodationIdAndStartDateAfterAndStatus(
                                eq(100L), any(LocalDateTime.class), eq(ReservationStatus.ACTIVE))).thenReturn(true);

                assertThatThrownBy(() -> accommodationService.deactivateAccommodation(100L, "host@example.com"))
                                .isInstanceOf(ActiveReservationsException.class)
                                .hasMessageContaining("reservas futuras");

                assertThat(testAccommodation.isDeleted()).isFalse();
                verify(accommodationRepository, never()).save(any());
        }

        @Test
        void deactivateAccommodation_NotFound_ThrowsException() {
                when(accommodationRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> accommodationService.deactivateAccommodation(999L, "host@example.com"))
                                .isInstanceOf(AccommodationNotFoundException.class);
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
}
