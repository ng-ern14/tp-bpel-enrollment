package tp.bpel.choreography.university;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import bpel.tp.university.CancelRequest;
import bpel.tp.university.RegisterRequest;
import bpel.tp.university.RegisterResponse;
import bpel.tp.university.Student;
import bpel.tp.university.UniversityServiceImpl;
import tp.bpel.choreography.common.Event;
import tp.bpel.choreography.common.EventPublisher;

@Component
class UniversityListener {

    private static final Logger LOG = LoggerFactory.getLogger(UniversityListener.class);
    private static final String SOURCE = "university-service";

    @Autowired private EventPublisher publisher;
    @Autowired private UniversityServiceImpl university;
    @Autowired private CompletionTracker tracker;

    /** step 1 : the client asks for an enrollment */
    @JmsListener(destination = "EnrollmentRequested")
    public void onEnrollmentRequested(String json) throws Exception {
        Event e = Event.parse(json);
        LOG.info("<-- received {}", e.summary());

        JsonNode st = e.data.path("student");
        Student s = new Student();
        s.setName(st.path("name").asText());
        s.setProgram(st.path("program").asText());
        RegisterRequest req = new RegisterRequest();
        req.setStudent(s);

        RegisterResponse r = university.register(req);
        tracker.start(e.correlationId, r.getStudentId());

        // the amount is forwarded because the bank needs it
        publisher.publish(Event.of("StudentRegistered", e.correlationId, SOURCE,
                Map.of("studentId", r.getStudentId(),
                       "name", s.getName(),
                       "program", s.getProgram(),
                       "amount", e.data.path("amount").asText())));
    }

    /** refused payment : compensation = cancel the registration */
    @JmsListener(destination = "PaymentDecided", selector = "approved = FALSE")
    public void onPaymentRefused(String json) throws Exception {
        Event e = Event.parse(json);
        LOG.info("<-- received {}", e.summary());

        String studentId = e.data.get("studentId").asText();
        CancelRequest cancel = new CancelRequest();
        cancel.setStudentId(studentId);
        cancel.setReason("PAYMENT_REFUSED");
        university.cancel(cancel);
        tracker.forget(e.correlationId);

        publisher.publish(Event.of("EnrollmentCompleted", e.correlationId, SOURCE,
                Map.of("status", "REFUSED",
                       "studentId", studentId,
                       "message", "Payment refused by the bank, the registration has been cancelled")));
    }

    /** approved payment : we only keep the receipt for the final message */
    @JmsListener(destination = "PaymentDecided", selector = "approved = TRUE")
    public void onPaymentApproved(String json) throws Exception {
        Event e = Event.parse(json);
        LOG.info("<-- received {}", e.summary());
        complete(tracker.receipt(e.correlationId, e.data.path("receipt").asText()));
    }

    @JmsListener(destination = "RoomAssigned")
    public void onRoomAssigned(String json) throws Exception {
        Event e = Event.parse(json);
        LOG.info("<-- received {}", e.summary());
        complete(tracker.room(e.correlationId, e.data.path("roomNumber").asText()));
    }

    @JmsListener(destination = "PassActivated")
    public void onPassActivated(String json) throws Exception {
        Event e = Event.parse(json);
        LOG.info("<-- received {}", e.summary());
        complete(tracker.pass(e.correlationId, e.data.path("passId").asText()));
    }

    /** publishes EnrollmentCompleted once room, pass and receipt are all known */
    private void complete(CompletionTracker.Pending p) {
        if (p == null) {
            return;   // still waiting for another event
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "ENROLLED");
        data.put("studentId", p.studentId);
        data.put("roomNumber", p.roomNumber);
        data.put("passId", p.passId);
        data.put("receipt", p.receipt);
        publisher.publish(Event.of("EnrollmentCompleted", p.correlationId, SOURCE, data));
    }
}
