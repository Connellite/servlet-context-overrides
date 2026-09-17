package io.github.connellite.overrides;

/**
 * Optional override values.
 */
record OverrideConfig(
        Integer sessionTimeout,
        String requestCharacterEncoding,
        String responseCharacterEncoding,
        String sessionCookieName,
        String sessionCookieDomain,
        String sessionCookiePath,
        Integer sessionCookieMaxAge,
        Boolean sessionCookieHttpOnly,
        Boolean sessionCookieSecure,
        String sessionTrackingModes
) {
    boolean isEmpty() {
        return sessionTimeout == null
                && isBlank(requestCharacterEncoding)
                && isBlank(responseCharacterEncoding)
                && isBlank(sessionCookieName)
                && isBlank(sessionCookieDomain)
                && isBlank(sessionCookiePath)
                && sessionCookieMaxAge == null
                && sessionCookieHttpOnly == null
                && sessionCookieSecure == null
                && isBlank(sessionTrackingModes);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
