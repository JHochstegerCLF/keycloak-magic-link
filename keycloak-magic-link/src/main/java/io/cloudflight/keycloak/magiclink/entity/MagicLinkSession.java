package io.cloudflight.keycloak.magiclink.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Custom entity for storing magic link login request information.
 * This entity is used to check if the received magic key from the clicked magic link is valid.
 * It is created when the magic link is sent.
 *
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
@Entity
@Table(name = "MAGIC_LINK_SESSION")
public class MagicLinkSession {

    @Id
    @Column(name = "ID", length = 36, nullable = false)
    private String id;

    @Column(name = "MAGIC_KEY", nullable = false)
    private String magicKey;

    @Column(name = "VALID_TO", nullable = false)
    private long validTo;

    @Column(name = "LOGGED_IN", nullable = false)
    private boolean loggedIn = false;

    @Column(name = "REDIRECT_URI")
    private String redirectUri;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMagicKey() {
        return magicKey;
    }

    public void setMagicKey(String magicKey) {
        this.magicKey = magicKey;
    }

    public long getValidTo() {
        return validTo;
    }

    public void setValidTo(long validTo) {
        this.validTo = validTo;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getRedirectUri() {
        return this.redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
