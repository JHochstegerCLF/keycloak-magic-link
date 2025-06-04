package io.cloudflight.keycloak.magiclink.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RealmTemplate {
    String value();
}
