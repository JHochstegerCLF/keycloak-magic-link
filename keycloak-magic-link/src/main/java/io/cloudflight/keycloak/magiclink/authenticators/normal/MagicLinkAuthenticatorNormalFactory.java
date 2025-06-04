package io.cloudflight.keycloak.magiclink.authenticators.normal;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;

import io.cloudflight.keycloak.magiclink.authenticators.AbstractMagicLinkAuthenticatorFactory;

/**
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
public class MagicLinkAuthenticatorNormalFactory extends AbstractMagicLinkAuthenticatorFactory {

    private static final String PROVIDER_ID = "magiclink";

    private static final MagicLinkAuthenticatorNormal INSTANCE = new MagicLinkAuthenticatorNormal();


    @Override
    public String getDisplayType() {
        return "Magic Link";
    }

    @Override
    public String getHelpText() {
        return "Authenticator that sends a magic link which can be used to log in";
    }

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
