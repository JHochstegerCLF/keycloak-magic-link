package io.cloudflight.keycloak.magiclink;

import static org.junit.jupiter.api.Assertions.*;

import com.microsoft.playwright.options.LoadState;
import io.cloudflight.keycloak.magiclink.container.KeycloakInstanceProvider;
import io.cloudflight.keycloak.magiclink.util.RealmTemplate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
class MagicLinkContinuationTest extends AbstractMagicLinkBaseTest {

    private static final String REALM_TEMPLATE = "magiclink-continuation.json.j2";


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

        String response = openLinkInNewBrowser(email.link());
        assertLogin(response, false);

        assertOriginalSessionLoggedIn(true);
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

        openLinkInNewBrowser(email.link());
        assertOriginalSessionLoggedIn(true);

        logout();

        //try logging in again
        openLinkInNewBrowser(email.link());
        assertOriginalSessionLoggedIn(false);
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

        openLinkInNewBrowser(email.link());
        assertOriginalSessionLoggedIn(false);
    }


    private void assertLogin(String response, boolean successful) {
        assertEquals(successful, response.contains("Welcome to Keycloak"));
    }

    private void assertOriginalSessionLoggedIn(boolean successful) {
        page.reload();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        String content = page.content();
        assertLogin(content, successful);
    }

}
