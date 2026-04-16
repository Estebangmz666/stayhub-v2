package edu.uniquindio.stayhub_v2.controller;

import edu.uniquindio.stayhub_v2.dto.notification.PaymentNotificationDTO;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import edu.uniquindio.stayhub_v2.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for in-app notifications related to pending payments.
 *
 * <p>
 * Provides an endpoint the frontend can poll to determine whether the
 * currently authenticated guest has reservation payments pending so that
 * it can display the appropriate modal or alert.
 * </p>
 *
 * @author StayHub Dev Team
 * @version 1.0
 */
@Tag(name = "Notifications", description = "Endpoints for in-app notifications")
@RestController
@RequestMapping("/api/v2/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

        private final ReservationRepository reservationRepository;
        private final UserService userService;

        @Value("${stayhub.payment.bank-account}")
        private String bankAccountNumber;

        /**
         * Returns all pending deposit payment notifications for the authenticated
         * guest.
         *
         * <p>
         * The frontend should call this endpoint after login or when navigating to the
         * reservations section to decide whether to display a payment reminder modal.
         * </p>
         *
         * @return List of {@link PaymentNotificationDTO} with details of pending
         *         payments.
         *         Returns an empty list if no payments are pending.
         */
        @Operation(summary = "Get pending payment notifications", description = "Returns all active reservations for the authenticated guest that have a pending deposit payment (depositPaid = false) and a deadline that has not yet expired.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "List of pending payment notifications (may be empty)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentNotificationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized — valid JWT token required")
        })
        @GetMapping("/payment-pending")
        public ResponseEntity<List<PaymentNotificationDTO>> getPendingPaymentNotifications() {
                Long guestId = userService.getCurrentUser().getId();
                log.debug("Fetching pending payment notifications for guest ID: {}", guestId);

                List<Reservation> pending = reservationRepository
                                .findPendingDepositsByGuest(guestId, LocalDateTime.now());

                List<PaymentNotificationDTO> notifications = pending.stream()
                                .map(r -> new PaymentNotificationDTO(
                                                r.getId(),
                                                r.getAccommodation().getTitle(),
                                                r.getDepositAmount(),
                                                r.getCurrency(),
                                                bankAccountNumber,
                                                r.getPaymentDeadline()))
                                .toList();

                log.debug("Found {} pending payment notification(s) for guest ID: {}", notifications.size(), guestId);
                return ResponseEntity.ok(notifications);
        }
}
