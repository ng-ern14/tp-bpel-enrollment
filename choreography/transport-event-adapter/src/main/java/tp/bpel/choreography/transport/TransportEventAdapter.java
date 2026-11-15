package tp.bpel.choreography.transport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

import bpel.tp.transport.TransportServiceImpl;

/**
 * Transport participant of the choreography.
 * Listens to PaymentDecided (approved = TRUE only), publishes PassActivated.
 */
@SpringBootApplication(scanBasePackages = "tp.bpel.choreography")
@EnableJms
public class TransportEventAdapter {

    public static void main(String[] a) {
        SpringApplication.run(TransportEventAdapter.class, a);
    }

    @Bean
    public TransportServiceImpl transportService() {
        return new TransportServiceImpl();
    }
}
