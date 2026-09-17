package io.github.connellite.overrides;

import lombok.extern.java.Log;
import org.apache.catalina.Context;

#if JAKARTA
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.ServletContext;
#else
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.ServletContext;
#endif

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Applies {@link OverrideConfig} to a Tomcat {@link Context} / {@link ServletContext}.
 * Unset ({@code null} / blank) options are skipped.
 */
@Log
final class ServletContextOverridesApplier {

    enum Phase {
        /** CONFIGURE_START: encodings, cookie config, tracking modes, session timeout. */
        EARLY,
        /** AFTER_START: session timeout via Tomcat Context API only (wins over web.xml). */
        LATE
    }

    private ServletContextOverridesApplier() {
    }

    static void apply(Context context, OverrideConfig config, Phase phase) {
        if (config == null || config.isEmpty()) {
            return;
        }

        if (config.sessionTimeout() != null) {
            context.setSessionTimeout(config.sessionTimeout());
            log.info(() -> "sessionTimeout=" + config.sessionTimeout() + " (" + phase + ") context=" + context.getPath());
        }

        if (phase == Phase.LATE) {
            return;
        }

        ServletContext servletContext = context.getServletContext();
        if (servletContext == null) {
            log.warning("ServletContext is null during EARLY apply; skipping ServletContext overrides");
            return;
        }

        applyServletContext(servletContext, config);
    }

    /**
     * Applies ServletContext-scoped overrides only (no Tomcat {@link Context#setSessionTimeout}).
     */
    static void applyServletContext(ServletContext servletContext, OverrideConfig config) {
        if (config == null || servletContext == null) {
            return;
        }

        if (notBlank(config.requestCharacterEncoding())) {
            servletContext.setRequestCharacterEncoding(config.requestCharacterEncoding().trim());
            log.info(() -> "requestCharacterEncoding=" + config.requestCharacterEncoding().trim());
        }
        if (notBlank(config.responseCharacterEncoding())) {
            servletContext.setResponseCharacterEncoding(config.responseCharacterEncoding().trim());
            log.info(() -> "responseCharacterEncoding=" + config.responseCharacterEncoding().trim());
        }

        applySessionCookieConfig(servletContext, config);
        applySessionTrackingModes(servletContext, config);
    }

    private static void applySessionCookieConfig(ServletContext servletContext, OverrideConfig config) {
        SessionCookieConfig cookieConfig = servletContext.getSessionCookieConfig();
        if (notBlank(config.sessionCookieName())) {
            cookieConfig.setName(config.sessionCookieName().trim());
        }
        if (notBlank(config.sessionCookieDomain())) {
            cookieConfig.setDomain(config.sessionCookieDomain().trim());
        }
        if (notBlank(config.sessionCookiePath())) {
            cookieConfig.setPath(config.sessionCookiePath().trim());
        }
        if (config.sessionCookieMaxAge() != null) {
            cookieConfig.setMaxAge(config.sessionCookieMaxAge());
        }
        if (config.sessionCookieHttpOnly() != null) {
            cookieConfig.setHttpOnly(config.sessionCookieHttpOnly());
        }
        if (config.sessionCookieSecure() != null) {
            cookieConfig.setSecure(config.sessionCookieSecure());
        }
        log.info("SessionCookieConfig overrides applied");
    }

    private static void applySessionTrackingModes(ServletContext servletContext, OverrideConfig config) {
        if (!notBlank(config.sessionTrackingModes())) {
            return;
        }

        Set<SessionTrackingMode> modes = EnumSet.noneOf(SessionTrackingMode.class);
        for (String token : config.sessionTrackingModes().split("[,\\s]+")) {
            if (token.isBlank()) {
                continue;
            }
            modes.add(SessionTrackingMode.valueOf(token.trim().toUpperCase(Locale.ROOT)));
        }
        if (modes.isEmpty()) {
            return;
        }
        servletContext.setSessionTrackingModes(modes);
        log.info(() -> "sessionTrackingModes=" + modes);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
