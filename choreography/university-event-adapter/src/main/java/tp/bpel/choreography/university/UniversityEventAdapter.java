package tp.bpel.choreography.university;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

import bpel.tp.university.UniversityServiceImpl;

/**
 * University participant of the choreography.
 *  - EnrollmentRequested            -> register the student, publish StudentRegistered
 *  - PaymentDecided (approved=FALSE) -> cancel the registration (compensation),
 *                                      publish EnrollmentCompleted (REFUSED)
 *  - RoomAssigned + PassActivated   -> publish EnrollmentCompleted (ENROLLED)
 *
 * The university owns the student record, so it is also the participant that
 * closes the enrollment (the "completion aggregator" of the interaction contract).
 */
@SpringBootApplication(scanBasePackages = "tp.bpel.choreography")
@EnableJms
public class UniversityEventAdapter {

    public static void main(String[] a) {
        SpringApplication.run(UniversityEventAdapter.class, a);
    }

    @Bean
    public UniversityServiceImpl universityService() {
        return new UniversityServiceImpl();
    }
}
