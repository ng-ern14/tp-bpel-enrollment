# TP BPEL - Student enrollment (orchestration and choreography)

Practical lab No. 4 - Information Systems Architecture - ISI 4.

The same enrollment workflow (University, Bank, Residence, Transport) is
implemented two times :

- **Part A - orchestration** : a WS-BPEL 2.0 process (`EnrollmentProcess`)
  deployed on Apache ODE calls the 4 SOAP partner services.
- **Part B - choreography** : the 4 services exchange JSON events through
  Apache ActiveMQ Artemis topics, there is no central process.

## Project layout

```
tp-bpel-enrollment/
├── pom.xml                       parent pom (packaging pom)
├── services/                     Part A partner services (JAX-WS / CXF, WAR)
│   ├── university-service/       register, cancel (compensation), getStudent
│   ├── bank-service/             pay  (refused above 500 000 FCFA)
│   ├── residence-service/        assignRoom
│   └── transport-service/        activatePass
├── bpel-orchestrator/            Part A WS-BPEL process
│   ├── EnrollmentProcess.bpel
│   ├── EnrollmentProcess.wsdl
│   ├── types/enrollment.xsd
│   ├── deploy.xml                ODE deployment descriptor
│   ├── pom.xml                   builds target/enrollment (folder for ODE)
│   └── assembly.xml              also zips it as target/enrollment.zip
├── choreography/                 Part B
│   ├── event-common/             JSON event envelope + publisher
│   ├── university-event-adapter/ also closes the enrollment (aggregator)
│   ├── bank-event-adapter/
│   ├── residence-event-adapter/
│   └── transport-event-adapter/
├── soapui/enrollment-tests.xml   SoapUI project (5 test cases)
└── report/                       final PDF report
```

## Versions used

| Tool | Version |
|------|---------|
| JDK | Eclipse Temurin 11.0.33 |
| Maven | 3.9.16 |
| Tomcat | 9.0.98 |
| Apache ODE | 1.3.8 (ode-axis2-war) |
| Apache CXF | 3.5.5 |
| ActiveMQ Artemis | 2.33.0 |
| Spring Boot (adapters) | 2.7.18 |
| SoapUI | 5.7.2 |

`JAVA_HOME` must point to the JDK 11 for every command below (Tomcat, Maven,
Artemis and the adapters). The commands are written for Git Bash on Windows,
they are the same on Linux with `catalina.sh` instead of `catalina.bat`.

## 1. Build everything

```bash
cd tp-bpel-enrollment
mvn clean install
```

This produces the 4 WARs (`services/*/target/*-service.war`), the ODE folder
`bpel-orchestrator/target/enrollment` (BPEL + WSDL + XSD + deploy.xml + the 4
partner WSDLs) and the 4 Spring Boot adapters.

## 2. Part A - orchestration

Start Tomcat once with `ode.war` in `$TOMCAT_HOME/webapps` so that ODE is
unpacked, then deploy the services and the process :

```bash
$TOMCAT_HOME/bin/catalina.bat run          # in its own terminal

# the 4 partner services
cp services/*/target/*-service.war $TOMCAT_HOME/webapps/

# the BPEL process (ODE hot-deploys every folder of WEB-INF/processes)
cp -r bpel-orchestrator/target/enrollment $TOMCAT_HOME/webapps/ode/WEB-INF/processes/
```

ODE writes `enrollment.deployed` next to the folder when the process is
deployed. To redeploy a new version, delete the `enrollment` folder first,
wait until `enrollment.deployed` disappears, then copy it again.

Checks :

- http://localhost:8080/ode/ : ODE console, process `EnrollmentProcess` ACTIVE
- http://localhost:8080/bank-service/BankService?wsdl (same for the 3 others)
- http://localhost:8080/ode/processes/EnrollmentService?wsdl

Run the SoapUI tests (GUI : *File > Import Project*, or command line) :

```bash
$SOAPUI_HOME/bin/testrunner.bat -r soapui/enrollment-tests.xml
```

Quick test without SoapUI :

```bash
curl -H "Content-Type: text/xml" -H 'SOAPAction: "enroll"' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:enr="http://tp.bpel/enrollment/"><soapenv:Body><enr:enrollRequest><student><name>Alice</name><program>ISI4</program></student><amount>350000</amount></enr:enrollRequest></soapenv:Body></soapenv:Envelope>' \
  http://localhost:8080/ode/processes/EnrollmentService
```

350000 gives `ENROLLED` with roomNumber, passId and receipt. 900000 gives
`REFUSED`, and `getStudent` on the University service then returns
`CANCELLED` for that student (compensation).

## 3. Part B - choreography

```bash
# broker (once)
artemis create ~/artemis-broker --user admin --password admin --allow-anonymous
~/artemis-broker/bin/artemis run                       # in its own terminal

# topics (the lab sheet lists 4, RoomAssigned and PassActivated are used
# by the interaction contract)
for t in EnrollmentRequested StudentRegistered PaymentDecided RoomAssigned PassActivated EnrollmentCompleted; do
  ~/artemis-broker/bin/artemis address create --name $t --multicast --user admin --password admin --silent
done

# the 4 adapters, one terminal each
cd choreography/university-event-adapter && mvn spring-boot:run
cd choreography/bank-event-adapter       && mvn spring-boot:run
cd choreography/residence-event-adapter  && mvn spring-boot:run
cd choreography/transport-event-adapter  && mvn spring-boot:run

# the client : one terminal listens to EnrollmentCompleted
~/artemis-broker/bin/artemis consumer --destination topic://EnrollmentCompleted \
  --message-count 100 --verbose --user admin --password admin
```

The request is published from another terminal with one curl command
(change `enr-001`, the name and the amount for the next one, for example
`enr-002`, `Bob`, `900000` for a refused payment) :

```bash
curl -u admin:admin -H "Content-Type: application/json" -H "Origin: http://localhost:8161" \
  -XPOST http://localhost:8161/console/jolokia/ -d '{"type":"exec",
  "mbean":"org.apache.activemq.artemis:broker=\"0.0.0.0\",component=addresses,address=\"EnrollmentRequested\"",
  "operation":"sendMessage(java.util.Map,int,java.lang.String,boolean,java.lang.String,java.lang.String)",
  "arguments":[{},3,"{\"correlationId\":\"enr-001\",\"type\":\"EnrollmentRequested\",\"data\":{\"student\":{\"name\":\"Alice\",\"program\":\"ISI4\"},\"amount\":\"350000\"}}",false,"admin","admin"]}'
```

The Artemis console is at http://localhost:8161/console (admin / admin).

Notes :

- The REST URL of the lab sheet (`/api/rest/queue/...`) answers 404 on
  Artemis 2.33, the REST interface is not part of the broker anymore.
  The curl above uses the Jolokia API of the console instead
  (`sendMessage` operation of the address, 3 = text message), it is still
  one curl command.
- On the test machine port 5672 was already used by RabbitMQ, so the `amqp`
  acceptor (5672) is commented in `etc/broker.xml` of the broker instance.
  AMQP is still accepted on 61616.
- Each adapter prints the events it receives (`<--`) and publishes (`-->`)
  with their correlationId, which gives the trace of a flow.
