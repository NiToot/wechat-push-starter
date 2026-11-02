# Contributing

## Validate changes without real accounts

Run commands from the repository root using Maven and JDK 11 (the version used
by the workflow). The source targets Java 8. Tests use Mockito instrumentation
and a `SecurityManager` network guard, so compatibility with newer JDKs should
not be assumed.

```shell
mvn -version
mvn -B -Dfile.encoding=UTF-8 package
```

In Windows PowerShell, quote the Maven property:

```powershell
mvn -B '-Dfile.encoding=UTF-8' package
```

No WeChat credentials, Amap key, recipient configuration, or repository Secrets
are required for this build and its tests. Keep the shipped empty recipient
configuration when checking a general-purpose change. Maven may download build
plugins and dependencies; the offline test guard blocks application connections
to external services, not Maven downloads. Once dependencies are cached, add
`-o` to build with Maven offline.

A successful run ends with `BUILD SUCCESS` and creates
`target/VxMessageActions-1.0-SNAPSHOT.jar`. Test reports are written to
`target/surefire-reports/`. Do not use `-DskipTests` when reporting validation.

## Find the relevant existing checks

| Change area | Tests to inspect |
| --- | --- |
| Required settings, duplicate recipients, dates, language validation | `WechatConfigTest` |
| Startup validation and check-only mode avoiding the sending service | `StartupCheckTest` |
| Basic, birthday, couple payloads and all six template files | `PortableMessageTest` |
| English and Chinese field assembly and weather translations | `MessageLanguageTest` |
| Calendar edge cases | `CalendarBoundaryTest` |
| Weather responses and poetry fallback | `GaoDeWeatherServiceTest`, `ApiMessageServiceTest` |
| HTTP behavior and sending errors | `VxUtilTest` |

Tests live under [src/test/java](src/test/java). Service tests inherit from
[OfflineTest](src/test/java/com/pt/vx/OfflineTest.java), which rejects network
connections after Mockito initialization. Stub external calls when adding tests;
keep this guard in place.

For a focused check, use a test class name, for example:

```shell
mvn -B -Dfile.encoding=UTF-8 -Dtest=PortableMessageTest test
```

In PowerShell, quote each `-D...` argument. Run the full `package` command before
submitting, since focused tests do not cover the other areas or package the JAR.

## Understand the execution boundary

| Action | Expected behavior |
| --- | --- |
| `mvn ... package` | Runs tests and builds the JAR; does not send messages. |
| `java -jar target/VxMessageActions-1.0-SNAPSHOT.jar --check-config` | Validates configuration without contacting external services. A fresh checkout fails because required settings are intentionally empty. |
| `java -jar target/VxMessageActions-1.0-SNAPSHOT.jar` | Attempts real sending to all configured recipients after validation. |

An empty-configuration failure from `--check-config` is expected and is not a
substitute for passing tests. A successful configuration check does not establish
that real credentials, subscriptions, or dashboard templates work.

The workflow runs only on manual dispatch or an enabled schedule; it has no
`push` or `pull_request` trigger. Creating a PR alone therefore does not run
these checks. Manual runs build and validate; sending requires the send option.
An enabled scheduled run sends to every configured recipient. See
[maven.yml](.github/workflows/maven.yml) before triggering a run.

## Prepare a reviewable contribution

- Explain the user-visible problem, changed files, and commands actually run.
- For template changes, check both language versions and keep the required
  placeholders; run `PortableMessageTest` and the full build.
- Keep credentials, actual OpenIDs, personal dates, `.env` files, logs, and
  generated `target/` output out of the contribution.
- State any untested service behavior or phone display; offline tests cannot
  establish live delivery. See [validation notes](docs/validation.md) for the
  existing validation scope.
