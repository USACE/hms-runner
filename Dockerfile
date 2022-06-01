FROM mcr.microsoft.com/openjdk/jdk:11-ubuntu as dev
ENV TZ=America/New_York
#need to get the jdk.
RUN apt update
RUN apt -y install wget

RUN wget https://github.com/HydrologicEngineeringCenter/hec-downloads/releases/download/1.0.22/HEC-HMS-4.9-linux64.tar.gz -P /
RUN tar -xvzf /HEC-HMS-4.9-linux64.tar.gz -C /

RUN apt -y install unzip
RUN unzip /HEC-HMS-4.9/samples.zip -d /

RUN apt -y install git

#FROM ubuntu:20.04 as prod
#RUN mkdir -p  /hms 
#COPY --from=dev /HEC-HMS-4.9 /hms
#COPY --from=dev /workspaces/hms-runner/hmsrunner.py /hms
#RUN chmod +x /hms/*