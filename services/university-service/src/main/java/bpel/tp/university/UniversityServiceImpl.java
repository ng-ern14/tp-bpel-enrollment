package bpel.tp.university;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.jws.WebService;

/**
 * University partner service.
 * Students are kept in memory (enough for the lab, the data is lost when
 * Tomcat restarts). A registration can be cancelled : this is the action used
 * by the BPEL compensation handler when the bank refuses the payment.
 */
@WebService(
        serviceName = "UniversityService",
        portName = "UniversityPort",
        endpointInterface = "bpel.tp.university.UniversityPT",
        targetNamespace = "http://tp.bpel/university/",
        wsdlLocation = "WEB-INF/wsdl/university.wsdl")
public class UniversityServiceImpl implements UniversityPT {

    public static final String REGISTERED = "REGISTERED";
    public static final String CANCELLED = "CANCELLED";
    public static final String UNKNOWN = "UNKNOWN";

    private static final Logger LOG = Logger.getLogger(UniversityServiceImpl.class.getName());

    /** studentId -> record. Static so that every instance sees the same data. */
    private static final Map<String, StudentRecord> STUDENTS = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(10345);

    @Override
    public RegisterResponse register(RegisterRequest req) {
        Student s = req.getStudent();
        String id = "STU-" + NEXT_ID.getAndIncrement();
        STUDENTS.put(id, new StudentRecord(s.getName(), s.getProgram()));

        LOG.info("[UNIVERSITY] register name=" + s.getName() + " program=" + s.getProgram()
                + " -> studentId=" + id);

        RegisterResponse r = new RegisterResponse();
        r.setStudentId(id);
        r.setStatus(REGISTERED);
        return r;
    }

    @Override
    public void cancel(CancelRequest req) {
        StudentRecord rec = STUDENTS.get(req.getStudentId());
        if (rec == null) {
            LOG.warning("[UNIVERSITY] cancel : unknown studentId " + req.getStudentId());
            return;
        }
        rec.status = CANCELLED;
        LOG.info("[UNIVERSITY] cancel studentId=" + req.getStudentId()
                + " reason=" + req.getReason() + " -> status=" + CANCELLED);
    }

    @Override
    public GetStudentResponse getStudent(GetStudentRequest req) {
        GetStudentResponse r = new GetStudentResponse();
        r.setStudentId(req.getStudentId());
        StudentRecord rec = STUDENTS.get(req.getStudentId());
        if (rec == null) {
            r.setStatus(UNKNOWN);
        } else {
            Student s = new Student();
            s.setName(rec.name);
            s.setProgram(rec.program);
            r.setStudent(s);
            r.setStatus(rec.status);
        }
        return r;
    }

    /** Simple in-memory record of a student. */
    private static final class StudentRecord {
        final String name;
        final String program;
        volatile String status = REGISTERED;

        StudentRecord(String name, String program) {
            this.name = name;
            this.program = program;
        }
    }
}
