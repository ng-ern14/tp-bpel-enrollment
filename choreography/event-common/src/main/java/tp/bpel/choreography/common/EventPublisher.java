package tp.bpel.choreography.common;

import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes an event on the topic that has the same name as the event type
 * (StudentRegistered, PaymentDecided, ...).
 *
 * The type, the correlationId and, when present, the "approved" flag are also
 * copied as JMS properties. This lets a consumer filter the messages with a
 * JMS selector, for example Residence and Transport only take
 * PaymentDecided with "approved = TRUE".
 */
@Component
public class EventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(EventPublisher.class);

    private final JmsTemplate jms;

    public EventPublisher(JmsTemplate jms) {
        this.jms = jms;
    }

    public void publish(Event e) {
        String json = e.toJson();
        jms.send(e.type, session -> {
            TextMessage m = session.createTextMessage(json);
            m.setStringProperty("type", e.type);
            m.setStringProperty("correlationId", e.correlationId);
            if (e.data.has("approved")) {
                m.setBooleanProperty("approved", e.data.get("approved").asBoolean());
            }
            return m;
        });
        LOG.info("--> published {}", e.summary());
    }
}
