package io.cloudflight.keycloak.magiclink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.cloudflight.keycloak.magiclink.container.KeycloakInstanceProvider;
import io.cloudflight.keycloak.magiclink.util.RealmTemplate;

/**
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
class MagicLinkNormalTest extends AbstractMagicLinkBaseTest {

    private static final String REALM_TEMPLATE = "magiclink-normal.json.j2";


    @Test
    @RealmTemplate(REALM_TEMPLATE)
    void testEmailSent() {
        KeycloakInstanceProvider.KeycloakInstanceInfo info = KeycloakInstanceProvider.getInfo();

        loginWithEmailAddress(info.authServerUrl(), "admin1@example.org");
        Email email = receiveEmail(1, 0);

        assertNotNull(email);
        assertEquals("keycloak@example.org", email.from());
        assertEquals("admin1@example.org", email.to());
        assertEquals("Login to Keycloak", email.subject());

        assertNotNull(email.link());
        assertTrue(email.bodyTxt().contains(email.link()));
        assertTrue(email.bodyHtml().contains(email.link()));
    }

    @Test
    @RealmTemplate(REALM_TEMPLATE)
    void testLoginSuccessful() {
        KeycloakInstanceProvider.KeycloakInstanceInfo info = KeycloakInstanceProvider.getInfo();

        loginWithEmailAddress(info.authServerUrl(), "admin1@example.org");
        Email email = receiveEmail(1, 0);

        assertNotNull(email);
        assertNotNull(email.link());

        String response = openLink(email.link());
        assertLogin(response, true);
    }

    @Test
    @RealmTemplate(REALM_TEMPLATE)
    void testLoginWrongUser() {
        KeycloakInstanceProvider.KeycloakInstanceInfo info = KeycloakInstanceProvider.getInfo();

        loginWithEmailAddress(info.authServerUrl(), "invalid@example.org");
        Email email = receiveEmail(0, 0);
        assertNull(email);

        String response = openLink(info.authServerUrl());
        assertLogin(response, false);
    }

    @Test
    @RealmTemplate(REALM_TEMPLATE)
    void testLoginLinkOnlyValidOnce() {
        KeycloakInstanceProvider.KeycloakInstanceInfo info = KeycloakInstanceProvider.getInfo();

        loginWithEmailAddress(info.authServerUrl(), "admin1@example.org");
        Email email = receiveEmail(1, 0);
        assertNotNull(email);

        String response = openLink(email.link());
        assertLogin(response, true);

        logout();

        //try logging in again
        response = openLink(email.link());
        assertLogin(response, false);
    }


    //This test is currently disabled until we find a way to manipulate time in keycloak.
    @Disabled
    @Test
    @RealmTemplate(REALM_TEMPLATE)
    void testLinkExpiry() {
        KeycloakInstanceProvider.KeycloakInstanceInfo info = KeycloakInstanceProvider.getInfo();

        loginWithEmailAddress(info.authServerUrl(), "admin1@example.org");

        //TODO manipulate time such that the subsequent login attempt fails because the magic link is already expired

        Email email = receiveEmail(1, 0);
        assertNotNull(email);

        String response = openLink(email.link());
        assertLogin(response, false);
    }


    private void assertLogin(String response, boolean successful) {
        assertEquals(successful, response.contains("Welcome to Keycloak"));
    }

}
