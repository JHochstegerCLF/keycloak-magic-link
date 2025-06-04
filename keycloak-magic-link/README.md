# Cloudflight keycloak magic link plugin

The hassle-free magic link plugin. 
This plugin provides two authenticator implementations for keycloak that use magic links to log in users (passwordless).


## Quickstart

There is an example for the setup which can be found in the `example` folder of this project.
To get started run:
```bash
./mvnw package
docker compose up --build
```

The started dev instance of keycloak (http://localhost:8080) comes with a `demo` realm and the "Magic link Continuation Authenticator" preconfigured.

You can access a MailHog instance on http://localhost:8025 to find the emails and login using the demo user with `demo@example.org`.
To test the flow you can use the account-console at http://localhost:8080/realms/demo/account .

## Authenticators
The typical flow for a passwordless authenticator is:

1) An email input form is shown.
2) User enters their email address and submits the form.
3) A special ("magic") link is sent to their email address.
4) They receive the email with the link, click on it and get authenticated.
5) The used magic link contains a "magic key" which acts as a one-time-password.

### "Normal" Authenticator
Users open the magic link in the same context (same browser) as the original login request.
Then, the authenticated session continues in the browser/tab that opened after clicking the link.


### "Continuation" Authenticator
Users open the magic link in a different context (different browser/device) as the original login request.
Then, the authenticated session continues in the original browser/tab that was used for the login
request. Users get not authenticated in browser/tab that opened after clicking the link. This allows
to log in on the desktop, but receive the email on a mobile device.


## Realm Config
To make use of this plugin you might want to...
- customize the UI ([see Templates](#templates))
- consider Security implications of magic-links ([see Crypthography](#Cryptographic aspects))
## Templates
THere are three ftl files for this plugin:

- `magiclink-email.ftl`
  - Template for the email
- `email-login.ftl`
    - Template for the Login page
- `wait-for-login.ftl`
    - Template for after sending the email in the continuation flow

## Cryptographic aspects
The "magic key" one time password in the links is a type 4 UUID generated using the default java implementation.
This 
Static factory to retrieve a type 4 (pseudo randomly generated) UUID. The UUID is generated using a cryptographically strong pseudo random number generator.
