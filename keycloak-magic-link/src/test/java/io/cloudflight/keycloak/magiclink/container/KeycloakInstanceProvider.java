package io.cloudflight.keycloak.magiclink.container;

/**
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
public class KeycloakInstanceProvider {

    private static final StringRealmImportKeycloakContainer keycloak = new StringRealmImportKeycloakContainer()
          .withDefaultProviderClasses();


    public static void start() {
        start(null);
    }

    public static void start(String realmContent) {
        if (realmContent != null) {
            keycloak.withRealmImportString(realmContent);
        }
        keycloak.start();
    }

    public static void stop() {
        keycloak.close();
    }

    public static KeycloakInstanceInfo getInfo() {
        return new KeycloakInstanceInfo(
              keycloak.getAuthServerUrl()
        );
    }


    public record KeycloakInstanceInfo(
          String authServerUrl
    ) {

    }

}
