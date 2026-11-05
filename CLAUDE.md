# Claude Code instructions

- Use [Architecture](docs/architecture.md) to locate the owning module and [Development and verification](docs/development.md) to choose build and test commands.
- Before editing, inspect the owning module's source, tests, and POM. When tracing a feature across modules, start with the interface in `contract-api` and locate the Spring bean that implements it.
- Keep changes in the owning module. Check the security matcher for route changes and the module changelog for schema changes.
- Run the applicable verification commands before summarizing the change. Report any checks you could not run.
