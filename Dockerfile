FROM centos:centos6

#ENV http_proxy http://www-cache.reith.bbc.co.uk:80
#ENV https_proxy https://www-cache.reith.bbc.co.uk:80
RUN yum update -y \
    && yum install -y java-1.7.0-openjdk-devel \
    && yum clean all

COPY  /target/universal/stage/ /opt/guide-assets
EXPOSE 9000
RUN unset http_proxy && unset https_proxy
ENV JAVA_OPTS "-Djavax.net.ssl.trustStore=/etc/pki/cosmos/current/client.jks -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.ssl.keyStore=/etc/pki/tls/private/client.p12 -Djavax.net.ssl.keyStorePassword=client -Djavax.net.ssl.keyStoreType=PKCS12"
WORKDIR /opt/guide-assets/
CMD bin/guide-assets
