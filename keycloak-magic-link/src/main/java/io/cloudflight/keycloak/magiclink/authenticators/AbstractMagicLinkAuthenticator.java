package io.cloudflight.keycloak.magiclink.authenticators;

import java.io.IOException;
import java.util.UUID;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

import io.cloudflight.keycloak.magiclink.entity.MagicLinkSession;
import io.cloudflight.keycloak.magiclink.sending.EmailLinkSender;
import io.cloudflight.keycloak.magiclink.sending.LinkSender;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.Response;

/**
 * Common implementation for magic link authenticators.
 *
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
public abstract class AbstractMagicLinkAuthenticator implements MagicLinkAuthenticator {

    protected static final String EMAIL_INPUT_FORM_TEMPLATE = "email-login.ftl";
    protected static final String EMAIL_ATTRIBUTE_FORM_NAME = "username";

    protected static final String MAGICKEY_QUERY_PARAM = "magickey";
    protected static final String MAGICLINK_SESSION_ID_KEY = "magiclink-session-id";

    private final LinkSender linkSender = new EmailLinkSender();
    private static final Logger logger = Logger.getLogger(AbstractMagicLinkAuthenticator.class);


    @Override
    public void action(AuthenticationFlowContext context) {
        // Get email from the submitted form
        final String email = getEmailAddressInput(context);
        if (ObjectUtil.isBlank(email)) {
            return;
        }

        UserModel user = findUserByEmailAddress(context, email);
        if (user != null) {
            // Create a magic link and send it only if the user exists
            context.setUser(user);
            final String magicKey = generateMagicKey();
            final String magicLinkSessionId = UUID.randomUUID().toString();
            storeMagicKey(context, magicKey, magicLinkSessionId);
            sendLink(context, getMagicLink(context, magicKey, magicLinkSessionId));
        }

        // Show waiting page even if user does not exist -> prevents guessing of users
        showLinkSentInfo(context);
    }

    @Override
    public void sendLink(AuthenticationFlowContext context, String magicLink) {
        try {
            linkSender.sendLink(context.getSession(), context.getUser(), magicLink);
        } catch (IOException e) {
            logger.warn("MagicLink not generated", e);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR, Response.serverError().build());
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        // not needed
    }

    @Override
    public void close() {
        // not needed
    }


    protected abstract String getMagicLink(AuthenticationFlowContext context, String magicKey, String magicLinkSessionId);

    protected abstract void showLinkSentInfo(AuthenticationFlowContext context);


    protected String generateMagicKey() {
        return KeycloakModelUtils.generateId();
    }

    protected void storeMagicKey(AuthenticationFlowContext context, String magicKey, String magicLinkSessionId) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        long validityDurationInSeconds = MagicLinkValidityConstants.DEFAULT_VALIDITY_IN_SECONDS;
        if (config != null) {
            validityDurationInSeconds = Integer.parseInt(config.getConfig().get(MagicLinkValidityConstants.VALIDITY_DURATION_CONFIG_KEY));
        }

        final long validTo = Time.currentTimeMillis() + validityDurationInSeconds * 1000L;

        MagicLinkSession magicLinkSession = new MagicLinkSession();
        magicLinkSession.setId(magicLinkSessionId);
        magicLinkSession.setMagicKey(magicKey);
        magicLinkSession.setValidTo(validTo);
        magicLinkSession.setRedirectUri(context.getRefreshUrl(true).toString());

        context.getAuthenticationSession().setAuthNote(MAGICLINK_SESSION_ID_KEY, magicLinkSessionId);
        getEntityManager(context).persist(magicLinkSession);
    }

    protected EntityManager getEntityManager(AuthenticationFlowContext context) {
        return context.getSession().getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    protected Response getEmailLoginForm(AuthenticationFlowContext context) {
        return context.form()
              .setAttribute(EMAIL_ATTRIBUTE_FORM_NAME, "")
              .setAuthContext(null)
              .createForm(EMAIL_INPUT_FORM_TEMPLATE);
    }

    protected String getEmailAddressInput(AuthenticationFlowContext context) {
        String email = context.getHttpRequest().getDecodedFormParameters().getFirst(EMAIL_ATTRIBUTE_FORM_NAME).trim();

        if (ObjectUtil.isBlank(email)) {
            context.failure(AuthenticationFlowError.INVALID_USER,
                  context.form().setError("Email cannot be empty").createForm(EMAIL_INPUT_FORM_TEMPLATE));
            return null;
        }
        return email;
    }

    protected UserModel findUserByEmailAddress(AuthenticationFlowContext context, String email) {
        return KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), email);
    }

    protected void removeMagicLinkSession(AuthenticationFlowContext context, MagicLinkSession session) {
        EntityManager em = getEntityManager(context);
        em.getTransaction().begin();
        em.remove(session);
        em.getTransaction().commit();
    }

}
