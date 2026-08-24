package com.healthcare.appointment.repository;

import com.healthcare.appointment.model.Appointment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    public void testPessimisticLockQuery() {
        // Just verify the context loads and query is syntactically valid
        Optional<Appointment> result = appointmentRepository
            .findByDoctorAndDatetimeWithLock(1L, LocalDateTime.now());
        
        assertThat(result).isEmpty();
    }
}
