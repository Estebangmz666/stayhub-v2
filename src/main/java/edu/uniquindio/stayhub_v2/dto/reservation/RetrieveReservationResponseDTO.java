package edu.uniquindio.stayhub_v2.dto.reservation;

import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "RetrieveReservationResponse", description = "Response DTO for retrieving a reservation.")
public record RetrieveReservationResponseDTO(

        @NotNull @Schema(description = "Unique identifier of the reservation", example = "12345")
        Long id,

        @NotNull @Schema(description = "Check-in date and time", example = "2025-06-01T15:00:00")
        LocalDateTime startDate,

        @NotNull @Schema(description = "Check-out date and time", example = "2025-06-05T11:00:00")
        LocalDateTime endDate,

        @NotNull @Schema(description = "ID of the booked accommodation", example = "1")
        Long accommodationId,

        @NotNull @Schema(description = "Title of the booked accommodation", example = "Beachfront Villa with Pool")
        String accommodationTitle,

        @NotNull @Schema(description = "City of the booked accommodation", example = "Medellín")
        String accommodationCity,

        @NotNull @Schema(description = "ID of the guest who made the reservation", example = "678")
        Long guestId,

        @NotNull @Schema(description = "Email of the guest who made the reservation", example = "johndoe@gmail.com")
        String guestEmail,

        @NotNull @Schema(description = "Total price of the reservation", example = "1250000")
        BigDecimal totalPrice,

        @NotNull @Schema(description = "Currency of the total price", example = "COP")
        String currency,

        @NotNull @Schema(description = "Required advance deposit (20% of total price). Must be paid within the payment deadline.", example = "200000")
        BigDecimal depositAmount,

        @NotNull @Schema(description = "Flag that indicates whether the guest has paid the 20% minimum deposit coverage", example = "true")
        Boolean depositPaid,

        @NotNull @Schema(description = "Datetime that defines the last possible moment the guest can pay the reservation deposit")
        LocalDateTime paymentDeadline,

        @NotNull @Schema(description = "Current status of the reservation", example = "ACTIVE")
        ReservationStatus status,

        @NotNull @Schema(description = "Datetime that defines when the reservation was created", example = "2025-06-01T15:00:00")
        LocalDateTime createdAt,

        @NotNull @Schema(description = "Datetime that defines when the reservation was last updated", example = "2025-06-01T15:00:00")
        LocalDateTime updatedAt
) {}