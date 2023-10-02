FROM ubuntu:20.04 as builder

ENV TZ=America/New_York
ENV GRADLE_HOME=/opt/gradle/latest
ENV PATH=${GRADLE_HOME}/bin:$PATH

#need to get the jdk.
RUN apt update &&\
    apt -y install wget &&\
    apt -y install libxrender1 libxtst6 libxi6 libfreetype6 libgfortran5 libfontconfig1 libgfortran5 &&\
    wget https://www.hec.usace.army.mil/nexus/repository/maven-releases/mil/army/usace/hec/hec-hms/4.12-beta-1-linux64/hec-hms-4.12-beta-1-linux64.tar.gz -P / &&\
    tar -xvzf /hec-hms-4.12-beta-1-linux64.tar.gz -C / &&\
    apt -y install git &&\
    apt -y install unzip &&\
    wget https://services.gradle.org/distributions/gradle-7.3.1-bin.zip -P /tmp &&\
    unzip -d /opt/gradle /tmp/gradle-7.3.1-bin.zip &&\
    ln -s /opt/gradle/gradle-7.3.1 /opt/gradle/latest