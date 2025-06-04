ARG KEYCLOAK_VERSION
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder
# Add the provider JAR file to the providers directory
ADD --chown=keycloak:keycloak --chmod=644 target/keycloak-magic-link-1.0-SNAPSHOT.jar /opt/keycloak/providers/magiclink.jar

# Context: RUN the build command
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION}
COPY --from=builder /opt/keycloak/ /opt/keycloak/
COPY example/realms /opt/keycloak/data/import
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
