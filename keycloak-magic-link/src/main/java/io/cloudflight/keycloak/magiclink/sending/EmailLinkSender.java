package io.cloudflight.keycloak.magiclink.sending;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.common.util.ObjectUtil;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
public class EmailLinkSender implements LinkSender {

    private static final String EMAIL_TEMPLATE_FILE = "magiclink-email.ftl";

    @Override
    public void sendLink(KeycloakSession session, UserModel user, String link) throws IOException {
        EmailTemplateProvider templateProvider = session.getProvider(EmailTemplateProvider.class);
        RealmModel realm = session.getContext().getRealm();
        final String realmName = ObjectUtil.isBlank(realm.getDisplayName()) ? realm.getName() : realm.getDisplayName();
        try {
            templateProvider
                  .setRealm(realm)
                  .setUser(user)
                  .setAttribute("realmName", realmName)
                  .send("magiclink-emailSubject", List.of(realmName), EMAIL_TEMPLATE_FILE, new HashMap<>(Map.of("link", link)));
        } catch (EmailException e) {
            throw new IOException(e);
        }
    }
}
