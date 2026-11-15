#!/usr/bin/env bash
# Creates the multicast addresses (topics) used by the choreography.
# The lab sheet lists 4 of them, RoomAssigned and PassActivated are added
# because they appear in the interaction contract (section 6.2).
#
# usage : BROKER_HOME=~/artemis-broker ./create-topics.sh
# (BROKER_HOME is the broker instance created with "artemis create")

BROKER_HOME="${BROKER_HOME:-$HOME/artemis-broker}"
ARTEMIS="$BROKER_HOME/bin/artemis"
# on Windows (Git Bash) the CLI is artemis.cmd
[ -f "$ARTEMIS.cmd" ] && ARTEMIS="$ARTEMIS.cmd"
# let the artemis script find its own instance
unset ARTEMIS_INSTANCE

for topic in EnrollmentRequested StudentRegistered PaymentDecided \
             RoomAssigned PassActivated EnrollmentCompleted; do
    "$ARTEMIS" address create --name "$topic" --multicast \
        --user admin --password admin --silent
done

"$ARTEMIS" address show --user admin --password admin
