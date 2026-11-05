# NiN backend architecture

NiN runs as one Spring Boot application backed by one PostgreSQL database. The root [Maven POM](../pom.xml) defines a nine-module reactor. The `app` module depends on the feature modules and packages the executable JAR. [AppApplication](../app/src/main/java/ua/nin/AppApplication.java) sits in `ua.nin`, so Spring discovers components in the feature packages on the same classpath.

## Maven modules

| Module | Ownership |
| --- | --- |
| [`app`](../app/pom.xml) | Application entry point, runtime configuration, executable packaging, root Liquibase changelog and aggregate JaCoCo coverage. |
| [`common`](../common/pom.xml) | Shared HTTP error response and handler utilities, request logging, Logback configuration, constants and general-purpose utilities. |
| [`contract-api`](../contract-api/pom.xml) | Java interfaces and data types shared across feature boundaries, including feed query and statistics ports and the profile-creation contract. |
| [`feed`](../feed/pom.xml) | Feed HTTP endpoint and response assembly. Combines video results with reaction, comment and view counts. Owns no tables or repositories. |
| [`identity`](../identity/pom.xml) | Users, credentials, JWT and refresh tokens, OAuth2, email verification, password recovery and profiles. Owns application-wide HTTP security. |
| [`media`](../media/pom.xml) | Media assets, bundles, file storage, video metadata and user avatars. Provides the base video queries used by the feed. |
| [`reactions`](../reactions/pom.xml) | Reaction types, user reactions on targets and aggregate reaction counts. |
| [`comments`](../comments/pom.xml) | Comments, threaded replies and comment counts. Owns the comment closure table used to traverse threads. |
| [`views`](../views/pom.xml) | View recording, unique-view tracking and view counts. |

## Module boundaries

- Put business rules, controllers and persistence in the module that owns the feature. Put orchestration in the consuming feature, as `feed` does, and application-wide composition in `app`.
- Call another feature in-process through an interface in `contract-api`. The provider implements it as a Spring bean and the consumer injects the interface. For example, `FeedService` uses `VideoFeedQueryPort` from `media` and the three statistics ports implemented by `reactions`, `comments` and `views`.
- Keep contract signatures in terms of IDs, values and contract DTOs. Feature entities, repositories and implementation classes stay with their owner. Avoid direct dependencies between feature POMs. Reserve `common` for reusable infrastructure and utilities.
- The database has existing cross-schema dependencies. [VideoRepository](../media/src/main/java/ua/nin/media/repository/VideoRepository.java) joins profile and view-count tables in native queries. Several changelogs also reference identity's user table. Check these consumers before changing schema names, keys or columns. Keep existing SQL dependencies separate from the Java API boundary.

## Source layout

- Feature Java code lives under `<module>/src/main/java/ua/nin/<feature>`. Follow existing packages for controllers, services, repositories, entities, DTOs, mappers and feature configuration. In `identity`, follow the `auth` and `profile` subtrees.
- Put module tests under `<module>/src/test/java`, mirroring the package under test. Put test-only resources in that module's `src/test/resources`.
- Application-wide settings live in [app's application.yaml](../app/src/main/resources/application.yaml). Shared logging configuration lives in [common's logback-spring.xml](../common/src/main/resources/logback-spring.xml).
- Add database changes in the owning module's `src/main/resources/db/changelog/logs/` and include them from its module changelog. The [app master changelog](../app/src/main/resources/db/changelog/db.changelog-master.xml) composes the identity, media, reactions, comments and views changelogs on the application classpath.

See [Development and verification](development.md) for build commands and repository invariants.
