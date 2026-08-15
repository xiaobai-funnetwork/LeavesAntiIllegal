# Repository Guidelines

## Project Structure & Module Organization

LeavesAntiIllegal is a Java 17, multi-module Maven plugin for Bukkit-compatible servers. Shared plugin code lives in `src/main/java/dev/leavesantiillegal`; commands, listeners, and scanners are grouped under their matching packages. Runtime resources are in `src/main/resources`, including `plugin.yml` and the default `config.yml`.

Server-version adapters live in `versions/v1_20_1`, `versions/v1_21`, and `versions/v26_2`. Keep version-specific compatibility code there and shared behavior in the root source tree. The `plugin` module assembles the shaded distributable JAR. Static user and developer documentation is under `docs/`, with images and styles in `docs/assets/`.

## Build, Test, and Development Commands

- `mvn -s .mvn/settings.xml clean package` compiles every module and creates `plugin/target/LeavesAntiIllegal-<version>.jar`.
- `mvn -s .mvn/settings.xml test` runs all configured Maven tests.
- `mvn -pl plugin -am -s .mvn/settings.xml package` builds the plugin and all required adapter modules.
- `mvn -Drevision=3.1.1 clean package` overrides the CI-friendly version property; release builds obtain this value from `version.txt`.

Use JDK 17 or newer. For local runtime checks, place the packaged JAR in a disposable Bukkit-compatible server's `plugins/` directory and inspect startup logs and generated configuration.

## Coding Style & Naming Conventions

Follow the existing Java style: four-space indentation, braces on the same line, one public top-level class per file, and explicit imports. Use `PascalCase` for classes, `camelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants. Keep packages lowercase beneath `dev.leavesantiillegal`. Prefer Bukkit public APIs; do not introduce NMS or CraftBukkit implementation dependencies. Preserve the existing boundary between main-thread Bukkit inventory/world access and asynchronous file or NBT work.

No formatter or linter is configured, so match nearby code and keep diffs focused.

## Testing Guidelines

There is currently no committed test suite or coverage threshold. Add tests under the relevant module's `src/test/java` tree and name them `*Test.java`. Before submitting, run the full Maven package command. Manually verify affected commands, listeners, scanner scheduling, configuration reloads, and supported server adapters; use `dry-run` and backups when exercising offline player-data scans.

## Commit & Pull Request Guidelines

History uses short, imperative summaries such as `Set up github workflow` and `Fix Download Source`. Write a concise subject that states the outcome; include a version prefix only for release work. Pull requests should explain behavior changes, list tested server versions and commands, link related issues, and call out configuration or migration effects. Include logs for runtime changes and screenshots only for documentation UI changes.
