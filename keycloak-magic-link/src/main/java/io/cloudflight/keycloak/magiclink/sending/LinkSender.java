package io.cloudflight.keycloak.magiclink.sending;

import java.io.IOException;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * Abstraction to send magic links to users.
 * Usually, this is done via email, but alternative options might be implemented in the future.
 *
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
public interface LinkSender {

    /**
     * Sends a given magic link to a given user.
     *
     * @param session The Keycloak session
     * @param user    The recipient (user who requested login)
     * @param link    The magic link
     * @throws IOException if the link was not sent successfully
     */
    void sendLink(KeycloakSession session, UserModel user, String link) throws IOException;
}
