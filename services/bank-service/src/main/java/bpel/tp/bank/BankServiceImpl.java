package bpel.tp.bank;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.logging.Logger;

import javax.jws.WebService;

/**
 * Bank partner service.
 * Demo rule given in the lab sheet: a payment above 500 000 FCFA is refused.
 */
@WebService(
        serviceName = "BankService",
        portName = "BankPort",
        endpointInterface = "bpel.tp.bank.BankPT",
        targetNamespace = "http://tp.bpel/bank/",
        wsdlLocation = "WEB-INF/wsdl/bank.wsdl")
public class BankServiceImpl implements BankPT {

    private static final Logger LOG = Logger.getLogger(BankServiceImpl.class.getName());
    private static final BigDecimal LIMIT = new BigDecimal("500000");

    @Override
    public PayResponse pay(PayRequest req) {
        PayResponse r = new PayResponse();

        boolean ok = req.getAmount() != null && req.getAmount().compareTo(LIMIT) <= 0;
        r.setApproved(ok);
        r.setReceipt(ok ? "RCT-" + UUID.randomUUID() : "N/A");

        LOG.info("[BANK] pay studentId=" + req.getStudentId() + " amount=" + req.getAmount()
                + " -> approved=" + ok + " receipt=" + r.getReceipt());
        return r;
    }
}
