#!/bin/sh

java8 -Xmx2G -Djava.net.preferIPv4Stack=true -cp /home/ec2-user/x-accounts-0.0.1-SNAPSHOT.jar com.ximedes.server.ServerMain
