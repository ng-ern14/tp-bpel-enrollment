#!/usr/bin/env bash
# Part A : copies the 4 partner WARs and the BPEL process into Tomcat.
# Run "mvn clean install" at the root of the project before.
#
# usage : TOMCAT_HOME=~/opt/tomcat9 ./scripts/deploy-part-a.sh

set -e
cd "$(dirname "$0")/.."

if [ -z "$TOMCAT_HOME" ]; then
    echo "TOMCAT_HOME is not set" >&2
    exit 1
fi

for s in university bank residence transport; do
    cp "services/$s-service/target/$s-service.war" "$TOMCAT_HOME/webapps/"
    echo "deployed $s-service.war"
done

# ODE hot-deploys every sub-folder of WEB-INF/processes
PROCESSES="$TOMCAT_HOME/webapps/ode/WEB-INF/processes"
if [ ! -d "$PROCESSES" ]; then
    echo "ODE is not deployed yet (start Tomcat once with ode.war in webapps)" >&2
    exit 1
fi
if [ -e "$PROCESSES/enrollment.deployed" ]; then
    # remove the old version and let ODE undeploy it before copying the new one
    rm -rf "$PROCESSES/enrollment"
    echo "waiting for ODE to undeploy the old process..."
    while [ -e "$PROCESSES/enrollment.deployed" ]; do sleep 2; done
fi
cp -r bpel-orchestrator/target/enrollment "$PROCESSES/enrollment"
echo "copied the BPEL process to $PROCESSES/enrollment, waiting for ODE..."
while [ ! -e "$PROCESSES/enrollment.deployed" ]; do sleep 2; done
echo "process deployed : http://localhost:8080/ode/processes/EnrollmentService?wsdl"
