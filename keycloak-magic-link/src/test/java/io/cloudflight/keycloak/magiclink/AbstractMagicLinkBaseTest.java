package io.cloudflight.keycloak.magiclink;

import static io.cloudflight.keycloak.magiclink.util.PlaywrightKeycloakConstants.BUTTON_LOGIN_LOCATOR;
import static io.cloudflight.keycloak.magiclink.util.PlaywrightKeycloakConstants.INPUT_EMAIL_LOCATOR;
import static io.cloudflight.keycloak.magiclink.util.PlaywrightKeycloakConstants.SIGNOUT_LOCATOR;
import static io.cloudflight.keycloak.magiclink.util.PlaywrightKeycloakConstants.USERINFO_LOCATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.common.util.Time;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import io.cloudflight.keycloak.magiclink.container.KeycloakInstanceProvider;
import io.cloudflight.keycloak.magiclink.container.MailhogInstanceProvider;
import io.cloudflight.keycloak.magiclink.util.KeycloakRealmTemplateExtension;

/**
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
@Testcontainers
@ExtendWith(KeycloakRealmTemplateExtension.class)
abstract class AbstractMagicLinkBaseTest {

    private static final Pattern LINK_PATTERN = Pattern.compile("(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    protected static boolean headless = true;

    protected static Playwright playwright;

    protected static Browser browser;

    protected BrowserContext browserContext;

    protected Page page;


    @BeforeAll
    static void beforeAll() {
        MailhogInstanceProvider.start();
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    }

    @AfterAll
    static void afterAll() {
        MailhogInstanceProvider.stop();
        playwright.close();
    }

    @AfterEach
    void afterEach() {
        playwright.request().newContext().delete(MailhogInstanceProvider.getInfo().apiV1Url() + "messages");
        KeycloakInstanceProvider.stop();
        browserContext.close();
        Time.setOffset(0);
    }

    @BeforeEach
    void beforeEach() {
        browserContext = browser.newContext();
        page = browserContext.newPage();
    }


    protected void loginWithEmailAddress(String authServerUrl, String email) {
        page.navigate(authServerUrl);
        page.locator(INPUT_EMAIL_LOCATOR).click();
        page.locator(INPUT_EMAIL_LOCATOR).fill(email);
        page.locator(BUTTON_LOGIN_LOCATOR).click();
    }

    protected Email receiveEmail(int expectedEmailCount, int emailIndex) {
        if (expectedEmailCount < 0) {
            return null;
        }

        final String url = MailhogInstanceProvider.getInfo().apiV2Url() + "messages";
        APIRequestContext request = playwright.request().newContext();
        JsonObject json = new Gson().fromJson(request.get(url).text(), JsonObject.class);

        if (expectedEmailCount == 0) {
            assertEquals(0, json.get("total").getAsInt());
            return null;
        }

        assertTrue(emailIndex < expectedEmailCount, "Requested email index must be smaller than expected number of emails");
        JsonObject jsonEmail = json.getAsJsonArray("items").get(emailIndex).getAsJsonObject();
        JsonObject headers = jsonEmail.getAsJsonObject("Content").getAsJsonObject("Headers");

        return new Email(
              headers.getAsJsonArray("From").get(0).getAsString(),
              headers.getAsJsonArray("To").get(0).getAsString(),
              headers.getAsJsonArray("Subject").get(0).getAsString(),
              getEmailBody(jsonEmail, "text/plain"),
              getEmailBody(jsonEmail, "text/html"),
              getEmailLink(getEmailBody(jsonEmail, "text/plain"))
        );
    }

    protected String openLink(String link) {
        Page loginPage = browserContext.newPage();
        loginPage.navigate(link);
        loginPage.waitForLoadState(LoadState.NETWORKIDLE);
        return loginPage.content();
    }

    protected String openLinkInNewBrowser(String link) {
        Browser separateBrowser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        Page newPage = separateBrowser.newContext().newPage();
        newPage.navigate(link);
        String response = newPage.content();
        separateBrowser.close();
        return response;
    }

    protected void logout() {
        page.locator(USERINFO_LOCATOR).click();
        page.locator(SIGNOUT_LOCATOR).click();
    }


    private String getEmailBody(JsonObject email, String contentType) {
        return email.getAsJsonObject("MIME").getAsJsonArray("Parts").asList().stream()
              .filter(part -> part.getAsJsonObject().getAsJsonObject("Headers")
                    .getAsJsonArray("Content-Type").asList().stream()
                        .anyMatch(value -> value.getAsString().contains(contentType))
              )
              .findFirst()
              .map(part -> part.getAsJsonObject().get("Body").getAsString())
              .orElse(null);
    }

    private String getEmailLink(String body) {
        Matcher matcher = LINK_PATTERN.matcher(body);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }


    public record Email(
          String from,
          String to,
          String subject,
          String bodyTxt,
          String bodyHtml,
          String link
    ) {

    }

}
