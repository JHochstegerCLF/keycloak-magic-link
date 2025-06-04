package io.cloudflight.keycloak.magiclink.util;

import io.cloudflight.keycloak.magiclink.entity.MagicLinkSession;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.common.util.Time;

import java.util.regex.Pattern;

/**
 * Utility class containing logic around validation of input/magic keys.
 *
 * @author Ludwig Burtscher (ludwig.burtscher@cloudflight.io)
 */
public class ValidationUtils {

    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");


    /**
     * Checks if the given received magic key is valid for the given session.
     *
     * @param session          The session (was stored when the magic link is sent)
     * @param receivedMagicKey The magic key received when the user clicked the magic link
     * @return true if the magic key is valid, false otherwise
     */
    public static boolean isMagicLinkSessionValid(MagicLinkSession session, String receivedMagicKey) {
        if (session == null || ObjectUtil.isBlank(receivedMagicKey)) {
            return false;
        }

        if (!session.getMagicKey().equals(receivedMagicKey)) {
            return false;
        }

        if (session.getValidTo() < Time.currentTimeMillis()) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a given input string is a valid UUID.
     * Used for validating input.
     *
     * @param input The input string
     * @return true if the input string is a valid UUID, false otherwise
     */
    public static boolean isUUID(String input) {
        return !ObjectUtil.isBlank(input) && UUID_PATTERN.matcher(input).matches();
    }


    private ValidationUtils() {
        //prevent instantiation
    }

}
