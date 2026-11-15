package tp.bpel.choreography.university;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Keeps, for each correlationId, what we already know about an enrollment.
 * RoomAssigned, PassActivated and PaymentDecided arrive on different listener
 * threads and in any order, so every update is synchronized on the entry.
 * When the last missing piece arrives the entry is removed and returned,
 * this way EnrollmentCompleted is published only once.
 */
@Component
class CompletionTracker {

    static final class Pending {
        final String correlationId;
        String studentId;
        String receipt;
        String roomNumber;
        String passId;

        Pending(String correlationId) {
            this.correlationId = correlationId;
        }

        boolean isComplete() {
            return receipt != null && roomNumber != null && passId != null;
        }
    }

    private final Map<String, Pending> pending = new ConcurrentHashMap<>();

    void start(String correlationId, String studentId) {
        entry(correlationId).studentId = studentId;
    }

    void forget(String correlationId) {
        pending.remove(correlationId);
    }

    Pending receipt(String correlationId, String receipt) {
        return update(correlationId, p -> p.receipt = receipt);
    }

    Pending room(String correlationId, String roomNumber) {
        return update(correlationId, p -> p.roomNumber = roomNumber);
    }

    Pending pass(String correlationId, String passId) {
        return update(correlationId, p -> p.passId = passId);
    }

    private Pending entry(String correlationId) {
        return pending.computeIfAbsent(correlationId, Pending::new);
    }

    private Pending update(String correlationId, java.util.function.Consumer<Pending> change) {
        Pending p = entry(correlationId);
        synchronized (p) {
            change.accept(p);
            if (p.isComplete() && pending.remove(correlationId, p)) {
                return p;
            }
        }
        return null;
    }
}
