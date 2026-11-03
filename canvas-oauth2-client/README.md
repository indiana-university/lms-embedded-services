# LMS Canvas OAuth2 Client

Per-user Canvas OAuth2 delegated access, as an alternative to the shared admin/service
token every tool gets from `canvas-services`. Lets a tool call the Canvas API *as the
actual logged-in user*, using a Canvas OAuth2 access token obtained via one-time
delegated consent, with automatic refresh — instead of the app's single static
`canvas.token`.

## The problem this solves

Every Canvas REST API call across the tools in this monorepo normally goes through
`shared/embedded-services/canvas-services`, whose `RestTemplate` beans all carry the
same static admin token (`canvas.token`), injected via `CanvasTokenAuthorizationInterceptor`.
That means every tool's Canvas API access is effectively "admin," regardless of whether
the operation it performs actually needs elevated privilege — and a single compromised
or leaked admin token can act as anyone, anywhere.

Many calls a tool makes are things Canvas would happily authorize against the *end
user's own* permissions (an instructor viewing their own course's roster, a user
favoriting their own course). LTI 1.3's OIDC login authenticates the *launch*; it's a
different protocol and doesn't produce a durable Canvas API token on its own. This
module adds that missing piece: real per-user Canvas OAuth2, wired into Spring
Security's standard OAuth2 client machinery.

## How it works

- Built on Spring Security's standard OAuth2 client abstractions
  (`ClientRegistrationRepository`, `OAuth2AuthorizedClientRepository`,
  `OAuth2AuthorizedClientManager`) — this module doesn't reinvent that machinery, it
  configures it for Canvas and adds the pieces Canvas-specific OAuth2 needs.
- Some of those Canvas-specific pieces are vendored (under `uk.ac.ox.ctl.oauth2`,
  originally from [`oxctl/canvas-spring-oauth2`](https://github.com/oxctl/canvas-spring-oauth2),
  updated for this repo's Spring version) — notably
  `OAuth2AccessTokenResponseHttpMessageConverter`, which fixes JSON deserialization of
  Canvas's non-standard token response shape (it includes a parameter with an object
  value that breaks a plain converter).
- Tokens are persisted per `(registration id, environment, Canvas user id)` in
  `LMS_CANVAS_OAUTH2_AUTHZ` (entity `CanvasOAuth2Authz`, repository
  `CanvasOAuth2AuthzRepository`) — access/refresh tokens encrypted at rest via
  `canvas.oauth2.encryptionPassword`/`encryptionSalt`. Table DDL:
  `sql/2026/2026-08-14_CANVAS_OAUTH2/lms_canvas_sql_release.sql`.
- A tool that adopts this gets a `CanvasRestTemplateAsUser` bean (defined in
  `CanvasOAuth2ClientConfig`, gated by `canvas.oauth2.enabled=true`) — use this
  `RestTemplate` instead of the admin-token ones for any call that should run as the
  logged-in user.
- A generic consent/callback UI is included, so no adopting tool needs to build its
  own: `OAuth2ConsentControllerAdvice` catches authorization-required exceptions and
  shows a "connect your Canvas account" interstitial; `OAuth2CallbackController`
  handles the return trip from Canvas and shows a "connected" (or error) page before
  redirecting back into the tool. Default English copy lives in
  `CanvasOAuth2ConsentText` and can be overridden per tool (see below).

## Adopting it in a new tool

1. **Add the annotation** to a `@Configuration` class (typically `WebApplication.java`,
   alongside `@EnableLtiClient`, which this depends on):

   ```java
   @EnableCanvasOAuth2Client(registrationIdSuffix = "mytool", rivetCssPathPrefix = "/app/jsrivet")
   ```

   Both attributes are required, with no defaults — see the full javadoc on
   `EnableCanvasOAuth2Client` for why. `rivetCssPathPrefix` must match wherever this
   tool already serves the `lms-canvas-rivet` webjar (check your
   `ApplicationConfig`'s resource handler mapping for `jsrivet`).

2. **Register the OAuth2 client in `application.yml`.** Spring's config-binding map
   keys aren't placeholder-resolvable, so this key has to be typed out by hand as
   `"lms_canvas_oauth2_" + registrationIdSuffix` — it can't be derived from the
   annotation automatically:

   ```yaml
   spring:
     security:
       oauth2:
         client:
           registration:
             lms_canvas_oauth2_mytool:
               client-id: ${canvas.oauth2.clientId}
               client-secret: ${canvas.oauth2.clientSecret}
               authorization-grant-type: authorization_code
               client-authentication-method: client_secret_post
               redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
               scope: [url:GET|/api/v1/courses/:course_id/users]  # whatever this tool actually needs
           provider:
             lms_canvas_oauth2_mytool:
               authorization-uri: ${canvas.baseUrl}/login/oauth2/auth
               token-uri: ${canvas.baseUrl}/login/oauth2/token

   canvas:
     oauth2:
       enabled: true
       clientId: ${canvas.oauth2.clientId.mytool}
       clientSecret: ${canvas.oauth2.clientSecret.mytool}
   ```

   See `tools/viewem/src/main/resources/application.yml` for a complete worked
   example, including an important note there: `canvas.oauth2.enabled` only gates the
   `CanvasRestTemplateAsUser` bean — the rest of this module's config (including the
   authorized-client repository, which needs the encryption properties below) is
   activated unconditionally the moment `@EnableCanvasOAuth2Client` is present.

3. **Set the encryption secrets.** `canvas.oauth2.encryptionPassword` and
   `canvas.oauth2.encryptionSalt` are required as soon as the annotation is active —
   the app fails to start without them. Never commit real values; supply them the same
   way `canvas.token`/`lms.db.password` are supplied (`security.properties` in
   deployed environments, `protected.properties` locally — see the root `CLAUDE.md`).

4. **Wire the token-response client into Spring Security**, in your `SecurityConfig`:

   ```java
   @Autowired
   private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> canvasOAuth2AccessTokenResponseClient;

   // ...
   .oauth2Client(oauth2 -> oauth2
       .authorizationCodeGrant(codeGrant -> codeGrant
           .accessTokenResponseClient(canvasOAuth2AccessTokenResponseClient)))
   ```

5. **Get a Canvas developer key** for this tool (out-of-band, in Canvas's admin
   developer-keys UI) and set its client id/secret as described above. Not something
   this repo can verify from source — coordinate with whoever administers your Canvas
   instance(s).

6. **Run the DB migration** so `LMS_CANVAS_OAUTH2_AUTHZ` exists in this tool's
   database — see `sql/2026/2026-08-14_CANVAS_OAUTH2/lms_canvas_sql_release.sql` for
   the reference DDL.

Once wired up, inject `CanvasRestTemplateAsUser` wherever a call should run as the
logged-in user instead of the admin token, e.g.:

```java
@Autowired
@Qualifier("CanvasRestTemplateAsUser")
private RestTemplate canvasRestTemplateAsUser;
```

## Deciding whether a tool is a good candidate

Evaluate every candidate against **two distinct goals** — a tool can be a strong fit
for one and a poor fit for the other, and the right move depends on which one applies:

1. **Close an authorization gap.** Does the tool make an admin-token Canvas call whose
   result *should* be scoped to what the launching user can personally see/do, but
   isn't currently enforced that way — no `as_user_id` masquerade, and no equivalent
   check that the launching user actually holds the Canvas-side permission for the
   operation? If the tool's own gate (e.g. an LTI role claim) is weaker than what
   Canvas itself would require for that call, the admin token is silently
   papering over a real gap. Migrating that call makes Canvas's own permission model
   the authority again — the user gets a real 401/403 if they don't actually have
   rights, instead of the app doing it for them regardless.
2. **Shrink admin-token blast radius.** Even when every call is already correctly
   scoped (e.g. everything already goes out with `as_user_id` matching the launching
   user), the tool still depends on holding a token capable of acting as anyone.
   Migrating those calls to per-user OAuth2 removes that dependency, without changing
   behavior for anyone — it's pure hardening, not a bug fix.

For goal #2 specifically, trace every Canvas call to its actual REST endpoint and
classify it as self-scoped (`/users/self/...`, `as_user_id`-redundant, or resolvable
via a caller-context `include[]`) vs. genuinely account/admin-scoped data that can't
be — don't assume a call needs admin access just because that's how it's fetched
today; check whether a narrower-scoped endpoint already returns the same thing
pre-resolved for the caller.

A tool can also turn out to be a poor fit for *both*: if an operation is genuinely
account/admin-gated in Canvas (e.g. requires `manage_master_courses`) and the tool is
meant to be usable by users who don't hold that permission personally, per-user OAuth2
isn't viable — Canvas would reject the call. In that situation, treat the mismatch
between the tool's own (weaker) authorization check and what Canvas actually requires
as the real finding, and resolve it with whoever owns that tool's product decisions
before assuming a migration is the fix.

## Current adopters

| Tool | Goal | Status |
|---|---|---|
| `viewem` | #1 — one call (`courseService.getRosterForCourseAsUser`, the instructor roster fetch) was an optional, defense-in-depth re-check that the launching user can currently see that specific roster | Done — first adopter, proof of concept |
| `courselist` | #2 — every call was already `as_user_id`-scoped to the launching user; migrating removes its dependency on the admin token entirely | Done — after migration, `courselist` has no remaining Canvas call that needs admin-level access |

