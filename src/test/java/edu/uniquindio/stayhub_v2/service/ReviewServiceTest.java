package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.review.CreateReviewRequestDTO;
import edu.uniquindio.stayhub_v2.dto.review.RespondReviewRequestDTO;
import edu.uniquindio.stayhub_v2.dto.review.ReviewResponseDTO;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ReviewPolicyViolationException;
import edu.uniquindio.stayhub_v2.mapper.ReviewMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import edu.uniquindio.stayhub_v2.model.Review;
import edu.uniquindio.stayhub_v2.model.Role;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import edu.uniquindio.stayhub_v2.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewService reviewService;

    private Accommodation accommodation;
    private User guest;
    private User host;
    private Reservation reservation;
    private Review review;

    @BeforeEach
    void setUp() {
        guest = User.builder()
                .id(1L)
                .email("guest@mail.com")
                .fullName("Juan Perez")
                .roles(Set.of(Role.GUEST))
                .build();

        host = User.builder()
                .id(2L)
                .email("host@mail.com")
                .fullName("Laura Host")
                .roles(Set.of(Role.HOST))
                .build();

        accommodation = Accommodation.builder()
                .id(10L)
                .title("Cabana en Quindio")
                .host(host)
                .deleted(false)
                .build();

        reservation = Reservation.builder()
                .id(100L)
                .guest(guest)
                .accommodation(accommodation)
                .startDate(LocalDateTime.now().minusDays(10))
                .endDate(LocalDateTime.now().minusDays(7))
                .status(ReservationStatus.COMPLETED)
                .build();

        review = Review.builder()
                .id(200L)
                .accommodation(accommodation)
                .guest(guest)
                .reservation(reservation)
                .rating(5)
                .comment("Excelente alojamiento.")
                .build();
        review.setCreatedAt(LocalDateTime.now().minusDays(1));
        review.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    private ReviewResponseDTO responseDto() {
        return new ReviewResponseDTO(
                200L,
                accommodation.getId(),
                guest.getId(),
                guest.getFullName(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                5,
                "Excelente alojamiento.",
                review.getHostResponse(),
                review.getHostRespondedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    @Test
    void createReview_CompletedReservation_CreatesReviewSuccessfully() {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO(100L, 5, "Excelente alojamiento.");

        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(accommodation));
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByReservationId(100L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponseDTO(review)).thenReturn(responseDto());

        ReviewResponseDTO response = reviewService.createReview(10L, request);

        assertThat(response.id()).isEqualTo(200L);
        assertThat(response.rating()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_NonCompletedReservation_ThrowsReviewPolicyViolationException() {
        reservation.setStatus(ReservationStatus.ACTIVE);
        CreateReviewRequestDTO request = new CreateReviewRequestDTO(100L, 4, "Buen lugar.");

        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(accommodation));
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reviewService.createReview(10L, request))
                .isInstanceOf(ReviewPolicyViolationException.class)
                .hasMessageContaining("completed stays");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_DuplicateReservationReview_ThrowsReviewPolicyViolationException() {
        CreateReviewRequestDTO request = new CreateReviewRequestDTO(100L, 5, "Excelente alojamiento.");

        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(accommodation));
        when(userService.getCurrentUser()).thenReturn(guest);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByReservationId(100L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(10L, request))
                .isInstanceOf(ReviewPolicyViolationException.class)
                .hasMessageContaining("already been created");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_DifferentGuest_ThrowsAccessDeniedException() {
        User otherGuest = User.builder()
                .id(99L)
                .email("other@mail.com")
                .fullName("Other Guest")
                .roles(Set.of(Role.GUEST))
                .build();

        CreateReviewRequestDTO request = new CreateReviewRequestDTO(100L, 5, "Excelente alojamiento.");

        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(accommodation));
        when(userService.getCurrentUser()).thenReturn(otherGuest);
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reviewService.createReview(10L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only the guest");
    }

    @Test
    void getAccommodationReviews_ExistingAccommodation_ReturnsMappedReviews() {
        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(accommodation));
        when(reviewRepository.findByAccommodationIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(review));
        when(reviewMapper.toResponseDTO(review)).thenReturn(responseDto());

        List<ReviewResponseDTO> response = reviewService.getAccommodationReviews(10L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().guestName()).isEqualTo("Juan Perez");
    }

    @Test
    void getAccommodationReviews_AccommodationNotFound_ThrowsAccommodationNotFoundException() {
        when(accommodationRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getAccommodationReviews(10L))
                .isInstanceOf(AccommodationNotFoundException.class);
    }

    @Test
    void respondToReview_HostOwner_SavesResponse() {
        RespondReviewRequestDTO request = new RespondReviewRequestDTO("Gracias por tu visita.");

        when(userService.getCurrentUser()).thenReturn(host);
        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewMapper.toResponseDTO(any(Review.class))).thenReturn(responseDto());

        ReviewResponseDTO response = reviewService.respondToReview(200L, request);

        assertThat(response.id()).isEqualTo(200L);
        assertThat(review.getHostResponse()).isEqualTo("Gracias por tu visita.");
        assertThat(review.getHostRespondedAt()).isNotNull();
    }

    @Test
    void respondToReview_NonHost_ThrowsAccessDeniedException() {
        RespondReviewRequestDTO request = new RespondReviewRequestDTO("Gracias por tu visita.");

        when(userService.getCurrentUser()).thenReturn(guest);
        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.respondToReview(200L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only the host");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void respondToReview_AlreadyAnswered_ThrowsReviewPolicyViolationException() {
        review.setHostResponse("Respuesta previa");
        review.setHostRespondedAt(LocalDateTime.now().minusHours(1));
        RespondReviewRequestDTO request = new RespondReviewRequestDTO("Nueva respuesta.");

        when(userService.getCurrentUser()).thenReturn(host);
        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.respondToReview(200L, request))
                .isInstanceOf(ReviewPolicyViolationException.class)
                .hasMessageContaining("already been answered");

        verify(reviewRepository, never()).save(any());
    }
}
