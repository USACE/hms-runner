FROM hmstesting/ubuntu:ubuntu20.04_openjdk11 as dev
ENV TZ=America/New_York
#need to get the jdk.
RUN apt update
RUN apt -y install wget

RUN wget https://www.hec.usace.army.mil/nexus/repository/maven-public/mil/army/usace/hec/hec-hms/4.11-beta.1-linux64/hec-hms-4.11-beta.1-linux64.tar.gz -P /
RUN tar -xvzf /HEC-HMS-4.11-beta.1-linux64.tar.gz -C /

RUN apt -y install unzip
RUN unzip /HEC-HMS-4.11-beta.1/samples.zip -d /

RUN apt -y install git
RUN apt -y install libgfortran5