package tp.bpel.choreography.transport;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import bpel.tp.transport.ActivateRequest;
import bpel.tp.transport.ActivateResponse;
import bpel.tp.transport.TransportServiceImpl;
import tp.bpel.choreography.common.Event;
import tp.bpel.choreography.common.EventPublisher;

@Component
class TransportListener {

    private static final Logger LOG = LoggerFactory.getLogger(TransportListener.class);
    private static final String SOURCE = "transport-service";

    @Autowired private EventPublisher publisher;
    @Autowired private TransportServiceImpl transport;

    @JmsListener(destination = "PaymentDecided", selector = "approved = TRUE")
    public void onPaymentApproved(String json) throws Exception {
        Event e = Event.parse(json);
        LOG.info("<-- received {}", e.summary());

        ActivateRequest req = new ActivateRequest();
        req.setStudentId(e.data.get("studentId").asText());
        ActivateResponse r = transport.activatePass(req);

        publisher.publish(Event.of("PassActivated", e.correlationId, SOURCE,
                Map.of("studentId", r.getStudentId(),
                       "passId", r.getPassId(),
                       "validUntil", r.getValidUntil())));
    }
}
