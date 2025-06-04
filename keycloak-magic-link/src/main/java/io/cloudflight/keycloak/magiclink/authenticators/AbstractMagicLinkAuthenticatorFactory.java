package io.cloudflight.keycloak.magiclink.authenticators;

import static io.cloudflight.keycloak.magiclink.authenticators.MagicLinkValidityConstants.DEFAULT_VALIDITY_IN_SECONDS;
import static io.cloudflight.keycloak.magiclink.authenticators.MagicLinkValidityConstants.VALIDITY_DURATION_CONFIG_KEY;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Common implementation for magic link authenticator factories.
 *
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
public abstract class AbstractMagicLinkAuthenticatorFactory implements AuthenticatorFactory {

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty magicKeyValidityDuration = new ProviderConfigProperty();
        magicKeyValidityDuration.setName(VALIDITY_DURATION_CONFIG_KEY);
        magicKeyValidityDuration.setLabel("MagicKey Validity Duration in seconds");
        magicKeyValidityDuration.setType(ProviderConfigProperty.INTEGER_TYPE);
        magicKeyValidityDuration.setHelpText("Duration in seconds that a magic key is valid");
        magicKeyValidityDuration.setRequired(true);
        magicKeyValidityDuration.setDefaultValue(DEFAULT_VALIDITY_IN_SECONDS);
        return List.of(magicKeyValidityDuration);
    }

    @Override
    public void init(Config.Scope scope) {
        // not needed
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // not needed
    }

    @Override
    public void close() {
        // not needed
    }

}
