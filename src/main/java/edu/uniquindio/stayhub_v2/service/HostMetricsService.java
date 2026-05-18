package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.host.HostAccommodationMetricsProjection;
import edu.uniquindio.stayhub_v2.dto.host.HostAccommodationMetricsResponseDTO;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service that exposes accommodation performance metrics for authenticated hosts.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HostMetricsService {

    private static final int MAX_PAGE_SIZE = 50;

    private final AccommodationRepository accommodationRepository;
    private final UserService userService;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public Page<HostAccommodationMetricsResponseDTO> getAccommodationMetricsByPeriod(
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        validateRequestedPeriod(startDate, endDate);
        validatePageRequest(page, size);

        User currentUser = userService.getCurrentUser();
        authorizationService.requireHostRole(currentUser);

        LocalDateTime periodStart = startDate.atStartOfDay();
        LocalDateTime periodEndExclusive = endDate.plusDays(1).atStartOfDay();
        Pageable pageable = PageRequest.of(page, size);

        log.info("Retrieving host accommodation metrics for user {} between {} and {}",
                currentUser.getEmail(), startDate, endDate);

        return accommodationRepository.findHostAccommodationMetricsByPeriod(
                        currentUser.getId(),
                        periodStart,
                        periodEndExclusive,
                        pageable
                )
                .map(this::toResponseDTO);
    }

    private void validateRequestedPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date is required");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date is required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be equal to or after start date");
        }
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Size must be between 1 and 50");
        }
    }

    private HostAccommodationMetricsResponseDTO toResponseDTO(
            HostAccommodationMetricsProjection projection) {
        return new HostAccommodationMetricsResponseDTO(
                projection.getAccommodationId(),
                projection.getAccommodationTitle(),
                projection.getCity(),
                projection.getCurrency(),
                projection.getTotalReservationsInPeriod(),
                projection.getActiveReservationsInPeriod(),
                projection.getCancelledReservationsInPeriod(),
                projection.getReservedRevenueInPeriod(),
                projection.getPaidDepositsCountInPeriod(),
                projection.getPaidDepositsAmountInPeriod(),
                projection.getAverageRating()
        );
    }
}