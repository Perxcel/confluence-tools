package com.perxcel.confluence.tools;

import org.apache.commons.codec.binary.Base64;

/**
 * Build an Authorization Token
 */
public interface AuthTokenBuilder {

    static String buildAuthHeader(final String email, final String apiToken) {
        String token = email + ":" + apiToken;
        return Base64.encodeBase64String(token.getBytes());
    }
}
