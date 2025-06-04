package io.cloudflight.keycloak.magiclink.authenticators;

/**
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
public class MagicLinkValidityConstants {

    /**
     * The key of the keycloak config to set how long a magic key is valid.
     */
    public static final String VALIDITY_DURATION_CONFIG_KEY = "magickey.validity.duration";

    /**
     * The default validity duration in seconds.
     */
    public static final long DEFAULT_VALIDITY_IN_SECONDS = 5 * 60L;


    private MagicLinkValidityConstants() {
        //prevent instantiation
    }
}
