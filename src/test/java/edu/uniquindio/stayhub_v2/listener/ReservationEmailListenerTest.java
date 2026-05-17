package edu.uniquindio.stayhub_v2.listener;

import edu.uniquindio.stayhub_v2.event.ReservationCompletedEvent;
import edu.uniquindio.stayhub_v2.model.Accommodation;
import edu.uniquindio.stayhub_v2.model.Reservation;
import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationEmailListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReservationEmailListener reservationEmailListener;

    private Reservation reservation;
    private User guest;
    private User host;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                reservationEmailListener,
                "frontendUrl",
                "https://stay-hub-xi.vercel.app"
        );

        guest = User.builder()
                .id(1L)
                .email("guest@mail.com")
                .fullName("Juan Perez")
                .build();

        host = User.builder()
                .id(2L)
                .email("host@mail.com")
                .fullName("Laura Host")
                .build();

        Accommodation accommodation = Accommodation.builder()
                .id(10L)
                .title("Cabana en Quindio")
                .host(host)
                .build();

        reservation = new Reservation();
        reservation.setId(100L);
        reservation.setGuest(guest);
        reservation.setAccommodation(accommodation);
        reservation.setStartDate(LocalDateTime.of(2026, 5, 10, 15, 0));
        reservation.setEndDate(LocalDateTime.of(2026, 5, 13, 11, 0));
    }

    @Test
    void handleReservationCompleted_SendsGuestAndHostEmails() {
        reservationEmailListener.handleReservationCompleted(new ReservationCompletedEvent(reservation));

        verify(emailService).sendEmailWithTemplate(
                eq(guest.getEmail()),
                contains("ha finalizado"),
                eq("reservation-completed-guest"),
                any(Context.class)
        );
        verify(emailService).sendEmailWithTemplate(
                eq(host.getEmail()),
                contains("ha finalizado"),
                eq("reservation-completed-host"),
                any(Context.class)
        );
    }

    @Test
    void handleReservationCompleted_GuestEmailContextContainsReviewUrl() {
        reservationEmailListener.handleReservationCompleted(new ReservationCompletedEvent(reservation));

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        verify(emailService, times(1)).sendEmailWithTemplate(
                eq(guest.getEmail()),
                contains("ha finalizado"),
                eq("reservation-completed-guest"),
                contextCaptor.capture()
        );

        Context guestContext = contextCaptor.getValue();

        assertThat(guestContext.getVariable("reviewUrl"))
                .isEqualTo("https://stay-hub-xi.vercel.app/dashboard/guest/reviews?accommodationId=10&reservationId=100");
        assertThat(guestContext.getVariable("reservationId")).isEqualTo(100L);
        assertThat(guestContext.getVariable("accommodationTitle")).isEqualTo("Cabana en Quindio");
    }
}
