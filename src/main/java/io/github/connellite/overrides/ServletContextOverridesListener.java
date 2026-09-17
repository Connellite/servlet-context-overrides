package io.github.connellite.overrides;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import java.util.logging.Level;

/**
 * Tomcat {@link LifecycleListener} that overrides selected {@code ServletContext}
 * / session settings from attributes on the {@code <Listener>} element in
 * {@code context.xml}.
 *
 * <p>Any attribute that is omitted is left unchanged (the corresponding WAR
 * {@code web.xml} value stays in effect).</p>
 *
 * <pre>{@code
 * <Context>
 *   <Listener className="io.github.connellite.overrides.ServletContextOverridesListener"
 *             sessionTimeout="60"
 *             requestCharacterEncoding="UTF-8"
 *             responseCharacterEncoding="UTF-8"/>
 * </Context>
 * }</pre>
 */
@Log
@Getter
@Setter
public class ServletContextOverridesListener implements LifecycleListener {

    /** Session timeout in minutes. */
    private Integer sessionTimeout;

    private String requestCharacterEncoding;
    private String responseCharacterEncoding;

    private String sessionCookieName;
    private String sessionCookieDomain;
    private String sessionCookiePath;
    private Integer sessionCookieMaxAge;
    private Boolean sessionCookieHttpOnly;
    private Boolean sessionCookieSecure;

    /**
     * Comma- or space-separated {@code SessionTrackingMode} names,
     * e.g. {@code COOKIE} or {@code COOKIE,URL}.
     */
    private String sessionTrackingModes;

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
        Object lifecycle = event.getLifecycle();
        if (!(lifecycle instanceof Context context)) {
            return;
        }

        String type = event.getType();
        if (Lifecycle.CONFIGURE_START_EVENT.equals(type)) {
            // Prefer applying encodings / cookie config before the context becomes available.
            // Session timeout is also applied here; AFTER_START re-applies it so we win over web.xml
            // regardless of LifecycleListener ordering vs ContextConfig.
            apply(context, ServletContextOverridesApplier.Phase.EARLY);
            return;
        }

        if (Lifecycle.AFTER_START_EVENT.equals(type)) {
            apply(context, ServletContextOverridesApplier.Phase.LATE);
        }
    }

    private void apply(Context context, ServletContextOverridesApplier.Phase phase) {
        try {
            ServletContextOverridesApplier.apply(context, toConfig(), phase);
        } catch (RuntimeException e) {
            log.log(Level.SEVERE, "Failed to apply servlet context overrides (" + phase + ")", e);
            throw e;
        }
    }

    OverrideConfig toConfig() {
        return new OverrideConfig(
                sessionTimeout,
                requestCharacterEncoding,
                responseCharacterEncoding,
                sessionCookieName,
                sessionCookieDomain,
                sessionCookiePath,
                sessionCookieMaxAge,
                sessionCookieHttpOnly,
                sessionCookieSecure,
                sessionTrackingModes
        );
    }
}
