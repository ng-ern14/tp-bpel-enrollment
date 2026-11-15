#!/usr/bin/env bash
# Publishes one EnrollmentRequested event (the "client" of the choreography).
#
# usage : ./send-enrollment.sh <correlationId> <name> <amount>
# example : ./send-enrollment.sh enr-001 Alice 350000
#
# The lab sheet uses http://localhost:8161/api/rest/queue/... but the REST
# interface does not exist anymore in Artemis 2.33 (HTTP 404). The broker
# still has the Jolokia management API on the same port, and the operation
# sendMessage of an address publishes a message on it, so it is still a
# single curl command. Type 3 = TextMessage, the body is the JSON event.

CORR="${1:-enr-001}"
NAME="${2:-Alice}"
AMOUNT="${3:-350000}"

EVENT="{\\\"correlationId\\\":\\\"$CORR\\\",\\\"type\\\":\\\"EnrollmentRequested\\\",\\\"source\\\":\\\"client\\\",\\\"data\\\":{\\\"student\\\":{\\\"name\\\":\\\"$NAME\\\",\\\"program\\\":\\\"ISI4\\\"},\\\"amount\\\":\\\"$AMOUNT\\\"}}"

curl -s -u admin:admin -H "Content-Type: application/json" -H "Origin: http://localhost:8161" \
     -XPOST http://localhost:8161/console/jolokia/ \
     -d "{\"type\":\"exec\",
          \"mbean\":\"org.apache.activemq.artemis:broker=\\\"0.0.0.0\\\",component=addresses,address=\\\"EnrollmentRequested\\\"\",
          \"operation\":\"sendMessage(java.util.Map,int,java.lang.String,boolean,java.lang.String,java.lang.String)\",
          \"arguments\":[{},3,\"$EVENT\",false,\"admin\",\"admin\"]}"
echo
