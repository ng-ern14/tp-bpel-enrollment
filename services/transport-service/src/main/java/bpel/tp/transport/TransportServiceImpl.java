package bpel.tp.transport;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.jws.WebService;

/**
 * Transport partner service.
 * Activates a transit pass valid until the end of the academic year.
 */
@WebService(
        serviceName = "TransportService",
        portName = "TransportPort",
        endpointInterface = "bpel.tp.transport.TransportPT",
        targetNamespace = "http://tp.bpel/transport/",
        wsdlLocation = "WEB-INF/wsdl/transport.wsdl")
public class TransportServiceImpl implements TransportPT {

    private static final Logger LOG = Logger.getLogger(TransportServiceImpl.class.getName());
    private static final String END_OF_YEAR = "2027-07-31";
    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    @Override
    public ActivateResponse activatePass(ActivateRequest req) {
        String passId = String.format("PASS-%06d", COUNTER.getAndIncrement());

        LOG.info("[TRANSPORT] activatePass studentId=" + req.getStudentId() + " -> passId=" + passId);

        ActivateResponse r = new ActivateResponse();
        r.setStudentId(req.getStudentId());
        r.setPassId(passId);
        r.setValidUntil(END_OF_YEAR);
        return r;
    }
}
