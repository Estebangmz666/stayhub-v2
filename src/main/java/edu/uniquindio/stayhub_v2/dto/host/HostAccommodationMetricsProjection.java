package edu.uniquindio.stayhub_v2.dto.host;

import java.math.BigDecimal;

/**
 * Internal projection used to retrieve aggregated host accommodation metrics
 * from repository queries.
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 */
public interface HostAccommodationMetricsProjection {

    Long getAccommodationId();

    String getAccommodationTitle();

    String getCity();

    String getCurrency();

    Long getTotalReservationsInPeriod();

    Long getActiveReservationsInPeriod();

    Long getCancelledReservationsInPeriod();

    BigDecimal getReservedRevenueInPeriod();

    Long getPaidDepositsCountInPeriod();

    BigDecimal getPaidDepositsAmountInPeriod();

    Double getAverageRating();
}
