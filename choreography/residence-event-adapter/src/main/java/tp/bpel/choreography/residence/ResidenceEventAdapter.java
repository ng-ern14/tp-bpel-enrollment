package tp.bpel.choreography.residence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

import bpel.tp.residence.ResidenceServiceImpl;

/**
 * Residence participant of the choreography.
 * Listens to PaymentDecided (approved = TRUE only), publishes RoomAssigned.
 */
@SpringBootApplication(scanBasePackages = "tp.bpel.choreography")
@EnableJms
public class ResidenceEventAdapter {

    public static void main(String[] a) {
        SpringApplication.run(ResidenceEventAdapter.class, a);
    }

    @Bean
    public ResidenceServiceImpl residenceService() {
        return new ResidenceServiceImpl();
    }
}
