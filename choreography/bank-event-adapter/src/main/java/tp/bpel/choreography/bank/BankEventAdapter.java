package tp.bpel.choreography.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

import bpel.tp.bank.BankServiceImpl;

/**
 * Bank participant of the choreography.
 * Listens to StudentRegistered, publishes PaymentDecided.
 */
@SpringBootApplication(scanBasePackages = "tp.bpel.choreography")
@EnableJms
public class BankEventAdapter {

    public static void main(String[] a) {
        SpringApplication.run(BankEventAdapter.class, a);
    }

    /** same business logic as the SOAP service of Part A */
    @Bean
    public BankServiceImpl bankService() {
        return new BankServiceImpl();
    }
}
