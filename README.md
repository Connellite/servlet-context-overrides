# Servlet Context Overrides

Tomcat `LifecycleListener` that overrides selected `ServletContext` / session settings
from attributes on a `<Listener>` in `context.xml` (outside the WAR).

Unset attributes are left alone — the WAR `WEB-INF/web.xml` value stays in effect.

## Install

1. Build the JAR and copy it to `$CATALINA_BASE/lib` (or `$CATALINA_HOME/lib`).
2. Configure the listener in an **external** context file so redeploys do not wipe it:
   `conf/Catalina/localhost/<app>.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Listener className="io.github.connellite.overrides.ServletContextOverridesListener"
              sessionTimeout="60"
              requestCharacterEncoding="UTF-8"
              responseCharacterEncoding="UTF-8"
              sessionCookieHttpOnly="true"
              sessionTrackingModes="COOKIE"/>
</Context>
```

Only list the options you want to force. Everything else is untouched.

## Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| `sessionTimeout` | int (minutes) | Default HTTP session timeout |
| `requestCharacterEncoding` | string | Default request character encoding |
| `responseCharacterEncoding` | string | Default response character encoding |
| `sessionCookieName` | string | Session cookie name |
| `sessionCookieDomain` | string | Session cookie domain |
| `sessionCookiePath` | string | Session cookie path |
| `sessionCookieMaxAge` | int (seconds) | Session cookie max-age |
| `sessionCookieHttpOnly` | boolean | Session cookie HttpOnly flag |
| `sessionCookieSecure` | boolean | Session cookie Secure flag |
| `sessionTrackingModes` | string | Comma/space-separated: `COOKIE`, `URL`, `SSL` |

## Behaviour

- **CONFIGURE_START** — applies encodings, session cookie settings, tracking modes, and session timeout.
- **AFTER_START** — reapplies `sessionTimeout` via Tomcat `Context.setSessionTimeout`, so it wins over `web.xml` even if listener order vs `ContextConfig` varies.

Blank strings are treated as unset.

## Requirements

- Java 17+
- Tomcat 9.x (`javax` build) or Tomcat 10.1+ / 11 (`jakarta` build)

## Build

| Profile | Servlet API | Tomcat |
|---------|-------------|----------------|
| `javax` (default) | `javax.servlet` 4.0 | 9.x |
| `jakarta` | `jakarta.servlet` 6.0 | 10.1+ / 11 |

```bash
mvn clean package
mvn clean package -Pjakarta
```

Artifacts:

- `servlet-context-overrides-1.0-beta-1.jar` — javax / Tomcat 9
- `servlet-context-overrides-1.0-beta-1-jakarta.jar` — jakarta / Tomcat 10.1+

Servlet imports are selected at compile time with the
[Manifold preprocessor](https://github.com/manifold-systems/manifold/tree/master/manifold-deps-parent/manifold-preprocessor)
(`JAVAX` / `JAKARTA` in `build.properties`).
