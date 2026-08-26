package com.healthcare.appointment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.boot.test.mock.mockito.MockBean(com.healthcare.appointment.service.MedicationReminderTask.class)
class AppointmentApplicationTests {

	@Test
	void contextLoads() {
	}

}
