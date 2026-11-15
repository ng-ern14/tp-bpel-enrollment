package tp.bpel.choreography.residence;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import bpel.tp.residence.AssignRequest;
import bpel.tp.residence.AssignResponse;
import bpel.tp.residence.ResidenceServiceImpl;
import tp.bpel.choreography.common.Event;
import tp.bpel.choreography.common.EventPublisher;

@Component
class ResidenceListener {

    private static final Logger LOG = LoggerFactory.getLogger(ResidenceListener.class);
    private static final String SOURCE = "residence-service";

    @Autowired private EventPublisher publisher;
    @Autowired private ResidenceServiceImpl residence;

    /** the JMS selector keeps only the approved payments */
    @JmsListener(destination = "PaymentDecided", selector = "approved = TRUE")
    public void onPaymentApproved(String json) throws Exception {
        Event e = Event.parse(json);
        LOG.info("<-- received {}", e.summary());

        AssignRequest req = new AssignRequest();
        req.setStudentId(e.data.get("studentId").asText());
        AssignResponse r = residence.assignRoom(req);

        publisher.publish(Event.of("RoomAssigned", e.correlationId, SOURCE,
                Map.of("studentId", r.getStudentId(),
                       "roomNumber", r.getRoomNumber(),
                       "building", r.getBuilding())));
    }
}
