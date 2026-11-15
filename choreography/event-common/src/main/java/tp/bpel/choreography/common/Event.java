package tp.bpel.choreography.common;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Common envelope shared by all the events of the choreography.
 *
 * <pre>
 * { "eventId": "uuid", "correlationId": "enr-001", "timestamp": "2026-11-10T14:35:21Z",
 *   "source": "bank-service", "type": "PaymentDecided", "data": { ... } }
 * </pre>
 *
 * The correlationId is the same for every event of one enrollment, this is
 * how each participant knows which enrollment an event belongs to.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String eventId;
    public String correlationId;
    public String timestamp;
    public String source;
    public String type;
    public JsonNode data;

    /** Reads an event received from the broker. */
    public static Event parse(String json) throws JsonProcessingException {
        Event e = MAPPER.readValue(json, Event.class);
        if (e.data == null) {
            e.data = MAPPER.createObjectNode();
        }
        return e;
    }

    /** Builds a new event, eventId and timestamp are filled automatically. */
    public static Event of(String type, String correlationId, String source, Map<String, ?> data) {
        Event e = new Event();
        e.eventId = UUID.randomUUID().toString();
        e.correlationId = correlationId;
        e.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        e.source = source;
        e.type = type;
        e.data = MAPPER.valueToTree(data);
        return e;
    }

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("cannot serialize event " + type, ex);
        }
    }

    /** Short text used in the logs to follow the cascade of events. */
    public String summary() {
        return type + " corr=" + correlationId + " data=" + data;
    }
}
