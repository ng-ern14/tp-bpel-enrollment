package bpel.tp.transport;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;

import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

/**
 * CXF servlet that publishes the Transport endpoint under /TransportService
 * (full address : http://localhost:8080/transport-service/TransportService).
 * We use the non-Spring servlet of CXF to keep the WAR small.
 */
public class TransportServlet extends CXFNonSpringServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void loadBus(ServletConfig sc) {
        super.loadBus(sc);
        BusFactory.setDefaultBus(getBus());

        EndpointImpl endpoint = new EndpointImpl(getBus(), new TransportServiceImpl());
        // Without Spring, CXF resolves the relative wsdlLocation of the
        // annotation ("WEB-INF/wsdl/...") from the folder where Tomcat was
        // started (bin/), so we give it the real URL inside the web app.
        endpoint.setWsdlLocation(wsdlUrl(sc, "/WEB-INF/wsdl/transport.wsdl"));
        endpoint.publish("/TransportService");
    }

    private static String wsdlUrl(ServletConfig sc, String path) {
        try {
            URL url = sc.getServletContext().getResource(path);
            if (url == null) {
                throw new IllegalStateException(path + " not found in the WAR");
            }
            return url.toString();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
