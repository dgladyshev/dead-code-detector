FROM ubuntu:15.04

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get clean

#configure libraries
RUN apt-get install libxrender1 -y
RUN apt-get install libxi6 libgconf-2-4 -y
RUN apt-get install libxtst6 -y

ENV LD_LIBRARY_PATH /usr/lib/jvm/java-8-oracle/jre/lib/amd64:/scitools/bin/linux64
ENV PYTHONPATH /scitools/bin/linux64/python
ENV STIHOME /scitools/bin/linux64

RUN wget http://builds.scitools.com/all_builds/b844/Understand/Understand-4.0.844-Linux-64bit.tgz && tar -zxvf Understand-4.0.844-Linux-64bit.tgz
RUN rm Understand-4.0.844-Linux-64bit.tgz
RUN echo "Server: scitools-license.devfactory.com 00000000 9000" > scitools/conf/license/locallicense.dat

ADD . /dead-code-detector

WORKDIR /dead-code-detector

ENV MEMORY 512
gra
EXPOSE 8080

RUN ./gradlew clean build

CMD java -Xmx${MEMORY}m -Djava.security.egd=file:/dev/./urandom -jar build/libs/dead-code-detector.jar