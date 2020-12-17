FROM ubuntu:16.04

RUN apt-get update && apt-get install -y

RUN apt-get install -y curl bzip2 gettext locales zip unzip

# Set the locale
RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

WORKDIR /opt/jdk
COPY jdk-6u45-linux-x64.bin .
RUN sh jdk-6u45-linux-x64.bin

WORKDIR /opt

COPY jboss-4.0.5.GA.zip .
RUN unzip jboss-4.0.5.GA.zip

RUN curl https://ftpmirror1.infania.net/mirror/apache//ant/binaries/apache-ant-1.9.15-bin.tar.gz \
        --output apache-ant-1.9.15-bin.tar.gz

RUN tar xf apache-ant-1.9.15-bin.tar.gz -C /usr/local
RUN ln -s /usr/local/apache-ant-1.9.15 /usr/local/ant

ENV JAVA_HOME /opt/jdk/jdk1.6.0_45
ENV ANT_HOME=/usr/local/ant
ENV JBOSS_HOME=/opt/jboss-4.0.5.GA

ENV PATH $PATH:$JAVA_HOME/bin:${ANT_HOME}/bin

ARG myuser
RUN echo $myuser
RUN adduser $myuser

RUN chown -R $myuser:$myuser /opt/jboss-4.0.5.GA

WORKDIR /home/$myuser

# Run a bash shell
CMD ["/bin/bash"]
