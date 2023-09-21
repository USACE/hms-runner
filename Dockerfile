FROM openjdk:17-jdk-slim-bullseye as dev
ENV TZ=America/New_York
#need to get the jdk.
RUN apt update
RUN apt -y install libxrender1 libxtst6 libxi6 libfreetype6 libgfortran5 libfontconfig1
RUN apt -y install wget

RUN wget https://www.hec.usace.army.mil/nexus/repository/maven-public/mil/army/usace/hec/hec-hms/4.12-alpha-3098-linux64/hec-hms-4.12-alpha-3098-linux64.tar.gz -P /
RUN tar -xvzf /hec-hms-4.12-alpha-3098-linux64.tar.gz -C /

RUN apt -y install git