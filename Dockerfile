# Unfortunately native build does not work (see Dockerfile.native).
# Most probably because old API 'javax.ws.rs:javax.ws.rs-api:2.1.1' used by atlassian JIRA REST Client
# So, we use JVM mode instead

# Dockerfile primarly to have single well-known entrypoint
# From https://www.graalvm.org/latest/docs/getting-started/container-images/
FROM ghcr.io/graalvm/native-image-community:21 as builder
#FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:22.3-java17 as builder

WORKDIR /app

COPY --chown=1001 . /app

RUN chmod "g+rwX" /app

RUN microdnf install findutils

# Tests disabled because they are integration now and are require external JIRA instance to function
RUN ./gradlew build -x test

## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ## ##

#FROM registry.access.redhat.com/ubi8/openjdk-17:1.15
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.22-1.1749462970

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=builder --chown=185 /app/build/quarkus-app/lib/ /deployments/lib/
COPY --from=builder --chown=185 /app/build/quarkus-app/*.jar /deployments/
COPY --from=builder --chown=185 /app/build/quarkus-app/app/ /deployments/app/
COPY --from=builder --chown=185 /app/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

