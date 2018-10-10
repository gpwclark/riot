FROM openjdk:8

ENV RIOT_HOME /opt/riot
ENV IGNITE_HOME /opt/run

RUN apt-get update && apt-get install -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt

CMD cp -r /opt/riot /opt/run && cd /opt/run && ls -l && ./gradlew --stacktrace -Dtest.single=ComposeRuleCompanionTest test
