package edu.uniquindio.stayhub_v2.service;

import edu.uniquindio.stayhub_v2.dto.host.HostAccommodationMetricsProjection;
import edu.uniquindio.stayhub_v2.dto.host.HostAccommodationMetricsResponseDTO;
import edu.uniquindio.stayhub_v2.exception.UnauthorizedHostException;
import edu.uniquindio.stayhub_v2.model.Role;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.AccommodationRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HostMetricsServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private HostMetricsService hostMetricsService;

    private User hostUser;
    private User guestUser;

    @BeforeEach
    void setUp() {
        hostUser = User.builder()
                .id(8L)
                .email("host@example.com")
                .roles(Set.of(Role.HOST))
                .build();

        guestUser = User.builder()
                .id(9L)
                .email("guest@example.com")
                .roles(Set.of(Role.GUEST))
                .build();
    }

    @Test
    void getAccommodationMetricsByPeriod_HostUser_ReturnsMappedPage() {
        HostAccommodationMetricsProjection projection = new HostAccommodationMetricsProjection() {
            @Override
            public Long getAccommodationId() {
                return 15L;
            }

            @Override
            public String getAccommodationTitle() {
                return "Cabana familiar con vista al valle";
            }

            @Override
            public String getCity() {
                return "Armenia";
            }

            @Override
            public String getCurrency() {
                return "COP";
            }

            @Override
            public Long getTotalReservationsInPeriod() {
                return 8L;
            }

            @Override
            public Long getActiveReservationsInPeriod() {
                return 5L;
            }

            @Override
            public Long getCancelledReservationsInPeriod() {
                return 3L;
            }

            @Override
            public BigDecimal getReservedRevenueInPeriod() {
                return new BigDecimal("1440000.00");
            }

            @Override
            public Long getPaidDepositsCountInPeriod() {
                return 4L;
            }

            @Override
            public BigDecimal getPaidDepositsAmountInPeriod() {
                return new BigDecimal("288000.00");
            }

            @Override
            public Double getAverageRating() {
                return 4.7;
            }
        };

        when(userService.getCurrentUser()).thenReturn(hostUser);
        when(accommodationRepository.findHostAccommodationMetricsByPeriod(
                eq(hostUser.getId()),
                eq(LocalDateTime.of(2026, 5, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 6, 1, 0, 0)),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(projection)));

        Page<HostAccommodationMetricsResponseDTO> response =
                hostMetricsService.getAccommodationMetricsByPeriod(
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 31),
                        0,
                        10
                );

        assertThat(response.getContent()).hasSize(1);
        HostAccommodationMetricsResponseDTO metrics = response.getContent().getFirst();
        assertThat(metrics.accommodationId()).isEqualTo(15L);
        assertThat(metrics.totalReservationsInPeriod()).isEqualTo(8L);
        assertThat(metrics.activeReservationsInPeriod()).isEqualTo(5L);
        assertThat(metrics.cancelledReservationsInPeriod()).isEqualTo(3L);
        assertThat(metrics.reservedRevenueInPeriod()).isEqualByComparingTo("1440000.00");
        assertThat(metrics.paidDepositsCountInPeriod()).isEqualTo(4L);
        assertThat(metrics.paidDepositsAmountInPeriod()).isEqualByComparingTo("288000.00");
        assertThat(metrics.averageRating()).isEqualTo(4.7);
    }

    @Test
    void getAccommodationMetricsByPeriod_GuestUser_ThrowsUnauthorizedHostException() {
        when(userService.getCurrentUser()).thenReturn(guestUser);

        assertThatThrownBy(() -> hostMetricsService.getAccommodationMetricsByPeriod(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                0,
                10
        ))
                .isInstanceOf(UnauthorizedHostException.class)
                .hasMessageContaining("HOST role");

        verify(accommodationRepository, never()).findHostAccommodationMetricsByPeriod(
                any(), any(), any(), any(Pageable.class));
    }

    @Test
    void getAccommodationMetricsByPeriod_EndDateBeforeStartDate_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> hostMetricsService.getAccommodationMetricsByPeriod(
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 5, 1),
                0,
                10
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End date");
    }

    @Test
    void getAccommodationMetricsByPeriod_InvalidPage_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> hostMetricsService.getAccommodationMetricsByPeriod(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                -1,
                10
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page must be zero or greater");
    }

    @Test
    void getAccommodationMetricsByPeriod_InvalidSize_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> hostMetricsService.getAccommodationMetricsByPeriod(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                0,
                51
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Size must be between 1 and 50");
    }
}
