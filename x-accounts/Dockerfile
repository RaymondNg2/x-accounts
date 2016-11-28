FROM java:8
# EXPOSE 8080
ADD /target/HelloWorld-1.0-SNAPSHOT.jar HelloWorld-1.0-SNAPSHOT.jar
#ENTRYPOINT ["java","-jar","HelloWorld-1.0-SNAPSHOT.jar"]
ENTRYPOINT ["java","-cp","HelloWorld-1.0-SNAPSHOT.jar","com.vc.app.helloworld.HelloWorld"]

# build the docker image
# docker build -f Dockerfile -t helloworld:0
#
# run the docker image
# docker run helloworld:0