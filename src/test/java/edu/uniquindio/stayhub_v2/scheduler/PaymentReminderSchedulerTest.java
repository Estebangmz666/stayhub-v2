package edu.uniquindio.stayhub_v2.scheduler;

import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.model.ReservationStatus;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.ReservationRepository;
import edu.uniquindio.stayhub_v2.service.EmailService;
import edu.uniquindio.stayhub_v2.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentReminderScheduler}.
 *
 * <p>Verifies that the scheduler correctly identifies reservations with approaching
 * payment deadlines and triggers reminder emails for each.</p>
 */
@ExtendWith(MockitoExtension.class)
class PaymentReminderSchedulerTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private PaymentReminderScheduler scheduler;

    private Reservation reservationWithApproachingDeadline;
    private User guest;
    private Accommodation accommodation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "bankAccountNumber", "3001234567890");

        guest = User.builder()
                .id(1L)
                .email("guest@mail.com")
                .fullName("Ana García")
                .build();

        accommodation = Accommodation.builder()
                .id(10L)
                .title("Finca en Armenia")
                .build();

        reservationWithApproachingDeadline = new Reservation();
        reservationWithApproachingDeadline.setId(200L);
        reservationWithApproachingDeadline.setGuest(guest);
        reservationWithApproachingDeadline.setAccommodation(accommodation);
        reservationWithApproachingDeadline.setTotalPrice(new BigDecimal("400000"));
        reservationWithApproachingDeadline.setCurrency(Currency.getInstance("COP"));
        reservationWithApproachingDeadline.setDepositAmount(new BigDecimal("80000.00"));
        reservationWithApproachingDeadline.setDepositPaid(false);
        reservationWithApproachingDeadline.setStatus(ReservationStatus.ACTIVE);
        reservationWithApproachingDeadline.setPaymentDeadline(LocalDateTime.now().plusHours(12));
        reservationWithApproachingDeadline.setStartDate(LocalDateTime.now().plusDays(5));
        reservationWithApproachingDeadline.setEndDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    void sendPaymentReminders_WithPendingReservations_SendsEmailToEachGuest() {
        // Arrange
        when(reservationRepository.findReservationsWithPaymentDeadlineApproaching(any(), any()))
                .thenReturn(List.of(reservationWithApproachingDeadline));

        // Act
        scheduler.sendPaymentReminders();

        // Assert — one email sent for one reservation
        verify(emailService, times(1)).sendEmailWithTemplate(
                eq(guest.getEmail()),
                contains("Finca en Armenia"),
                eq("payment-reminder"),
                any(Context.class)
        );
    }

    @Test
    void sendPaymentReminders_NoPendingReservations_DoesNotSendEmails() {
        // Arrange
        when(reservationRepository.findReservationsWithPaymentDeadlineApproaching(any(), any()))
                .thenReturn(List.of());

        // Act
        scheduler.sendPaymentReminders();

        // Assert — no emails sent
        verify(emailService, never()).sendEmailWithTemplate(any(), any(), any(), any());
    }

    @Test
    void sendPaymentReminders_EmailFailureOnOneReservation_ContinuesWithOthers() {
        // Arrange — two reservations, first one causes an email failure
        Reservation second = new Reservation();
        second.setId(201L);
        second.setGuest(User.builder()
                .id(2L).email("second@mail.com").fullName("Carlos López").build());
        second.setAccommodation(accommodation);
        second.setDepositAmount(new BigDecimal("60000"));
        second.setPaymentDeadline(LocalDateTime.now().plusHours(6));
        second.setStatus(ReservationStatus.ACTIVE);

        when(reservationRepository.findReservationsWithPaymentDeadlineApproaching(any(), any()))
                .thenReturn(List.of(reservationWithApproachingDeadline, second));

        // First email throws, second should still be attempted
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmailWithTemplate(eq(guest.getEmail()), any(), any(), any());

        // Act — should NOT throw despite the first failure
        scheduler.sendPaymentReminders();

        // Assert — second email was still attempted
        verify(emailService, times(1)).sendEmailWithTemplate(
                eq("second@mail.com"), any(), eq("payment-reminder"), any(Context.class)
        );
    }

    @Test
    void cancelExpiredUnpaidReservations_DelegatesToReservationService() {
        when(reservationService.cancelExpiredUnpaidReservations()).thenReturn(2);

        scheduler.cancelExpiredUnpaidReservations();

        verify(reservationService).cancelExpiredUnpaidReservations();
    }
}
