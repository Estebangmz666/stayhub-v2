package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.review.CreateReviewRequestDTO;
import edu.uniquindio.stayhub_v2.dto.review.RespondReviewRequestDTO;
import edu.uniquindio.stayhub_v2.dto.review.ReviewResponseDTO;
import edu.uniquindio.stayhub_v2.exception.AccommodationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ReservationNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ReviewNotFoundException;
import edu.uniquindio.stayhub_v2.exception.ReviewPolicyViolationException;
import edu.uniquindio.stayhub_v2.mapper.ReviewMapper;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import edu.uniquindio.stayhub_v2.model.Review;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import edu.uniquindio.stayhub_v2.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class that manages guest reviews and host responses.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final AccommodationRepository accommodationRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final AuthorizationService authorizationService;

    @Transactional
    public ReviewResponseDTO createReview(
            Long accommodationId,
            CreateReviewRequestDTO createReviewRequestDTO) {

        log.info("Creating review for accommodation {} and reservation {}",
                accommodationId,
                createReviewRequestDTO.reservationId());

        Accommodation accommodation = accommodationRepository.findByIdAndDeletedFalse(accommodationId)
                .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));

        User currentUser = userService.getCurrentUser();

        authorizationService.requireGuestRole(currentUser);

        Reservation reservation = reservationRepository.findById(createReviewRequestDTO.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation with ID " + createReviewRequestDTO.reservationId() + " not found"
                ));

        validateReviewCreation(accommodation, reservation, currentUser);

        if (reviewRepository.existsByReservationId(reservation.getId())) {
            throw new ReviewPolicyViolationException(
                    "A review has already been created for this reservation");
        }

        Review review = Review.builder()
                .accommodation(accommodation)
                .guest(currentUser)
                .reservation(reservation)
                .rating(createReviewRequestDTO.rating())
                .comment(createReviewRequestDTO.comment())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review {} created successfully", savedReview.getId());

        return reviewMapper.toResponseDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAccommodationReviews(Long accommodationId) {

        log.info("Retrieving reviews for accommodation {}", accommodationId);

        accommodationRepository.findByIdAndDeletedFalse(accommodationId)
                .orElseThrow(() -> new AccommodationNotFoundException("Accommodation not found"));

        return reviewRepository.findByAccommodationIdOrderByCreatedAtDesc(accommodationId)
                .stream()
                .map(reviewMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public ReviewResponseDTO respondToReview(
            Long reviewId,
            RespondReviewRequestDTO respondReviewRequestDTO) {

        log.info("Responding to review {}", reviewId);

        User currentUser = userService.getCurrentUser();

        authorizationService.requireHostRole(currentUser);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review with ID " + reviewId + " not found"
                ));

        if (!review.getAccommodation().getHost().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the host of the accommodation can answer this review");
        }

        if (review.getHostResponse() != null && !review.getHostResponse().isBlank()) {
            throw new ReviewPolicyViolationException("This review has already been answered by the host");
        }

        review.setHostResponse(respondReviewRequestDTO.response());
        review.setHostRespondedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);
        log.info("Host response saved for review {}", updatedReview.getId());

        return reviewMapper.toResponseDTO(updatedReview);
    }

    private void validateReviewCreation(
            Accommodation accommodation,
            Reservation reservation,
            User currentUser) {

        if (!reservation.getGuest().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the guest who completed the stay can create a review");
        }

        if (!reservation.getAccommodation().getId().equals(accommodation.getId())) {
            throw new ReviewPolicyViolationException(
                    "Reservation does not belong to the specified accommodation");
        }

        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new ReviewPolicyViolationException("Only completed stays can be reviewed");
        }
    }
}