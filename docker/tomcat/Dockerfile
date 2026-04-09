FROM azul/zulu-openjdk:7

RUN apt-get update \
    &&  apt-get -y -o Dpkg::Options::="--force-confnew" upgrade \
    &&  apt-get -y install \
    perl \
    zip \
    unzip \
    libterm-readkey-perl \
    openssl \
    gettext

ADD apache-tomcat-8.5.30 /opt/tomcat/apache-tomcat-8.5.30
ADD apache-ant-1.9.0 /opt/apache-ant-1.9.0
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Pre-download Ivy so the build can run offline (ant -Doffline=1)
RUN mkdir -p /root/.ant/lib \
    && curl -fsSL https://repo1.maven.org/maven2/org/apache/ivy/ivy/2.5.0/ivy-2.5.0.jar \
       -o /root/.ant/lib/ivy.jar

ENV CATALINA_HOME="/opt/tomcat/apache-tomcat-8.5.30"
ENV PATH="${JAVA_HOME}/bin:/opt/apache-ant-1.9.0/bin:${PATH}"

WORKDIR /opt/biobank
