package bpel.tp.residence;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.jws.WebService;

/**
 * Residence partner service.
 * Rooms are given one after the other : buildings A, B and C, 20 rooms per
 * floor. It is only a counter, there is no real stock of rooms.
 */
@WebService(
        serviceName = "ResidenceService",
        portName = "ResidencePort",
        endpointInterface = "bpel.tp.residence.ResidencePT",
        targetNamespace = "http://tp.bpel/residence/",
        wsdlLocation = "WEB-INF/wsdl/residence.wsdl")
public class ResidenceServiceImpl implements ResidencePT {

    private static final Logger LOG = Logger.getLogger(ResidenceServiceImpl.class.getName());
    private static final String[] BUILDINGS = {"A", "B", "C"};
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Override
    public AssignResponse assignRoom(AssignRequest req) {
        int n = COUNTER.getAndIncrement();
        String building = BUILDINGS[n % BUILDINGS.length];
        int floor = 1 + (n / BUILDINGS.length) / 20;
        int room = 1 + (n / BUILDINGS.length) % 20;
        String roomNumber = building + "-" + floor + String.format("%02d", room);

        LOG.info("[RESIDENCE] assignRoom studentId=" + req.getStudentId() + " -> room=" + roomNumber);

        AssignResponse r = new AssignResponse();
        r.setStudentId(req.getStudentId());
        r.setRoomNumber(roomNumber);
        r.setBuilding("Building " + building);
        return r;
    }
}
