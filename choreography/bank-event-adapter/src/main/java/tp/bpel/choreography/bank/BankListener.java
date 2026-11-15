package tp.bpel.choreography.bank;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import bpel.tp.bank.BankServiceImpl;
import bpel.tp.bank.PayRequest;
import bpel.tp.bank.PayResponse;
import tp.bpel.choreography.common.Event;
import tp.bpel.choreography.common.EventPublisher;

@Component
class BankListener {

    private static final Logger LOG = LoggerFactory.getLogger(BankListener.class);
    private static final String SOURCE = "bank-service";

    @Autowired private EventPublisher publisher;
    @Autowired private BankServiceImpl bank;

    @JmsListener(destination = "StudentRegistered")
    public void onStudentRegistered(String json) throws Exception {
        Event e = Event.parse(json);
        LOG.info("<-- received {}", e.summary());

        String studentId = e.data.get("studentId").asText();
        PayRequest req = new PayRequest();
        req.setStudentId(studentId);
        req.setAmount(new BigDecimal(e.data.get("amount").asText()));

        PayResponse r = bank.pay(req);

        Event out = Event.of("PaymentDecided", e.correlationId, SOURCE,
                Map.of("studentId", studentId,
                       "approved", r.isApproved(),
                       "receipt", r.getReceipt()));
        publisher.publish(out);
    }
}
