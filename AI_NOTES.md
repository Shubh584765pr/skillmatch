# AI notes

## Tools used

- **Claude Code** (Claude Opus, in the terminal) – planning, backend, Android app, running and checking the app on a real phone over `adb`.
- **Android Studio** – project setup, running the app.

## How AI was used

| Stage | What the AI did | How it was checked |
|---|---|---|
| Planning | Read the assessment PDF and the app screenshots, listed what each screen does, proposed scope (must-haves first, then profile + Hindi), stack and build order. | I reviewed the plan and changed it: native Kotlin instead of Expo, Hindi as the second language, and no tests. |
| Backend | Wrote the schema, seed data, matching SQL and route handlers. | Type-checked (`tsc`), then every endpoint was called with curl, including error cases: wrong password, no token, bad/unknown ids, duplicate email, invalid language, unknown skill id, `%` in search. |
| Android | Wrote the Compose screens, ViewModels and API client. | Built with Gradle, installed on a phone, and every flow was driven and screenshotted: login → list → search → detail → save → Saved tab → profile → Hindi → add skill → match updates. |
| Review | Looked at screenshots and code for visual and logic issues. | Fixes listed below. |

## Key prompts

- *"[assessment PDF] the assessment I got, I need to build an app … backend in Next.js with local DB Postgres hosted on Docker…"* – starting point; the AI read the brief and asked about the screens.
- *"I have attached screenshots in root folder"* – the AI read the 7 Play Store screenshots and described each screen (Opportunities, My Skills, Explore Learnings).
- *"Nice to have … A profile screen that saves settings / Notifications or a second language"* – added profile + language to scope.
- *"Hindi is fine, go ahead and build it"*
- *"I have Android Studio ready to go … no need to use Expo, I have Android app setup already"* – switched the app to native Kotlin.

## Where the AI got it wrong (and how it was fixed)

1. **Assumed an emulator.** The AI set the API address to `10.0.2.2` (the emulator's address for the PC). The device was actually a physical phone, where that address doesn't work. Fixed by switching to `localhost` + `adb reverse tcp:3000 tcp:3000`, moving the URL into `gradle.properties`, and making the "can't reach server" error message say what to run.
2. **Installed a stale APK and didn't notice at first.** A build command was piped through `head`, so a Gradle failure was hidden and the *old* APK was installed. Caught by reading the output; fixed by re-running with `set -o pipefail` so a failed build stops the chain.
3. **Broke a string resource.** An automated edit wrote `Can't` without escaping the apostrophe in `strings.xml`, which failed the Android resource compiler. Fixed to `Can\'t`.
4. **Purple UI in a teal app.** The theme only set primary colours, so Material 3 filled selected chips, the bottom bar, dialogs and progress tracks with its default purple. Spotted in phone screenshots; fixed by setting the secondary and surface-container colours.
5. **Red marker on "No gaps".** The gaps line always used a red marker, even when there were no gaps. Spotted in a screenshot; it now turns green.
6. **Caught before running:** a route handler with no request argument (`/api/skills`) would have been pre-rendered at Docker build time, when there is no database, so it was marked `force-dynamic`. Nested `Scaffold`s would also have added the status-bar padding twice.
