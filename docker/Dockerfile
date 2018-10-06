# vim:set ft=dockerfile:
FROM debian:jessie-backports

# explicitly set user/group IDs
RUN groupadd -r cassandra --gid=999 && useradd -r -g cassandra --uid=999 cassandra


# grab gosu for easy step-down from root
ENV GOSU_VERSION 1.7
RUN set -x \
        && apt-get update && apt-get install -y --no-install-recommends ca-certificates jq wget curl && rm -rf /var/lib/apt/lists/* \
        && wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture)" \
        && wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture).asc" \
        && export GNUPGHOME="$(mktemp -d)" \
        && gpg --keyserver hkp://ha.pool.sks-keyservers.net:80 --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
        && gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu \
        && rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc \
        && chmod +x /usr/local/bin/gosu \
        && gosu nobody true \
        && apt-get purge -y --auto-remove ca-certificates

# solves warning: "jemalloc shared library could not be preloaded to speed up memory allocations"
RUN apt-get update && apt-get install -y --no-install-recommends libjna-java libjemalloc1 && rm -rf /var/lib/apt/lists/*

# https://github.com/docker-library/cassandra/pull/98#issuecomment-280761137
RUN { \
                echo 'Package: openjdk-* ca-certificates-java'; \
                echo 'Pin: release n=*-backports'; \
                echo 'Pin-Priority: 990'; \
        } > /etc/apt/preferences.d/java-backports

RUN apt-get update && apt-get install -y --no-install-recommends openjdk-8-jre-headless python python-pip && \
    rm -rf /var/lib/apt/lists/* && \
    pip install cassandra-driver yq

LABEL maintainer="support@strapdata.com"
LABEL description="Elassandra docker image"

ARG elassandra_version
ARG elassandra_url=https://github.com/strapdata/elassandra/releases/download/v${elassandra_version}/elassandra-${elassandra_version}.tar.gz}

ENV ELASSANDRA_VERSION=${elassandra_version}
ENV ELASSANDRA_URL=${elassandra_url}

ENV ELASSANDRA_VERSION ${ELASSANDRA_VERSION} 
ENV CASSANDRA_HOME /opt/elassandra-${ELASSANDRA_VERSION}
ENV CASSANDRA_CONF /opt/elassandra-${ELASSANDRA_VERSION}/conf
ENV CASSANDRA_PIDFILE /opt/elassandra-${ELASSANDRA_VERSION}/cassandra.pid
ENV PATH $PATH:/opt/elassandra-${ELASSANDRA_VERSION}/bin

# Copy or download elassandra tarball
RUN /bin/bash -c "if [[ $ELASSANDRA_URL == http* ]]; then wget -q -O /elassandra-$ELASSANDRA_VERSION.tar.gz $ELASSANDRA_URL; fi"
COPY --chown=cassandra:cassandra ready-probe.sh elassandra-${ELASSANDRA_VERSION}.tar.gz* /

RUN cd /opt \
  && tar -xzf "/elassandra-${ELASSANDRA_VERSION}.tar.gz" \
  && rm -v "/elassandra-${ELASSANDRA_VERSION}.tar.gz" \
  && chown -R cassandra:cassandra "$CASSANDRA_HOME" \
  && chmod -R o-rwx "$CASSANDRA_HOME"

# Add custom logback.xml including variables.
COPY --chown=cassandra:cassandra logback.xml "$CASSANDRA_HOME"/conf/

RUN mkdir -p /var/lib/cassandra /var/log/cassandra \
  && rm -r "$CASSANDRA_HOME"/data "$CASSANDRA_HOME"/logs \
  && ln -s /var/lib/cassandra "$CASSANDRA_HOME"/data \
  && ln -s /var/log/cassandra "$CASSANDRA_HOME"/logs \
  && chown -R cassandra:cassandra /var/lib/cassandra /var/log/cassandra "$CASSANDRA_HOME"/data "$CASSANDRA_HOME"/logs \
  && chmod 750 /var/lib/cassandra /var/log/cassandra \
  && chmod a+rx /ready-probe.sh

COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

VOLUME /var/lib/cassandra

# 7000: intra-node communication
# 7001: TLS intra-node communication
# 7199: JMX
# 9042: CQL
# 9160: thrift service
# 9200: elassandra HTTP
# 9300: elasticsearch transport
EXPOSE 7000 7001 7199 9042 9160 9200 9300
CMD ["bin/cassandra"]
