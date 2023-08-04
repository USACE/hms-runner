# Build the cc-java-sdk jar
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu as cc-builder
ENV TZ=America/New_York
RUN apt update
RUN apt -y install wget unzip
RUN wget https://services.gradle.org/distributions/gradle-7.3.1-bin.zip
RUN mkdir /opt/gradle
RUN unzip -d /opt/gradle gradle-7.3.1-bin.zip
ENV PATH=$PATH:/opt/gradle/gradle-7.3.1/bin
RUN mkdir /cc
WORKDIR /cc
COPY ./cc-java-sdk /cc
RUN gradle build 

# Build the hms-runner
FROM openjdk:17-jdk-slim-bullseye as hms-builder
ENV TZ=America/New_York
RUN apt update
RUN apt -y install wget unzip
RUN wget https://services.gradle.org/distributions/gradle-7.3.1-bin.zip
RUN mkdir /opt/gradle
RUN unzip -d /opt/gradle gradle-7.3.1-bin.zip
ENV PATH=$PATH:/opt/gradle/gradle-7.3.1/bin
RUN apt -y install libxrender1 libxtst6 libxi6 libfreetype6 libgfortran5 libfontconfig1

RUN wget https://www.hec.usace.army.mil/nexus/repository/maven-public/mil/army/usace/hec/hec-hms/4.11-linux64/hec-hms-4.11-linux64.tar.gz -P /
RUN tar -xvzf /hec-hms-4.11-linux64.tar.gz -C /

COPY --from=cc-builder /cc/build/libs/cc-0.0.50.jar /HEC-HMS-4.11/lib
COPY . /app/hms-runner 
WORKDIR /app/hms-runner
RUN gradle build --no-daemon


FROM ubuntu:20.04 as prod
RUN apt update
RUN apt -y install libxrender1 libxtst6 libxi6 libfreetype6 libgfortran5 libfontconfig1

COPY --from=hms-builder /HEC-HMS-4.11 /HEC-HMS-4.11
COPY --from=hms-builder /app/hms-runner/build/libs/hms-runner-0.0.1.jar /HEC-HMS-4.11/lib

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

WORKDIR /HEC-HMS-4.11/lib
ENTRYPOINT ["java", "-Djava.library.path=/HEC-HMS-4.11/bin/gdal:/HEC-HMS-4.11/bin", "-jar", "hms-runner-0.0.1.jar"]