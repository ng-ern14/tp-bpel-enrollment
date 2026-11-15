#!/usr/bin/env bash
# Plays the client side of the choreography : waits for the
# EnrollmentCompleted events and prints them (artemis CLI consumer).
#
# usage : BROKER_HOME=~/artemis-broker ./client-listen.sh

BROKER_HOME="${BROKER_HOME:-$HOME/artemis-broker}"
ARTEMIS="$BROKER_HOME/bin/artemis"
[ -f "$ARTEMIS.cmd" ] && ARTEMIS="$ARTEMIS.cmd"
unset ARTEMIS_INSTANCE

"$ARTEMIS" consumer --destination topic://EnrollmentCompleted \
    --message-count 100 --verbose --user admin --password admin
