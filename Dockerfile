FROM openjdk:17-jdk-slim-bullseye as builder
ENV TZ=America/New_York
#need to get the jdk.
RUN apt update
RUN apt -y install libxrender1 libxtst6 libxi6 libfreetype6 libgfortran5 libfontconfig1
RUN apt -y install wget

RUN wget https://www.hec.usace.army.mil/nexus/repository/maven-public/mil/army/usace/hec/hec-hms/4.11-linux64/hec-hms-4.11-linux64.tar.gz -P /
RUN tar -xvzf /hec-hms-4.11-linux64.tar.gz -C /
RUN apt -y install git
RUN apt -y install unzip

RUN wget https://services.gradle.org/distributions/gradle-7.3.1-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-7.3.1-bin.zip
RUN ln -s /opt/gradle/gradle-7.3.1 /opt/gradle/latest
ENV GRADLE_HOME=/opt/gradle/latest
ENV PATH=${GRADLE_HOME}/bin:$PATH
ARG USERNAME
ENV USERNAME $USERNAME
ARG TOKEN
ENV TOKEN $TOKEN

RUN mkdir -p /app
WORKDIR /app
#clone repo
RUN git clone -b feature/mca-option https://github.com/Dewberry/hms-runner.git
WORKDIR /app/hms-runner
#build gradle
RUN gradle build --no-daemon

FROM ubuntu:20.04 as prod
RUN apt update
RUN apt -y install libxrender1 libxtst6 libxi6 libfreetype6 libgfortran5 libfontconfig1
#get built jar and put in prod container.
RUN mkdir -p  /hms

COPY --from=builder /app/hms-runner/build/libs /hms
RUN mkdir -p  /HEC-HMS-4.11
COPY --from=builder /HEC-HMS-4.11 /HEC-HMS-4.11
COPY --from=builder /root/.gradle/caches/modules-2/files-2.1/mil.army.usace.hec/cc-java-sdk/0.0.50/a153375802493d588e7b23007626a6c9dd392934/cc-java-sdk-0.0.50.jar /HEC-HMS-4.11/lib
RUN mkdir -p /root/.gradle/caches
COPY --from=builder /root/.gradle/caches /root/.gradle/caches
ENV HMS_HOME=/HEC-HMS-4.11
ENV JAVA_EXE=$HMS_HOME/jre/bin/java
ENV JAVA_HOME=$HMS_HOME/jre
ENV PROG=hms.hms
ENV PATH=$HMS_HOME/bin/taudem:$HMS_HOME/bin/mpi:$HMS_HOME/bin:$HMS_HOME/jre/bin/:$HMS_HOME/jre/lib/:$PATH
ENV GDAL_DATA=$HMS_HOME/bin/gdal/gdal-data
ENV PROJ_LIB=$HMS_HOME/bin/gdal/proj
ENV CLASSPATH=$HMS_HOME/*:$HMS_HOME/lib/*:$HMS_HOME/lib/hec/*:$HMS_HOME/jre/lib/*
ENV JAVA_OPTS="-Djava.library.path=$HMS_HOME/bin/gdal:$HMS_HOME/bin"
RUN chmod +x /HEC-HMS-4.11/jre/bin/java

WORKDIR /hms
#CMD ["sleep","1d"] # for testing
ENTRYPOINT ["java", "-Djava.library.path=/HEC-HMS-4.11/bin/gdal:/HEC-HMS-4.11/bin", "-jar", "hms-runner-0.0.1.jar"]
# #cmd will append