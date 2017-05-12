FROM java:8
# EXPOSE 8080
ADD /target/x-accounts-0.0.1-SNAPSHOT.jar x-accounts-0.0.1-SNAPSHOT.jar
#ENTRYPOINT ["java","-jar","HelloWorld-1.0-SNAPSHOT.jar"]
ENTRYPOINT ["java","-Xmx2G","-Djava.net.preferIPv4Stack=true","-cp","x-accounts-0.0.1-SNAPSHOT.jar","com.ximedes.server.ServerMain"]

# build the docker image
# docker build -f Dockerfile -t helloworld:0
#
# run the docker image
# docker run helloworld:0