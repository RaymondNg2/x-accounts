#!/bin/sh
#
# Build and deploy the app to all machines.
#

( cd x-accounts; mvn3 package || exit 1 )

scp x-accounts/target/x-accounts-0.0.1-SNAPSHOT.jar run.db0.sh db0:
