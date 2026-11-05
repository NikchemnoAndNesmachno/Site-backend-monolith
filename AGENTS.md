# Codex instructions

- Read [Architecture](docs/architecture.md) to identify the owning module and its boundaries. Use [Development and verification](docs/development.md) for Maven commands, CI expectations, and checks to make before editing.
- Start changes in the owning module and follow its existing package layout. For cross-module behavior, use the interfaces and DTOs in `contract-api`. Keep feature repositories and entities internal.
- Inspect the root and affected module POMs before changing dependencies. For endpoint or schema changes, check the central security configuration and the owning Liquibase changelog, respectively.
- Run the relevant Maven wrapper checks and report the commands, results, and any checks the environment prevented.
